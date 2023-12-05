package it.gov.pagopa.bizeventsservice.controller.impl;

import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import it.gov.pagopa.bizeventsservice.controller.IBizEventController;
import it.gov.pagopa.bizeventsservice.entity.BizEvent;
import it.gov.pagopa.bizeventsservice.service.IBizEventsService;

@RestController
public class BizEventController implements IBizEventController {

    private final IBizEventsService bizEventsService;

    @Autowired
    public BizEventController(IBizEventsService bizEventsService) {
        this.bizEventsService = bizEventsService;
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
}
