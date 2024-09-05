package it.gov.pagopa.bizeventsservice.controller.impl;

import it.gov.pagopa.bizeventsservice.controller.IPaidNoticeController;
import it.gov.pagopa.bizeventsservice.service.IBizEventsService;
import it.gov.pagopa.bizeventsservice.service.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;

/**
 * Implementation of {@link IPaidNoticeController} that contains the Rest Controller
 * for events services
 */
@RestController
public class PaidNoticeController implements IPaidNoticeController {

    private final ITransactionService transactionService;
    private final IBizEventsService bizEventsService;

    @Autowired
    public PaidNoticeController(ITransactionService transactionService, IBizEventsService bizEventsService) {
        this.transactionService = transactionService;
        this.bizEventsService = bizEventsService;
    }


    @Override
    public ResponseEntity<Void> disablePaidNotice(String fiscalCode, String transactionId) {
        transactionService.disableTransaction(fiscalCode, transactionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Resource> generatePDF(@NotBlank String fiscalCode, @NotBlank String eventId) {
        // to check if is an OLD event present only on the PM --> the receipt is not available for events present exclusively on the PM
        bizEventsService.getBizEvent(eventId);
        return transactionService.getPDFReceiptResponse(fiscalCode, eventId);
    }

}
