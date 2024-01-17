package it.gov.pagopa.bizeventsservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListItem;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Payment Receipts REST APIs")
@RequestMapping("/transactions")
@Validated
public interface ITransactionController {

    /**
     *
     * @param fiscalCode
     * @param start
     * @param size
     * @return
     */
    @GetMapping
    @Operation(summary = "Retrieve the paged transaction list from biz events.", security = {
            @SecurityRequirement(name = "ApiKey") }, operationId = "getTransactionList")
    ResponseEntity<List<TransactionListItem>> getTransactionList(
            @RequestHeader(name = "x-fiscal-code") String fiscalCode,
            @RequestParam(name = "start") Integer start,
            @RequestParam(name = "size", required = false, defaultValue = "5") Integer size
    );

}
