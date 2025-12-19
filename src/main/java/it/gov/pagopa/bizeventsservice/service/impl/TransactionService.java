package it.gov.pagopa.bizeventsservice.service.impl;

import com.azure.spring.data.cosmos.core.query.CosmosPageRequest;
import feign.FeignException;
import it.gov.pagopa.bizeventsservice.client.IReceiptGeneratePDFClient;
import it.gov.pagopa.bizeventsservice.client.IReceiptGetPDFClient;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewCart;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewGeneral;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewUser;
import it.gov.pagopa.bizeventsservice.exception.AppError;
import it.gov.pagopa.bizeventsservice.exception.AppException;
import it.gov.pagopa.bizeventsservice.mapper.ConvertViewsToTransactionDetailResponse;
import it.gov.pagopa.bizeventsservice.model.filterandorder.Order.TransactionListOrder;
import it.gov.pagopa.bizeventsservice.model.response.AttachmentsDetailsResponse;
import it.gov.pagopa.bizeventsservice.model.response.paidnotice.NoticeDetailResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionDetailResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListItem;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListResponse;
import it.gov.pagopa.bizeventsservice.repository.primary.BizEventsViewCartRepository;
import it.gov.pagopa.bizeventsservice.repository.primary.BizEventsViewGeneralRepository;
import it.gov.pagopa.bizeventsservice.repository.primary.BizEventsViewUserRepository;
import it.gov.pagopa.bizeventsservice.service.ITransactionService;
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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TransactionService implements ITransactionService {

    private static final String CART = "_CART_";
    private final BizEventsViewGeneralRepository bizEventsViewGeneralRepository;
    private final BizEventsViewCartRepository bizEventsViewCartRepository;
    private final BizEventsViewUserRepository bizEventsViewUserRepository;
    private final IReceiptGetPDFClient receiptClient;
    private final IReceiptGeneratePDFClient generateReceiptClient;

    @Autowired
    public TransactionService(
            BizEventsViewGeneralRepository bizEventsViewGeneralRepository,
            BizEventsViewCartRepository bizEventsViewCartRepository,
            BizEventsViewUserRepository bizEventsViewUserRepository,
            IReceiptGetPDFClient receiptClient,
            IReceiptGeneratePDFClient generateReceiptClient
    ) {
        this.bizEventsViewGeneralRepository = bizEventsViewGeneralRepository;
        this.bizEventsViewCartRepository = bizEventsViewCartRepository;
        this.bizEventsViewUserRepository = bizEventsViewUserRepository;
        this.receiptClient = receiptClient;
        this.generateReceiptClient = generateReceiptClient;
    }

    @Cacheable("noticeList")
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

        Map<String, BizEventsViewUser> viewUserGrouped = listOfViewUser.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        BizEventsViewUser::getTransactionId,
                        Function.identity(),
                        (u1, u2) -> u1,
                        LinkedHashMap::new
                ));
        List<BizEventsViewCart> bizEventsViewCart = this.bizEventsViewCartRepository.findByTransactionIdIn(viewUserGrouped.keySet());

        if (bizEventsViewCart.isEmpty()) {
            return TransactionListResponse.builder()
                    .transactionList(listOfTransactionListItem)
                    .continuationToken(null)
                    .build();
        }

        Map<String, List<BizEventsViewCart>> viewCartGrouped = bizEventsViewCart.stream()
                .collect(Collectors.groupingBy(BizEventsViewCart::getTransactionId));

        for (BizEventsViewUser viewUser : viewUserGrouped.values()) {
            List<BizEventsViewCart> viewCarts = viewCartGrouped.get(viewUser.getTransactionId());

            if (viewCarts != null && !viewCarts.isEmpty()) {
                boolean isCart = viewCarts.size() > 1;
                for (BizEventsViewCart viewCart : viewCarts) {
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
        }

        CosmosPageRequest pageResponse = (CosmosPageRequest) page.getPageable().next();
        String nextToken = pageResponse.getRequestContinuation();

        return TransactionListResponse.builder()
                .transactionList(listOfTransactionListItem)
                .continuationToken(nextToken)
                .build();
    }

    @Override
    public TransactionDetailResponse getTransactionDetails(String taxCode, String eventReference) {
        List<BizEventsViewGeneral> bizEventsViewGeneral = this.bizEventsViewGeneralRepository.findByTransactionId(eventReference);
        if (bizEventsViewGeneral.isEmpty()) {
            throw new AppException(AppError.VIEW_GENERAL_NOT_FOUND_WITH_ID, eventReference);
        }

        List<BizEventsViewCart> listOfCartViews;
        if (bizEventsViewGeneral.get(0).getPayer() != null && bizEventsViewGeneral.get(0).getPayer().getTaxCode().equals(taxCode)) {
            listOfCartViews = this.bizEventsViewCartRepository.getBizEventsViewCartByTransactionId(eventReference);
        } else {
            listOfCartViews = this.bizEventsViewCartRepository.getBizEventsViewCartByTransactionIdAndFilteredByTaxCode(eventReference, taxCode);
        }
        if (listOfCartViews.isEmpty()) {
            throw new AppException(AppError.VIEW_CART_NOT_FOUND_WITH_TRANSACTION_ID_AND_TAX_CODE, eventReference);
        }

        return ConvertViewsToTransactionDetailResponse.convertTransactionDetails(taxCode, bizEventsViewGeneral.get(0), listOfCartViews);
    }

    @Cacheable("noticeDetails")
    @Override
    public NoticeDetailResponse getPaidNoticeDetail(String taxCode, String transactionId) {
        List<BizEventsViewGeneral> bizEventsViewGeneral = this.bizEventsViewGeneralRepository.findByTransactionId(transactionId);
        if (bizEventsViewGeneral.isEmpty()) {
            throw new AppException(AppError.VIEW_GENERAL_NOT_FOUND_WITH_ID, transactionId);
        }

        List<BizEventsViewCart> listOfCartViews;
        if (bizEventsViewGeneral.get(0).getPayer() != null && bizEventsViewGeneral.get(0).getPayer().getTaxCode().equals(taxCode)) {
            listOfCartViews = this.bizEventsViewCartRepository.getBizEventsViewCartByTransactionId(transactionId);
        } else {
            listOfCartViews = this.bizEventsViewCartRepository.getBizEventsViewCartByTransactionIdAndFilteredByTaxCode(transactionId, taxCode);
        }
        if (listOfCartViews.isEmpty()) {
            throw new AppException(AppError.VIEW_CART_NOT_FOUND_WITH_TRANSACTION_ID_AND_TAX_CODE, transactionId);
        }

        return ConvertViewsToTransactionDetailResponse.convertPaidNoticeDetails(taxCode, bizEventsViewGeneral.get(0), listOfCartViews);
    }


    @Override
    public void disableTransaction(String fiscalCode, String transactionId) {

        List<BizEventsViewUser> listOfViewUser = this.bizEventsViewUserRepository
                .getBizEventsViewUserByTaxCodeAndTransactionId(fiscalCode, transactionId);

        setHiddenAndSave(fiscalCode, transactionId, listOfViewUser);
    }

    @Override
    public void disablePaidNotice(String fiscalCode, String transactionId) {
        List<BizEventsViewUser> listOfViewUser;

        if(transactionId.contains(CART)){
            // if the transactionId contains _CART_ it means that it's a cart transaction
            String transaction = transactionId.split(CART)[0];
            boolean isDebtor = transactionId.split(CART).length > 1;
            if(isDebtor){
                // if there is something after _CART_ it means that we have to filter also by eventId for debtor
                String eventId = transactionId.split(CART)[1];
                 listOfViewUser = this.bizEventsViewUserRepository
                        .findByFiscalCodeAndTransactionIdAndEventId(fiscalCode, transaction, eventId);
            }
            else {
                // if there is nothing after _CART_ it means that we have to filter only by transactionId for payer
                listOfViewUser = this.bizEventsViewUserRepository
                        .getBizEventsViewUserByTaxCodeAndTransactionId(fiscalCode, transactionId);
            }
        }
        else {
        // single paid notice transaction
         listOfViewUser = this.bizEventsViewUserRepository
                .getBizEventsViewUserByTaxCodeAndTransactionId(fiscalCode, transactionId);
        }

        // set hidden to true and save
        setHiddenAndSave(fiscalCode, transactionId, listOfViewUser);
    }

    /**
     * This method sets the 'hidden' attribute to true for all BizEventsViewUser entities in the provided list
     *
     * @param fiscalCode the user fiscal code
     * @param transactionId the transaction id
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
    public byte[] getPDFReceipt(String fiscalCode, String eventId) {
        return this.acquirePDFReceipt(fiscalCode, eventId);
    }

    @Override
    public ResponseEntity<Resource> getPDFReceiptResponse(String fiscalCode, String eventId) {
        var attachmentDetails = getAttachmentDetails(fiscalCode, eventId);
        var name = attachmentDetails.getAttachments().get(0).getName();
        var url = attachmentDetails.getAttachments().get(0).getUrl();
        var receiptFile = getAttachmentFile(fiscalCode, eventId, url);

        return ResponseEntity
                .ok()
                .contentLength(receiptFile.length)
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(Optional.ofNullable(name).orElse("Receipt.pdf")).build().toString())
                .body(new ByteArrayResource(receiptFile));
    }

    private byte[] acquirePDFReceipt(String fiscalCode, String eventId) {
        String url = getAttachmentDetails(fiscalCode, eventId).getAttachments().get(0).getUrl();
        return this.getAttachmentFile(fiscalCode, eventId, url);
    }

    private AttachmentsDetailsResponse getAttachmentDetails(String fiscalCode, String eventId) {
        try {
            // call the receipt-pdf-service to retrieve the PDF receipt details
            return receiptClient.getAttachments(fiscalCode, eventId);
        } catch (FeignException.NotFound e) {
            generateReceiptClient.generateReceipt(eventId, "false", "{}");
            return receiptClient.getAttachments(fiscalCode, eventId);
        } catch (FeignException.InternalServerError e) {
            String responseBody = e.contentUTF8();
            if (responseBody != null && responseBody.contains("PDFS_700")) {
                generateReceiptClient.generateReceipt(eventId, "false", "{}");
                return receiptClient.getAttachments(fiscalCode, eventId);
            } else {
                throw e; // rethrow the exception if this is not the expected case
            }
        }
    }

    private byte[] getAttachmentFile(String fiscalCode, String eventId, String url) {
        try {
            // call the receipt-pdf-service to retrieve the PDF receipt attachment
            return receiptClient.getReceipt(fiscalCode, eventId, url);
        } catch (FeignException.NotFound e) {
            // re-generate the PDF receipt and return the generated file by getReceipt call
            generateReceiptClient.generateReceipt(eventId, "false", "{}");
            return receiptClient.getReceipt(fiscalCode, eventId, url);
        }
    }

    private CosmosPageRequest getCosmosPageRequest(
            String continuationToken,
            Integer size,
            TransactionListOrder orderBy,
            Direction ordering
    ) {
        String columnName = Optional.ofNullable(orderBy).map(o -> o.getColumnName()).orElse("transactionDate");
        String direction = Optional.ofNullable(ordering).map(Enum::name).orElse(Direction.DESC.name());

        final Sort sort = Sort.by(Direction.fromString(direction), columnName);
        return new CosmosPageRequest(0, size, continuationToken, sort);
    }
}
