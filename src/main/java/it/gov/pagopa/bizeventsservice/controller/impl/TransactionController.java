package it.gov.pagopa.bizeventsservice.controller.impl;

import it.gov.pagopa.bizeventsservice.client.IReceiptGeneratePDFClient;
import it.gov.pagopa.bizeventsservice.client.IReceiptGetPDFClient;
import it.gov.pagopa.bizeventsservice.controller.ITransactionController;
import it.gov.pagopa.bizeventsservice.entity.BizEvent;
import it.gov.pagopa.bizeventsservice.model.filterandorder.Order.TransactionListOrder;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionDetailResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListWrapResponse;
import it.gov.pagopa.bizeventsservice.service.IBizEventsService;
import it.gov.pagopa.bizeventsservice.service.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;

/**
 * Implementation of {@link ITransactionController} that contains the Rest Controller
 * for transaction services
 */
@RestController
public class TransactionController implements ITransactionController {

    private final ITransactionService transactionService;
    private final IBizEventsService bizEventsService;

    @Autowired
    public TransactionController(ITransactionService transactionService, IBizEventsService bizEventsService,
                                 IReceiptGetPDFClient receiptClient, IReceiptGeneratePDFClient generateReceiptClient) {
        this.transactionService = transactionService;
        this.bizEventsService = bizEventsService;
    }


    @Override
    public ResponseEntity<TransactionListWrapResponse> getTransactionList(String fiscalCode, Boolean isPayer, Boolean isDebtor,
                                                                          String continuationToken, Integer size, TransactionListOrder orderBy, Direction ordering) {
        TransactionListResponse transactionListResponse = transactionService.getTransactionList(fiscalCode, isPayer, isDebtor,
                continuationToken, size, orderBy, ordering);

        return ResponseEntity.ok()
                .header(X_CONTINUATION_TOKEN, transactionListResponse.getContinuationToken())
                .body(TransactionListWrapResponse.builder().transactions(transactionListResponse.getTransactionList()).build());
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
        // to check if is an OLD event present only on the PM --> the receipt is not available for events present exclusively on the PM
        BizEvent bizEvent = bizEventsService.getBizEvent(eventId);
        byte[] receiptFile = transactionService.getPDFReceipt(fiscalCode, bizEvent);
        return ResponseEntity
                .ok()
                .contentLength(receiptFile.length)
                .contentType(MediaType.APPLICATION_PDF)
                .header("content-disposition", "filename=receipt")
                .body(receiptFile);
    }


}
