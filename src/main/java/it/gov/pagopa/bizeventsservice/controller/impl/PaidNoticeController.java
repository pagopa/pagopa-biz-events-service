package it.gov.pagopa.bizeventsservice.controller.impl;

import it.gov.pagopa.bizeventsservice.controller.IPaidNoticeController;
import it.gov.pagopa.bizeventsservice.service.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Implementation of {@link IPaidNoticeController} that contains the Rest Controller
 * for events services
 */
@RestController
public class PaidNoticeController implements IPaidNoticeController {

    private final ITransactionService transactionService;

    @Autowired
    public PaidNoticeController(ITransactionService transactionService) {
        this.transactionService = transactionService;
    }


    @Override
    public ResponseEntity<Void> disablePaidNotice(String fiscalCode, String transactionId) {
        transactionService.disableTransaction(fiscalCode, transactionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
