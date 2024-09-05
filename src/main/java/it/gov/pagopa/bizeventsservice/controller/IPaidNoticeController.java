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
import it.gov.pagopa.bizeventsservice.model.response.paidnotice.NoticeListWrapResponse;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;


@Tag(name = "Paid Notice REST APIs")
@RequestMapping("/paids")
@Validated
public interface IPaidNoticeController {
    String X_FISCAL_CODE = "x-fiscal-code";
    String X_CONTINUATION_TOKEN = "x-continuation-token";
    String PAGE_SIZE = "size";

    /**
     * recovers biz-event data for the paid notices list
     *
     * @param fiscalCode        tokenized user fiscal code
     * @param continuationToken continuation token for paginated query
     * @param size              optional parameter defining page size, defaults to 10
     * @return the paid notices list
     */
    @GetMapping
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obtained paid notices list.",
                    headers = @Header(name = X_CONTINUATION_TOKEN, description = "continuation token for paginated query", schema = @Schema(type = "string")),
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NoticeListWrapResponse.class))),
            @ApiResponse(responseCode = "401", description = "Wrong or missing function key.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Not found the fiscal code.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Service unavailable.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))})
    @Operation(summary = "Retrieve the paged transaction list from biz events.", description = "This operation is deprecated. Use Paid Notice APIs instead", security = {
            @SecurityRequirement(name = "ApiKey")})
    ResponseEntity<NoticeListWrapResponse> getPaidNotices(
            @RequestHeader(name = X_FISCAL_CODE) String fiscalCode,
            @RequestHeader(name = X_CONTINUATION_TOKEN, required = false) String continuationToken,
            @RequestParam(name = PAGE_SIZE, required = false, defaultValue = "10") Integer size,
            @Valid @Parameter(description = "Filter by payer") @RequestParam(value = "is_payer", required = false) Boolean isPayer,
            @Valid @Parameter(description = "Filter by debtor") @RequestParam(value = "is_debtor", required = false) Boolean isDebtor,
            @RequestParam(required = false, name = "orderby", defaultValue = "TRANSACTION_DATE") @Parameter(description = "Order by TRANSACTION_DATE") Order.TransactionListOrder orderBy,
            @RequestParam(required = false, name = "ordering", defaultValue = "DESC") @Parameter(description = "Direction of ordering") Sort.Direction ordering);




    @Operation(summary = "Disable the paid notice details given its id.", security = {
            @SecurityRequirement(name = "ApiKey")}, operationId = "disablePaidNotice")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event Disabled.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description = "Wrong or missing function key.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Not found the paid event.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Service unavailable.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))})
    @PostMapping(value = "/{event-id}/disable", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> disablePaidNotice(
            @RequestHeader(X_FISCAL_CODE) @NotBlank String fiscalCode,
            @Parameter(description = "The id of the paid event.", required = true) @NotBlank @PathVariable("event-id") String eventId);


}
