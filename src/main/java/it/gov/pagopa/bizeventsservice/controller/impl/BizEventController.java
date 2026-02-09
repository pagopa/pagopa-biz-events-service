package it.gov.pagopa.bizeventsservice.controller.impl;

import it.gov.pagopa.bizeventsservice.controller.IBizEventController;
import it.gov.pagopa.bizeventsservice.entity.BizEvent;
import it.gov.pagopa.bizeventsservice.service.IBizEventsService;
import it.gov.pagopa.bizeventsservice.service.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;

@RestController
public class BizEventController implements IBizEventController {

    private final IBizEventsService bizEventsService;
    private final ITransactionService transactionService;

    @Autowired
    public BizEventController(IBizEventsService bizEventsService, ITransactionService transactionService) {
        this.bizEventsService = bizEventsService;
        this.transactionService = transactionService;
    }

    @Override
    public ResponseEntity<BizEvent> getBizEvent(@NotBlank String bizEventId) {
        return new ResponseEntity<>(bizEventsService.getBizEvent(bizEventId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<BizEvent> getBizEventByOrganizationFiscalCodeAndIuv(
            @NotBlank String organizationFiscalCode, @NotBlank String iuv) {
        return new ResponseEntity<>(bizEventsService.getBizEventByOrgFiscalCodeAndIuv(organizationFiscalCode, iuv),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> enablePaidNotice(String fiscalCode, String transactionId) {
        transactionService.enablePaidNotice(fiscalCode, transactionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
