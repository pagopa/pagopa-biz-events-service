package it.gov.pagopa.bizeventsservice.controller.impl;

import it.gov.pagopa.bizeventsservice.client.IReceiptPDFClient;
import it.gov.pagopa.bizeventsservice.controller.ITransactionController;
import it.gov.pagopa.bizeventsservice.entity.BizEvent;
import it.gov.pagopa.bizeventsservice.exception.AppException;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListItem;
import it.gov.pagopa.bizeventsservice.model.response.AttachmentsDetailsResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionDetailResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListResponse;
import it.gov.pagopa.bizeventsservice.service.IBizEventsService;
import it.gov.pagopa.bizeventsservice.service.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
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
    public ResponseEntity<File> getPDFReceipt(@NotBlank String fiscalCode, @NotBlank String eventId) {
    	
    	ResponseEntity<?> notFound = new ResponseEntity<>(HttpStatus.NOT_FOUND);
    	
    	try {
    		BizEvent bizEvent = bizEventsService.getBizEvent(eventId);
    		if (bizEvent.getIdPaymentManager() != null) {
    			// call the receipt-pdf-service to retrieve the PDF receipt
    			AttachmentsDetailsResponse response = receiptClient.getAttachments(fiscalCode, eventId);
    			String url = response.getAttachments().get(0).getUrl();

    			return new ResponseEntity<>(
    					receiptClient.getReceipt(fiscalCode, eventId, url),
    					HttpStatus.OK);
    		} 
    		
    	} catch (AppException e) {
    		if (e.getHttpStatus().equals(HttpStatus.NOT_FOUND)) {
    			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	return (ResponseEntity<File>)notFound;
    }
}
