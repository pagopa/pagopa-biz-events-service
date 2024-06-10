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
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListWrapResponse;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotBlank;


@Tag(name = "IO Transactions REST APIs")
@RequestMapping("/transactions")
@Validated
public interface ITransactionController {

    String X_CONTINUATION_TOKEN = "x-continuation-token";
    String X_FISCAL_CODE = "x-fiscal-code";
    String PAGE_SIZE = "size";
    String PAGE_NUMBER = "page";

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
                    headers = @Header(name = X_CONTINUATION_TOKEN, description = "continuation token for paginated query", schema = @Schema(type="string")),
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(name = "TransactionListWrapResponse", implementation = TransactionListWrapResponse.class))),
            @ApiResponse(responseCode = "401", description = "Wrong or missing function key.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Not found the transaction.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Service unavailable.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))})
    @Operation(summary = "Retrieve the paged transaction list from biz events.", security = {
            @SecurityRequirement(name = "ApiKey")}, operationId = "getTransactionList")
    ResponseEntity<TransactionListWrapResponse> getTransactionList(
            @RequestHeader(name = X_FISCAL_CODE) String fiscalCode,
            @RequestHeader(name = X_CONTINUATION_TOKEN, required = false) String continuationToken,
            @RequestParam(name = PAGE_SIZE, required = false, defaultValue = "10") Integer size

    );
    
    /**
     * recovers biz-event data for the transaction list
     *
     * @param fiscalCode        tokenized user fiscal code
     * @param page              optional parameter defining page number, default to 0 (first page)
     * @param size              optional parameter defining page size, defaults to 10
     * @return the transaction list
     */
    @GetMapping(value = "/cached", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obtained transaction list.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(name = "TransactionListWrapResponse", implementation = TransactionListWrapResponse.class))),
            @ApiResponse(responseCode = "401", description = "Wrong or missing function key.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Not found the transaction.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Service unavailable.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))})
    @Operation(summary = "Retrieve the paged transaction list from biz events.", security = {
            @SecurityRequirement(name = "ApiKey")}, operationId = "getTransactionList")
    ResponseEntity<TransactionListWrapResponse> getCachedTransactionList(
            @RequestHeader(name = X_FISCAL_CODE) String fiscalCode,
            @RequestParam(name = PAGE_NUMBER, required = false, defaultValue = "0") Integer page,
            @RequestParam(name = PAGE_SIZE, required = false, defaultValue = "10") Integer size
    );

    @Operation(summary = "Retrieve the transaction details given its id.", security = {
            @SecurityRequirement(name = "ApiKey")}, operationId = "getTransactionDetails")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obtained transaction details.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(name = "TransactionDetailResponse", implementation = TransactionDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "Wrong or missing function key.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Not found the transaction.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
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
            @ApiResponse(responseCode = "429", description = "Too many requests.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Service unavailable.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))})
    @PostMapping(value = "/{transaction-id}/disable", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> disableTransaction(
            @RequestHeader("x-fiscal-code") @NotBlank String fiscalCode,
            @Parameter(description = "The id of the transaction.", required = true) @NotBlank @PathVariable("transaction-id") String transactionId);
    
    @Operation(summary = "Retrieve the PDF receipt given event id.", security = {
            @SecurityRequirement(name = "ApiKey")}, operationId = "getPDFReceipt")
    @ApiResponses(value = {
    		@ApiResponse(responseCode = "200", description = "Obtained the PDF receipt.", content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE, schema = @Schema( type = "string", format = "binary"))),
            @ApiResponse(responseCode = "401", description = "Wrong or missing function key.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Not found the receipt.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "422", description = "Unprocessable receipt.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Service unavailable.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))})
    @GetMapping(value = "/{event-id}/pdf")
    ResponseEntity<byte[]> getPDFReceipt(
    		@RequestHeader("x-fiscal-code") @NotBlank String fiscalCode,
            @Parameter(description = "The id of the event.", required = true) @NotBlank @PathVariable("event-id") String eventId);

}
