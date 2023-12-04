package it.gov.pagopa.bizeventsservice.controller.impl;

import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import it.gov.pagopa.bizeventsservice.controller.IPaymentsController;
import it.gov.pagopa.bizeventsservice.model.response.CtReceiptModelResponse;
import it.gov.pagopa.bizeventsservice.service.IBizEventsService;

@RestController
public class PaymentsController implements IPaymentsController {
	
	@Autowired 
	private IBizEventsService bizEventsService;

    @Override
    public ResponseEntity<CtReceiptModelResponse> getOrganizationReceipt(@NotBlank String organizationFiscalCode,
                                                                         @NotBlank String iur,
                                                                         @NotBlank String iuv) {
    	return new ResponseEntity<>(
    			bizEventsService.getOrganizationReceipt(organizationFiscalCode, iur, iuv),
                HttpStatus.OK);
    }

}
