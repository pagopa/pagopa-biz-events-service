package it.gov.pagopa.bizeventsservice.controller.impl;

import it.gov.pagopa.bizeventsservice.controller.ITransactionController;
import it.gov.pagopa.bizeventsservice.model.transaction.TransactionDetailResponse;
import it.gov.pagopa.bizeventsservice.service.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransactionController implements ITransactionController {


    private ITransactionService transactionService;

    @Autowired
    public TransactionController(ITransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public ResponseEntity<TransactionDetailResponse> getTransactionDetails(String fiscalCode, boolean isCart, String transactionId) {
        return null;
    }
}
