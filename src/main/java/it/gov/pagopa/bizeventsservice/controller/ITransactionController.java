package it.gov.pagopa.bizeventsservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.gov.pagopa.bizeventsservice.model.ProblemJson;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListItem;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionDetailResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Payment Receipts REST APIs")
@RequestMapping("/transactions")
@Validated
public interface ITransactionController {

    /**
     * recovers biz-event data for the transaction list
     * @param fiscalCode user fiscal code
     * @param start start offset for paged list
     * @param size optional parameter defining page size, defaults to 5
     * @return
     */
    @GetMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obtained transaction list.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(name = "TransactionListItem", implementation = List.class))),
            @ApiResponse(responseCode = "401", description = "Wrong or missing function key.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Not found the transaction.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "422", description = "Unable to process the request.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Service unavailable.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class))) })
    @Operation(summary = "Retrieve the paged transaction list from biz events.", security = {
            @SecurityRequirement(name = "ApiKey") }, operationId = "getTransactionList")
    ResponseEntity<List<TransactionListItem>> getTransactionList(
            @RequestHeader(name = "x-fiscal-code") String fiscalCode,
            @RequestParam(name = "start") Integer start,
            @RequestParam(name = "size", required = false, defaultValue = "5") Integer size
    );

    @Operation(summary = "Retrieve the transaction details given its id.", security = {
            @SecurityRequirement(name = "ApiKey") }, operationId = "getTransactionDetails")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obtained transaction details.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(name = "TransactionDetailResponse", implementation = TransactionDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "Wrong or missing function key.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Not found the transaction.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "422", description = "Unable to process the request.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Service unavailable.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class))) })
    @GetMapping(value = "/{transaction-id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<TransactionDetailResponse> getTransactionDetails(
            @RequestHeader("x-fiscal-code") @NotBlank String fiscalCode,
            @RequestParam(defaultValue = "false", required = false) boolean isCart,
            @Parameter(description = "The id of the transaction.", required = true) @NotBlank @PathVariable("transaction-id") String transactionId);
}
