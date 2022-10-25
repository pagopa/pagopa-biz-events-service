package it.gov.pagopa.bizeventsservice.controller.impl;

import javax.validation.constraints.NotBlank;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import it.gov.pagopa.bizeventsservice.controller.IPaymentsController;
import it.gov.pagopa.bizeventsservice.model.response.CtReceiptModelResponse;

@RestController
public class PaymentsController implements IPaymentsController {


    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ResponseEntity<CtReceiptModelResponse> getOrganizationReceipt(@NotBlank String organizationFiscalCode,
                                                                         @NotBlank String iur) {
        return null;
    }

}
