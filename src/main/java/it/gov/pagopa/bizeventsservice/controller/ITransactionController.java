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
import it.gov.pagopa.bizeventsservice.model.filterandorder.Order;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionDetailResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListWrapResponse;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import static it.gov.pagopa.bizeventsservice.util.Constants.X_FISCAL_CODE;


@Tag(name = "IO Transactions REST APIs")
@RequestMapping("/transactions")
@Validated
public interface ITransactionController {

    String X_CONTINUATION_TOKEN = "x-continuation-token";
    String PAGE_SIZE = "size";
    String PAGE_NUMBER = "page";

    /**
     * @param fiscalCode        tokenized user fiscal code
     * @param isPayer           optional flag defining the filter to select only the notices where the user is the payer
     * @param isDebtor          optional flag defining the filter to select only the notices where the user is the debtor
     * @param continuationToken continuation token for paginated query
     * @param size              optional parameter defining page size, defaults to 10
     * @param orderBy           optional parameter defining the sort field for the returned list, defaults to TRANSACTION_DATE
     * @param ordering          optional parameter defining the sorting direction of the returned list, defaults to DESC
     * @return the transaction list
     * @deprecated recovers biz-event data for the transaction list
     */
    @GetMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obtained transaction list.",
                    headers = @Header(name = X_CONTINUATION_TOKEN, description = "continuation token for paginated query", schema = @Schema(type = "string")),
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(name = "TransactionListWrapResponse", implementation = TransactionListWrapResponse.class))),
            @ApiResponse(responseCode = "401", description = "Wrong or missing function key.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Not found the transaction.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Service unavailable.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))})
    @Operation(summary = "Retrieve the paged transaction list from biz events.", description = "This operation is deprecated. Use Paid Notice APIs instead", security = {
            @SecurityRequirement(name = "ApiKey")}, deprecated = true, operationId = "getTransactionList")
    @Deprecated(forRemoval = false)
    ResponseEntity<TransactionListWrapResponse> getTransactionList(
            @RequestHeader(name = X_FISCAL_CODE) String fiscalCode,
            @Valid @Parameter(description = "Filter by payer") @RequestParam(value = "is_payer", required = false) Boolean isPayer,
            @Valid @Parameter(description = "Filter by debtor") @RequestParam(value = "is_debtor", required = false) Boolean isDebtor,
            @RequestHeader(name = X_CONTINUATION_TOKEN, required = false) String continuationToken,
            @RequestParam(name = PAGE_SIZE, required = false, defaultValue = "10") Integer size,
            @RequestParam(required = false, name = "orderby", defaultValue = "TRANSACTION_DATE") @Parameter(description = "Order by TRANSACTION_DATE") Order.TransactionListOrder orderBy,
            @RequestParam(required = false, name = "ordering", defaultValue = "DESC") @Parameter(description = "Direction of ordering") Sort.Direction ordering);


    /**
     * @param fiscalCode
     * @param transactionId
     * @return the transaction details
     * @deprecated
     */
    @Operation(summary = "Retrieve the transaction details given its id.", description = "This operation is deprecated. Use Paid Notice APIs instead", deprecated = true, security = {
            @SecurityRequirement(name = "ApiKey")}, operationId = "getTransactionDetails")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obtained transaction details.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(name = "TransactionDetailResponse", implementation = TransactionDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "Wrong or missing function key.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Not found the transaction.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Service unavailable.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))})
    @GetMapping(value = "/{transaction-id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Deprecated(forRemoval = false)
    ResponseEntity<TransactionDetailResponse> getTransactionDetails(
            @RequestHeader("x-fiscal-code") @NotBlank String fiscalCode,
            @Parameter(description = "The id of the transaction.", required = true) @NotBlank @PathVariable("transaction-id") String transactionId);

    /**
     * @param fiscalCode
     * @param transactionId
     * @return
     * @deprecated
     */
    @Operation(summary = "Disable the transaction details given its id.", description = "This operation is deprecated. Use Paid Notice APIs instead", deprecated = true, security = {
            @SecurityRequirement(name = "ApiKey")}, operationId = "disableTransaction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Disabled Transactions.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description = "Wrong or missing function key.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Not found the transaction.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Service unavailable.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))})
    @PostMapping(value = "/{transaction-id}/disable", produces = MediaType.APPLICATION_JSON_VALUE)
    @Deprecated(forRemoval = false)
    ResponseEntity<Void> disableTransaction(
            @RequestHeader("x-fiscal-code") @NotBlank String fiscalCode,
            @Parameter(description = "The id of the transaction.", required = true) @NotBlank @PathVariable("transaction-id") String transactionId);

    /**
     * @deprecated
     * @param fiscalCode
     * @param eventId
     * @return
     */
    @Operation(summary = "Retrieve the PDF receipt given event id.", deprecated = true,
            description = "This operation is deprecated. Use Paid Notice APIs instead", security = {
            @SecurityRequirement(name = "ApiKey")}, operationId = "getPDFReceipt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obtained the PDF receipt.", content = @Content(mediaType = MediaType.APPLICATION_PDF_VALUE, schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "401", description = "Wrong or missing function key.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Not found the receipt.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "422", description = "Unprocessable receipt.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Service unavailable.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))})
    @GetMapping(value = "/{event-id}/pdf")
    @Deprecated(forRemoval = false)
    ResponseEntity<byte[]> getPDFReceipt(
            @RequestHeader("x-fiscal-code") @NotBlank String fiscalCode,
            @Parameter(description = "The id of the event.", required = true) @NotBlank @PathVariable("event-id") String eventId);

}
