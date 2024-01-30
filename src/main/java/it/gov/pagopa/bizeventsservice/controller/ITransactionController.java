package it.gov.pagopa.bizeventsservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.gov.pagopa.bizeventsservice.model.ProblemJson;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionDetailResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListItem;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Tag(name = "IO Transactions REST APIs")
@RequestMapping("/transactions")
@Validated
public interface ITransactionController {

    /**
     * recovers biz-event data for the transaction list
     *
     * @param fiscalCode        tokenized user fiscal code
     * @param continuationToken continuation token for paginated query
     * @param size              optional parameter defining page size, defaults to 5
     * @return the transaction list
     */
    @GetMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obtained transaction list.",
                    headers = @Header(name = "x-continuation-token", description = "continuation token for paginated query", schema = @Schema(implementation = String.class)),
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(name = "TransactionListItem", implementation = List.class))),
            @ApiResponse(responseCode = "401", description = "Wrong or missing function key.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Not found the transaction.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "422", description = "Unable to process the request.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Service unavailable.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))})
    @Operation(summary = "Retrieve the paged transaction list from biz events.", security = {
            @SecurityRequirement(name = "ApiKey")}, operationId = "getTransactionList")
    ResponseEntity<List<TransactionListItem>> getTransactionList(
            @RequestHeader(name = "x-fiscal-code") String fiscalCode,
            @RequestHeader(name = "x-continuation-token", required = false) String continuationToken,
            @RequestHeader(name = "x-page-size", required = false, defaultValue = "5") Integer size
    );

    @Operation(summary = "Retrieve the transaction details given its id.", security = {
            @SecurityRequirement(name = "ApiKey")}, operationId = "getTransactionDetails")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obtained transaction details.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(name = "TransactionDetailResponse", implementation = TransactionDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "Wrong or missing function key.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Not found the transaction.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "422", description = "Unable to process the request.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Service unavailable.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))})
    @GetMapping(value = "/{transaction-id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<TransactionDetailResponse> getTransactionDetails(
            @RequestHeader("x-fiscal-code") @NotBlank String fiscalCode,
            @Parameter(description = "The id of the transaction.", required = true) @NotBlank @PathVariable("transaction-id") String transactionId);

    @Operation(summary = "Disable the transaction details given its id.", security = {
            @SecurityRequirement(name = "ApiKey")}, operationId = "disableTransaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Disabled Transactions.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description = "Wrong or missing function key.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Not found the transaction.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "422", description = "Unable to process the request.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Service unavailable.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))})
    @PostMapping(value = "/{transaction-id}/disable", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity disableTransaction(
            @RequestHeader("x-fiscal-code") @NotBlank String fiscalCode,
            @Parameter(description = "The id of the transaction.", required = true) @NotBlank @PathVariable("transaction-id") String transactionId);

}
