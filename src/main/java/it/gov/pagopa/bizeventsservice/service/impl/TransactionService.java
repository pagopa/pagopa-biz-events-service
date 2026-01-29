package it.gov.pagopa.bizeventsservice.service.impl;

import com.azure.spring.data.cosmos.core.query.CosmosPageRequest;
import feign.FeignException;
import it.gov.pagopa.bizeventsservice.client.IReceiptGeneratePDFClient;
import it.gov.pagopa.bizeventsservice.client.IReceiptGetPDFClient;
import it.gov.pagopa.bizeventsservice.entity.BizEvent;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewCart;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewGeneral;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewUser;
import it.gov.pagopa.bizeventsservice.exception.AppError;
import it.gov.pagopa.bizeventsservice.exception.AppException;
import it.gov.pagopa.bizeventsservice.mapper.ConvertViewsToTransactionDetailResponse;
import it.gov.pagopa.bizeventsservice.model.filterandorder.Order.TransactionListOrder;
import it.gov.pagopa.bizeventsservice.model.response.paidnotice.NoticeDetailResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListItem;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListResponse;
import it.gov.pagopa.bizeventsservice.repository.primary.BizEventsViewCartRepository;
import it.gov.pagopa.bizeventsservice.repository.primary.BizEventsViewGeneralRepository;
import it.gov.pagopa.bizeventsservice.repository.primary.BizEventsViewUserRepository;
import it.gov.pagopa.bizeventsservice.service.IBizEventsService;
import it.gov.pagopa.bizeventsservice.service.ITransactionService;
import it.gov.pagopa.bizeventsservice.util.CacheService;
import it.gov.pagopa.bizeventsservice.util.TransactionIdFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotBlank;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static it.gov.pagopa.bizeventsservice.exception.enumeration.ReceiptServiceStatusCode.*;
import static it.gov.pagopa.bizeventsservice.util.TransactionIdFactory.isCart;

@Service
@Slf4j
public class TransactionService implements ITransactionService {

    private final BizEventsViewGeneralRepository bizEventsViewGeneralRepository;
    private final BizEventsViewCartRepository bizEventsViewCartRepository;
    private final BizEventsViewUserRepository bizEventsViewUserRepository;
    private final IReceiptGetPDFClient receiptClient;
    private final IReceiptGeneratePDFClient generateReceiptClient;
    private final IBizEventsService bizEventsService;
    private final CacheService cacheService;

    @Autowired
    public TransactionService(
            BizEventsViewGeneralRepository bizEventsViewGeneralRepository,
            BizEventsViewCartRepository bizEventsViewCartRepository,
            BizEventsViewUserRepository bizEventsViewUserRepository,
            IReceiptGetPDFClient receiptClient,
            IReceiptGeneratePDFClient generateReceiptClient,
            IBizEventsService bizEventsService,
            CacheService cacheService) {
        this.bizEventsViewGeneralRepository = bizEventsViewGeneralRepository;
        this.bizEventsViewCartRepository = bizEventsViewCartRepository;
        this.bizEventsViewUserRepository = bizEventsViewUserRepository;
        this.receiptClient = receiptClient;
        this.generateReceiptClient = generateReceiptClient;
        this.bizEventsService = bizEventsService;
        this.cacheService = cacheService;
    }

