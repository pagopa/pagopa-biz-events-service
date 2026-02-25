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
import it.gov.pagopa.bizeventsservice.config.openapi.OpenApiScope;
import it.gov.pagopa.bizeventsservice.config.openapi.VisibleOnlyFor;
import it.gov.pagopa.bizeventsservice.model.ProblemJson;
import it.gov.pagopa.bizeventsservice.model.filterandorder.Order;
import it.gov.pagopa.bizeventsservice.model.response.paidnotice.NoticeDetailResponse;
import it.gov.pagopa.bizeventsservice.model.response.paidnotice.NoticeListWrapResponse;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import static it.gov.pagopa.bizeventsservice.util.Constants.*;


@Tag(name = "Paid Notice REST APIs")
@RequestMapping("/paids")
@Validated
public interface IPaidNoticeController {

    /**
     * @param fiscalCode
     * @param eventId
     * @return the paid notice detail
     */
    @Operation(summary = "Retrieve the paid notice details given its id.", security = {
            @SecurityRequirement(name = "ApiKey")}, operationId = "getPaidNoticeDetail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obtained paid notice detail.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(name = "NoticeDetailResponse", implementation = NoticeDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "401", description = "Wrong or missing function key.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Not found the transaction.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Service unavailable.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))})
    @GetMapping(value = "/{event-id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<NoticeDetailResponse> getPaidNoticeDetail(
            @RequestHeader("x-fiscal-code") @NotBlank String fiscalCode,
            @Parameter(description = "The id of the paid event.", required = true) @NotBlank @PathVariable("event-id") String eventId);

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
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "401", description = "Wrong or missing function key.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Not found the fiscal code.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Service unavailable.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))})
    @Operation(summary = "Retrieve the paged transaction list from biz events.", description = "This operation is deprecated. Use Paid Notice APIs instead", security = {
            @SecurityRequirement(name = "ApiKey")})
    ResponseEntity<NoticeListWrapResponse> getPaidNotices(
            @RequestHeader(name = X_FISCAL_CODE) String fiscalCode,
            @RequestHeader(name = X_CONTINUATION_TOKEN, required = false) String continuationToken,
            @RequestParam(name = "size", required = false, defaultValue = "10") Integer size,
            @Valid @Parameter(description = "Filter by payer") @RequestParam(value = "is_payer", required = false) Boolean isPayer,
            @Valid @Parameter(description = "Filter by debtor") @RequestParam(value = "is_debtor", required = false) Boolean isDebtor,
            @VisibleOnlyFor(OpenApiScope.HELPDESK) @RequestParam(required = false, defaultValue = "false", name = "hidden") @Parameter(description = "Filter notices by hidden property") Boolean hidden,
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
    @VisibleOnlyFor(OpenApiScope.HELPDESK)
    ResponseEntity<Void> enablePaidNotice(
            @RequestHeader(X_FISCAL_CODE) @NotBlank String fiscalCode,
            @Parameter(description = "The id of the paid event.", required = true) @NotBlank @PathVariable("event-id") String eventId);

    @Operation(summary = "Retrieve the PDF receipt given event id.", security = {
            @SecurityRequirement(name = "ApiKey")}, operationId = "generatePDF")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obtained the PDF receipt.",
                    headers = {@Header(name = HttpHeaders.CONTENT_DISPOSITION, description = "Content disposition with name of the file", schema = @Schema(type = "string"))},
                    content = {@Content(mediaType = MediaType.APPLICATION_PDF_VALUE, schema = @Schema(implementation = Resource.class)),}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "401", description = "Wrong or missing function key.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "404", description = "Not found the receipt.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "422", description = "Unprocessable receipt.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Service unavailable.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))})
    @GetMapping(value = "/{event-id}/pdf", produces = {MediaType.APPLICATION_PDF_VALUE, MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity<Resource> generatePDF(
            @RequestHeader(X_FISCAL_CODE) @NotBlank String fiscalCode,
            @Parameter(description = "The id of the paid event.", required = true) @NotBlank @PathVariable("event-id") String eventId);

}
