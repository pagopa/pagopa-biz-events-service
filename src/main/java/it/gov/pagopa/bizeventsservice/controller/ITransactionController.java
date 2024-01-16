package it.gov.pagopa.bizeventsservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Payment Receipts REST APIs")
@RequestMapping("/transactions")
@Validated
public interface ITransactionController {

}