    @Cacheable(value = "noticeList")
    @Override
    public TransactionListResponse getTransactionList(
            String taxCode,
            Boolean isPayer,
            Boolean isDebtor,
            String continuationToken,
            Integer size,
            TransactionListOrder orderBy,
            Direction ordering
    ) {
        List<TransactionListItem> listOfTransactionListItem = new ArrayList<>();

        final CosmosPageRequest pageRequest = getCosmosPageRequest(continuationToken, size, orderBy, ordering);
        final Page<BizEventsViewUser> page = this.bizEventsViewUserRepository
                .getBizEventsViewUserByTaxCode(taxCode, isPayer, isDebtor, pageRequest);
        List<BizEventsViewUser> listOfViewUser = page.getContent();

        if (listOfViewUser.isEmpty()) {
            return TransactionListResponse.builder()
                    .transactionList(listOfTransactionListItem)
                    .continuationToken(null)
                    .build();
        }

        Set<String> uniqueTransactionIds = listOfViewUser.stream()
                .map(BizEventsViewUser::getTransactionId)
                .collect(Collectors.toSet());
        List<BizEventsViewCart> bizEventsViewCart = this.bizEventsViewCartRepository.findByTransactionIdIn(uniqueTransactionIds);

        if (bizEventsViewCart.isEmpty()) {
            return TransactionListResponse.builder()
                    .transactionList(listOfTransactionListItem)
                    .continuationToken(null)
                    .build();
        }

        Map<String, Boolean> viewCartsGroupedByTrxId = bizEventsViewCart.stream()
                .collect(Collectors.toMap(
                        BizEventsViewCart::getTransactionId,
                        firstOccurrence -> false,
                        (existingOccurrence, newOccurrence) -> true
                ));

        for (BizEventsViewUser viewUser : listOfViewUser) {

            // Removing "-d" or "-p" suffix
            final String viewUserIdPrefix = viewUser.getId().substring(0, viewUser.getId().length() - 2);

            Optional<BizEventsViewCart> viewCartAssociatedToViewUser = bizEventsViewCart.stream()
                    .filter(cart -> viewUserIdPrefix.equals(cart.getId()))
                    .findFirst();
            if (viewCartAssociatedToViewUser.isPresent()) {

                boolean isCart = viewCartsGroupedByTrxId.getOrDefault(viewUser.getTransactionId(), false);

                BizEventsViewCart viewCart = viewCartAssociatedToViewUser.get();
                TransactionListItem transactionListItem =
                        ConvertViewsToTransactionDetailResponse
                                .convertTransactionListItem(
                                        viewUser,
                                        viewCart,
                                        isCart
                                );
                listOfTransactionListItem.add(transactionListItem);
            }
        }

        CosmosPageRequest pageResponse = (CosmosPageRequest) page.getPageable().next();
        String nextToken = pageResponse.getRequestContinuation();

        return TransactionListResponse.builder()
                .transactionList(listOfTransactionListItem)
                .continuationToken(nextToken)
                .build();
    }

    @Cacheable("noticeDetails")
    @Override
    public NoticeDetailResponse getPaidNoticeDetail(String taxCode, String transactionId) {

        // Split passed transaction ID made as <viewUser.transactionId>_CART_<viewCart.id
        TransactionIdFactory.ViewTransactionId viewTransactionId = TransactionIdFactory.extract(transactionId);
        String extractedTransactionId = viewTransactionId.transactionId();
        String extractedViewCartId = viewTransactionId.eventId();

        List<BizEventsViewGeneral> bizEventsViewGeneral = this.bizEventsViewGeneralRepository.findByTransactionId(extractedTransactionId);
        if (bizEventsViewGeneral.isEmpty()) {
            throw new AppException(AppError.VIEW_GENERAL_NOT_FOUND_WITH_ID, transactionId);
        }

        List<BizEventsViewCart> listOfCartViews;

        boolean isPayer = bizEventsViewGeneral.get(0).getPayer() != null && taxCode.equals(bizEventsViewGeneral.get(0).getPayer().getTaxCode());
        boolean isCart = isCart(transactionId);
        if (isCart && !isPayer) {
            listOfCartViews = this.bizEventsViewCartRepository.getBizEventsViewCartByTransactionIdAndIdAndFilteredByTaxCode(extractedTransactionId, extractedViewCartId, taxCode);
        } else {
            listOfCartViews = this.bizEventsViewCartRepository.getBizEventsViewCartByTransactionId(extractedTransactionId);
        }
        if (listOfCartViews.isEmpty()) {
            throw new AppException(AppError.VIEW_CART_NOT_FOUND_WITH_TRANSACTION_ID_AND_TAX_CODE, transactionId);
        }

        return ConvertViewsToTransactionDetailResponse.convertPaidNoticeDetails(taxCode, bizEventsViewGeneral.get(0), listOfCartViews);
    }

    @Override
    public void disablePaidNotice(String fiscalCode, String transactionId) {
        cacheService.evictNoticeListByTaxCode(fiscalCode);

        List<BizEventsViewUser> listOfViewUser;

        TransactionIdFactory.ViewTransactionId viewTransactionId = TransactionIdFactory.extract(transactionId);
        String transaction = viewTransactionId.transactionId();
        boolean isDebtor = viewTransactionId.eventId() != null;

        // if the transactionId contains _CART_ it means that it's a cart transaction
        if (isCart(transactionId) && isDebtor) {
            // if there is something after _CART_ it means that we have to filter also by eventId for debtor
            listOfViewUser = this.bizEventsViewUserRepository
                    .findByFiscalCodeAndTransactionIdAndEventId(fiscalCode, transaction, viewTransactionId.eventId());
        } else {
            // single paid notice transaction
            listOfViewUser = this.bizEventsViewUserRepository
                    .getBizEventsViewUserByTaxCodeAndTransactionId(fiscalCode, transaction);
        }

        // set hidden to true and save
        setHiddenAndSave(fiscalCode, transactionId, listOfViewUser);
    }

