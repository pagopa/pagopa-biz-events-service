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
import it.gov.pagopa.bizeventsservice.repository.BizEventsViewCartRepository;
import it.gov.pagopa.bizeventsservice.repository.BizEventsViewGeneralRepository;
import it.gov.pagopa.bizeventsservice.repository.BizEventsViewUserRepository;
import it.gov.pagopa.bizeventsservice.service.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import it.gov.pagopa.bizeventsservice.util.Constants;
import it.gov.pagopa.bizeventsservice.util.Util;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TransactionService implements ITransactionService {

    private final BizEventsViewGeneralRepository bizEventsViewGeneralRepository;
    private final BizEventsViewCartRepository bizEventsViewCartRepository;
    private final BizEventsViewUserRepository bizEventsViewUserRepository;
    private final IReceiptGetPDFClient receiptClient;
    private final IReceiptGeneratePDFClient generateReceiptClient;

    @Autowired
    public TransactionService(BizEventsViewGeneralRepository bizEventsViewGeneralRepository,
                              BizEventsViewCartRepository bizEventsViewCartRepository,
                              BizEventsViewUserRepository bizEventsViewUserRepository,
                              IReceiptGetPDFClient receiptClient,
                              IReceiptGeneratePDFClient generateReceiptClient) {
        this.bizEventsViewGeneralRepository = bizEventsViewGeneralRepository;
        this.bizEventsViewCartRepository = bizEventsViewCartRepository;
        this.bizEventsViewUserRepository = bizEventsViewUserRepository;
        this.receiptClient = receiptClient;
        this.generateReceiptClient = generateReceiptClient;
    }

    @Override
    public TransactionListResponse getTransactionList(String taxCode, Boolean isPayer,
                                                      Boolean isDebtor, String continuationToken, Integer size, TransactionListOrder orderBy, Direction ordering) {
        List<TransactionListItem> listOfTransactionListItem = new ArrayList<>();

        String columnName = Optional.ofNullable(orderBy).map(o -> o.getColumnName()).orElse("transactionDate");
        String direction = Optional.ofNullable(ordering).map(Enum::name).orElse(Sort.Direction.DESC.name());

        final Sort sort = Sort.by(Sort.Direction.fromString(direction), columnName);
        final CosmosPageRequest pageRequest = new CosmosPageRequest(0, size, continuationToken, sort);
        final Page<BizEventsViewUser> page = this.bizEventsViewUserRepository.getBizEventsViewUserByTaxCode(taxCode, isPayer, isDebtor, pageRequest);
        Set<String> set = new HashSet<>(page.getContent().size());

        List<BizEventsViewUser> listOfViewUser = page.getContent().stream()
                .sorted(Comparator.comparing(BizEventsViewUser::getIsDebtor, Comparator.reverseOrder()))
                .filter(p -> set.add(p.getTransactionId()))
                .collect(Collectors.toList())
                .stream()
                .sorted(Comparator.comparing(BizEventsViewUser::getTransactionDate, Comparator.reverseOrder()))
                .toList();

        if (listOfViewUser.isEmpty()) {
            throw new AppException(AppError.VIEW_USER_NOT_FOUND_WITH_TAX_CODE_AND_FILTER, taxCode, isPayer, isDebtor);
        }
        for (BizEventsViewUser viewUser : listOfViewUser) {
            List<BizEventsViewCart> listOfViewCart;
            if (Boolean.TRUE.equals(viewUser.getIsPayer())) {
                listOfViewCart = this.bizEventsViewCartRepository.getBizEventsViewCartByTransactionId(viewUser.getTransactionId());
            } else {
                listOfViewCart = this.bizEventsViewCartRepository.getBizEventsViewCartByTransactionIdAndFilteredByTaxCode(viewUser.getTransactionId(), taxCode);
            }

            if (!listOfViewCart.isEmpty()) {
                TransactionListItem transactionListItem = ConvertViewsToTransactionDetailResponse.convertTransactionListItem(viewUser, listOfViewCart);
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

    @Override
    public TransactionDetailResponse getTransactionDetails(String taxCode, String eventReference) {
        List<BizEventsViewGeneral> bizEventsViewGeneral = this.bizEventsViewGeneralRepository.findByTransactionId(eventReference);
        if (bizEventsViewGeneral.isEmpty()) {
            throw new AppException(AppError.VIEW_GENERAL_NOT_FOUND_WITH_TRANSACTION_ID, eventReference);
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

    @Override
    public NoticeDetailResponse getPaidNoticeDetail(String taxCode, String eventId) {
        List<BizEventsViewGeneral> bizEventsViewGeneral = this.bizEventsViewGeneralRepository.findByTransactionId(eventId);
        if (bizEventsViewGeneral.isEmpty()) {
            throw new AppException(AppError.VIEW_GENERAL_NOT_FOUND_WITH_TRANSACTION_ID, eventId);
        }

        List<BizEventsViewCart> listOfCartViews;
        if (bizEventsViewGeneral.get(0).getPayer() != null && bizEventsViewGeneral.get(0).getPayer().getTaxCode().equals(taxCode)) {
            listOfCartViews = this.bizEventsViewCartRepository.getBizEventsViewCartByTransactionId(eventId);
        } else {
            listOfCartViews = this.bizEventsViewCartRepository.getBizEventsViewCartByTransactionIdAndFilteredByTaxCode(eventId, taxCode);
        }
        if (listOfCartViews.isEmpty()) {
            throw new AppException(AppError.VIEW_CART_NOT_FOUND_WITH_TRANSACTION_ID_AND_TAX_CODE, eventId);
        }

        return ConvertViewsToTransactionDetailResponse.convertPaidNoticeDetails(taxCode, bizEventsViewGeneral.get(0), listOfCartViews);
    }


    @Override
    public void disableTransaction(String fiscalCode, String transactionId) {

        List<BizEventsViewUser> listOfViewUser = this.bizEventsViewUserRepository
                .getBizEventsViewUserByTaxCodeAndTransactionId(fiscalCode, transactionId);

        if (CollectionUtils.isEmpty(listOfViewUser)) {
            throw new AppException(AppError.VIEW_USER_NOT_FOUND_WITH_TRANSACTION_ID, fiscalCode, transactionId);
        }

        // PAGOPA-1831: set hidden to true for all transactions with the same transactionId for the given fiscalCode
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
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(name).build().toString())
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


}
