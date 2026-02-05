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
import it.gov.pagopa.bizeventsservice.entity.BizEvent;
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

import static it.gov.pagopa.bizeventsservice.util.Constants.PAGE_SIZE;
import static it.gov.pagopa.bizeventsservice.util.Constants.X_CONTINUATION_TOKEN;
import static it.gov.pagopa.bizeventsservice.util.Constants.X_FISCAL_CODE;

@Tag(name = "Biz-Events Helpdesk")
@RequestMapping("/events")
@Validated
public interface IBizEventController {

    @Operation(summary = "Retrieve the biz-event given its id.", security = {
            @SecurityRequirement(name = "ApiKey")}, operationId = "getBizEvent")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obtained biz-event.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(name = "BizEvent", implementation = BizEvent.class))),
            @ApiResponse(responseCode = "401", description = "Wrong or missing function key.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Not found the biz-event.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "422", description = "Unable to process the request.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Service unavailable.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))})
    @GetMapping(value = "/{biz-event-id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<BizEvent> getBizEvent(
            @Parameter(description = "The id of the biz-event.", required = true) @NotBlank @PathVariable("biz-event-id") String bizEventId);

    @Operation(summary = "Retrieve the biz-event given the organization fiscal code and IUV.", security = {
            @SecurityRequirement(name = "ApiKey")}, operationId = "getBizEventByOrganizationFiscalCodeAndIuv")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obtained biz-event.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(name = "BizEvent", implementation = BizEvent.class))),
            @ApiResponse(responseCode = "401", description = "Wrong or missing function key.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Not found the biz-event.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "422", description = "Unable to process the request.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Service unavailable.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))})
    @GetMapping(value = "/organizations/{organization-fiscal-code}/iuvs/{iuv}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<BizEvent> getBizEventByOrganizationFiscalCodeAndIuv(
            @Parameter(description = "The fiscal code of the Organization.", required = true) @NotBlank @PathVariable("organization-fiscal-code") String organizationFiscalCode,
            @Parameter(description = "The unique payment identification. Alphanumeric code that uniquely associates and identifies three key elements of a payment: reason, payer, amount", required = true) @NotBlank @PathVariable("iuv") String iuv);

    /**
     * recovers biz-event data for the paid notices list
     *
     * @param fiscalCode        tokenized user fiscal code
     * @param continuationToken continuation token for paginated query
     * @param size              optional parameter defining page size, defaults to 10
     * @return the paid notices list
     */
    @GetMapping("/paids")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obtained paid notices list.",
                    headers = @Header(name = X_CONTINUATION_TOKEN, description = "continuation token for paginated query", schema = @Schema(type = "string")),
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = NoticeListWrapResponse.class))),
            @ApiResponse(responseCode = "401", description = "Wrong or missing function key.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Not found the fiscal code.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Service unavailable.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))})
    @Operation(summary = "Retrieve the paged transaction list from biz events.", description = "Retrieve the list of paid notices", security = {
            @SecurityRequirement(name = "ApiKey")})
    ResponseEntity<NoticeListWrapResponse> getPaidNoticesWithHiddenParam(
            @RequestHeader(name = X_FISCAL_CODE) String fiscalCode,
            @RequestHeader(name = X_CONTINUATION_TOKEN, required = false) String continuationToken,
            @RequestParam(name = PAGE_SIZE, required = false, defaultValue = "10") Integer size,
            @Valid @Parameter(description = "Filter by payer") @RequestParam(value = "is_payer", required = false) Boolean isPayer,
            @Valid @Parameter(description = "Filter by debtor") @RequestParam(value = "is_debtor", required = false) Boolean isDebtor,
            @RequestParam(required = false, defaultValue = "false") @Parameter(description = "Filter notices by hidden property") Boolean hidden,
            @RequestParam(required = false, name = "orderby", defaultValue = "TRANSACTION_DATE") @Parameter(description = "Order by TRANSACTION_DATE") Order.TransactionListOrder orderBy,
            @RequestParam(required = false, name = "ordering", defaultValue = "DESC") @Parameter(description = "Direction of ordering") Sort.Direction ordering);

    @Operation(summary = "Enable the paid notice details given its id.", security = {
            @SecurityRequirement(name = "ApiKey")}, operationId = "enablePaidNotice")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event enabled.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description = "Wrong or missing function key.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Not found the paid event.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Service unavailable.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))})
    @PostMapping(value = "/{event-id}/enable", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> enablePaidNotice(
            @RequestHeader(X_FISCAL_CODE) @NotBlank String fiscalCode,
            @Parameter(description = "The id of the paid event.", required = true) @NotBlank @PathVariable("event-id") String eventId);
}
