package it.gov.pagopa.bizeventsservice.controller.impl;

import it.gov.pagopa.bizeventsservice.client.IReceiptGeneratePDFClient;
import it.gov.pagopa.bizeventsservice.client.IReceiptGetPDFClient;
import it.gov.pagopa.bizeventsservice.controller.ITransactionController;
import it.gov.pagopa.bizeventsservice.exception.AppException;
import it.gov.pagopa.bizeventsservice.model.response.AttachmentsDetailsResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionDetailResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListWrapResponse;
import it.gov.pagopa.bizeventsservice.service.IBizEventsService;
import it.gov.pagopa.bizeventsservice.service.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import feign.FeignException;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

/**
 * Implementation of {@link ITransactionController} that contains the Rest Controller
 * for transaction services
 */
@RestController
public class TransactionController implements ITransactionController {

    private final ITransactionService transactionService;
    private final IBizEventsService bizEventsService;
    private final IReceiptGetPDFClient receiptClient;
    private final IReceiptGeneratePDFClient generateReceiptClient;

    @Autowired
    public TransactionController(ITransactionService transactionService, IBizEventsService bizEventsService, 
    		IReceiptGetPDFClient receiptClient, IReceiptGeneratePDFClient generateReceiptClient) {
        this.transactionService = transactionService;
        this.bizEventsService = bizEventsService;
        this.receiptClient = receiptClient;
        this.generateReceiptClient = generateReceiptClient;
    }

    @Override
    public ResponseEntity<TransactionListWrapResponse> getTransactionList(
            String fiscalCode, String continuationToken, Integer size) {
        TransactionListResponse transactionListResponse = transactionService.getTransactionList(fiscalCode, continuationToken, size);

        return ResponseEntity.ok()
                .header(X_CONTINUATION_TOKEN, transactionListResponse.getContinuationToken())
                .body(TransactionListWrapResponse.builder().transactions(transactionListResponse.getTransactionList()).build());
    }
    
    @Override
	public ResponseEntity<TransactionListWrapResponse> getCachedTransactionList(String fiscalCode,
			@Valid @Min(0) Integer page, Integer size) {
		// TODO Auto-generated method stub
		return null;
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
    	// to check if is an OLD event present only on the PM --> the receipt is not available for events present exclusively on the PM
		bizEventsService.getBizEvent(eventId);
		String url;
    	try {
    		// call the receipt-pdf-service to retrieve the PDF receipt details
    		AttachmentsDetailsResponse response = receiptClient.getAttachments(fiscalCode, eventId);
    		url = response.getAttachments().get(0).getUrl();
    	} catch (FeignException.NotFound e) {
    		throw new AppException(HttpStatus.NOT_FOUND, "Receipt Not Found", e.getMessage());
    	}
    	return this.getAttachment(fiscalCode, eventId, url);
    }

    private ResponseEntity<byte[]> getAttachment(String fiscalCode, String eventId, String url) {
    	byte[] receiptFile = {};
    	try {
    		// call the receipt-pdf-service to retrieve the PDF receipt attachment
    		receiptFile = receiptClient.getReceipt(fiscalCode, eventId, url);
    		return ResponseEntity
    				.ok()
    				.contentLength(receiptFile.length)
    				.contentType(MediaType.APPLICATION_PDF)
    				.header("content-disposition", "filename=receipt")
    				.body(receiptFile);
    	} catch (FeignException.NotFound e) {
    		// re-generate the PDF receipt and return the generated file by getReceipt call
    		generateReceiptClient.generateReceipt(eventId, "false", "{}");
    		receiptFile = receiptClient.getReceipt(fiscalCode, eventId, url);
    		return ResponseEntity
    				.ok()
    				.contentLength(receiptFile.length)
    				.contentType(MediaType.APPLICATION_PDF)
    				.header("content-disposition", "filename=receipt")
    				.body(receiptFile);
    	}
    }
}