    /**
     * This method sets the 'hidden' attribute to true for all BizEventsViewUser entities in the provided list
     *
     * @param fiscalCode     the user fiscal code
     * @param transactionId  the transaction id
     * @param listOfViewUser the list of BizEventsViewUser Entities to be updated
     */
    private void setHiddenAndSave(String fiscalCode, String transactionId, List<BizEventsViewUser> listOfViewUser) {
        if (CollectionUtils.isEmpty(listOfViewUser)) {
            throw new AppException(AppError.VIEW_USER_NOT_FOUND_WITH_ID, fiscalCode, transactionId);
        }

        // set hidden to true for all paid notices with the same transactionId for the given fiscalCode
        listOfViewUser.forEach(u -> u.setHidden(true));
        bizEventsViewUserRepository.saveAll(listOfViewUser);
    }

    @Override
    public ResponseEntity<Resource> getPDFReceiptResponse(String fiscalCode, @NotBlank String eventId) {
        ResponseEntity<byte[]> response = getReceiptPdf(fiscalCode, eventId);
        byte[] receiptFile = response.getBody();

        if(receiptFile == null){
            throw new AppException(AppError.ATTACHMENT_NOT_FOUND, eventId);
        }

        return ResponseEntity
                .ok()
                .contentLength(receiptFile.length)
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(
                        response.getHeaders().getOrDefault("filename", Collections.singletonList("receipt.pdf")).get(0)
                ).build().toString())
                .body(new ByteArrayResource(receiptFile));
    }

    private ResponseEntity<byte[]> getReceiptPdf(String fiscalCode, String eventId) {
        try {
            return this.receiptClient.getReceiptPdf(fiscalCode, eventId);
        } catch (FeignException e) {
            String responseBody = e.contentUTF8();
            if (responseBody == null) {
                throw e;
            }
            // Receipt not yet generated
            if (responseBody.contains(PDFS_714.getErrorCode())) {
                throw new AppException(AppError.ATTACHMENT_GENERATING, eventId);
            }
            // Receipt generation failed, retry
            if (responseBody.contains(PDFS_715.getErrorCode())) {
                asyncRegenerate(eventId, isCart(eventId));
                throw new AppException(AppError.ATTACHMENT_GENERATING, eventId);
            }
            // Receipt generation failed, review needed
            if (responseBody.contains(PDFS_716.getErrorCode())) {
                throw new AppException(AppError.ATTACHMENT_NOT_FOUND, eventId);
            }
            // Receipt not found
            if (responseBody.contains(PDFS_800.getErrorCode()) || responseBody.contains(PDFS_801.getErrorCode())) {
                BizEvent bizEvent = this.bizEventsService.getBizEventFromLAPId(eventId);
                if (bizEvent.getTs().isBefore(OffsetDateTime.now().minusMinutes(30))) {
                    asyncRegenerate(eventId, isCart(eventId));
                }

                throw new AppException(AppError.ATTACHMENT_GENERATING, eventId);
            }
            // Fiscal code not authorized
            if (responseBody.contains(PDFS_706.getErrorCode())) {
                throw new AppException(AppError.INVALID_FISCAL_CODE, eventId);
            }
            // Rethrow the exception if this is not the expected case
            throw e;
        }
    }

    private void asyncRegenerate(String eventId, boolean isCart) {
        CompletableFuture.runAsync(() -> {
            try {
                regeneratePdf(eventId, isCart);
            } catch (Exception ex) {
                log.error("Error during the generation of the receipt", ex);
            }
        });
    }

    private void regeneratePdf(String event, boolean isCart) {
        if (isCart) {
            generateReceiptClient.generateReceiptCart(event);
        } else {
            generateReceiptClient.generateReceipt(event);
        }
    }

    private CosmosPageRequest getCosmosPageRequest(
            String continuationToken,
            Integer size,
            TransactionListOrder orderBy,
            Direction ordering
    ) {
        String columnName = Optional.ofNullable(orderBy).map(TransactionListOrder::getColumnName).orElse("transactionDate");
        String direction = Optional.ofNullable(ordering).map(Enum::name).orElse(Direction.DESC.name());

        final Sort sort = Sort.by(Direction.fromString(direction), columnName);
        return new CosmosPageRequest(0, size, continuationToken, sort);
    }
}
