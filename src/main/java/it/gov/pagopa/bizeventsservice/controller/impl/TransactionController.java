package it.gov.pagopa.bizeventsservice.controller.impl;

import it.gov.pagopa.bizeventsservice.controller.ITransactionController;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListItem;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionDetailResponse;
import it.gov.pagopa.bizeventsservice.service.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implementation of {@link ITransactionController} that contains the Rest Controller
 * for transaction services
 */
@RestController
public class TransactionController implements ITransactionController {

    private final ITransactionService transactionService;

    @Autowired
    public TransactionController(ITransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public ResponseEntity<List<TransactionListItem>> getTransactionList(
            String fiscalCode, String continuationToken, Integer size) {
        return new ResponseEntity<>(
                transactionService.getTransactionList(fiscalCode, continuationToken, size),
                HttpStatus.OK);
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
}
