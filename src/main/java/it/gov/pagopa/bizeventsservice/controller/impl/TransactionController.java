package it.gov.pagopa.bizeventsservice.controller.impl;

import it.gov.pagopa.bizeventsservice.client.IReceiptPDFClient;
import it.gov.pagopa.bizeventsservice.controller.ITransactionController;
import it.gov.pagopa.bizeventsservice.exception.AppException;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListItem;
import it.gov.pagopa.bizeventsservice.model.response.AttachmentsDetailsResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionDetailResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListResponse;
import it.gov.pagopa.bizeventsservice.service.IBizEventsService;
import it.gov.pagopa.bizeventsservice.service.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import feign.FeignException;

import java.util.List;

import javax.validation.constraints.NotBlank;

/**
 * Implementation of {@link ITransactionController} that contains the Rest Controller
 * for transaction services
 */
@RestController
public class TransactionController implements ITransactionController {

    private final ITransactionService transactionService;
    private final IBizEventsService bizEventsService;
    private final IReceiptPDFClient receiptClient;

    @Autowired
    public TransactionController(ITransactionService transactionService, IBizEventsService bizEventsService, IReceiptPDFClient receiptClient) {
        this.transactionService = transactionService;
        this.bizEventsService = bizEventsService;
        this.receiptClient = receiptClient;
    }

    @Override
    public ResponseEntity<List<TransactionListItem>> getTransactionList(
            String fiscalCode, String continuationToken, Integer size) {
        TransactionListResponse transactionListResponse = transactionService.getTransactionList(fiscalCode, continuationToken, size);

        return ResponseEntity.ok()
                .header(X_CONTINUATION_TOKEN, transactionListResponse.getContinuationToken())
                .body(transactionListResponse.getTransactionList());
    }

    @Override
    public ResponseEntity<TransactionDetailResponse> getTransactionDetails(String fiscalCode, String eventReference) {
        return new ResponseEntity<>(
                transactionService.getTransactionDetails(fiscalCode, eventReference),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> disableTransaction(String fiscalCode, String transactionId) {
        transactionService.disableTransaction(fiscalCode, transactionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<byte[]> getPDFReceipt(@NotBlank String fiscalCode, @NotBlank String eventId) {
    	return acquirePDFReceipt(fiscalCode, eventId);
    }

    private ResponseEntity<byte[]> acquirePDFReceipt(String fiscalCode, String eventId) {
    	try {
    		// to check if is an OLD event present only on the PM --> the receipt is not available for events present exclusively on the PM
    		bizEventsService.getBizEvent(eventId);

    		// call the receipt-pdf-service to retrieve the PDF receipt
    		AttachmentsDetailsResponse response = receiptClient.getAttachments(fiscalCode, eventId);
    		String url = response.getAttachments().get(0).getUrl();

    		byte[] receiptFile = receiptClient.getReceipt(fiscalCode, eventId, url);

    		return ResponseEntity
    				.ok()
    				.contentLength(receiptFile.length)
    				.contentType(MediaType.APPLICATION_PDF)
    				.header("content-disposition", "filename=receipt")
    				.body(receiptFile);

    	} catch (FeignException.NotFound e) {
    		// TODO receipt generation
    		throw new AppException(HttpStatus.NOT_FOUND, "Receipt Not Found", "Something was wrong - " + e.getMessage());
    	}
    }
}
