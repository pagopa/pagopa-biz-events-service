package it.gov.pagopa.bizeventsservice.controller;

import javax.validation.constraints.NotBlank;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.gov.pagopa.bizeventsservice.entity.BizEvent;
import it.gov.pagopa.bizeventsservice.model.ProblemJson;

@Tag(name = "get BizEvent APIs")
@RequestMapping
@Validated
public interface IBizEventController {

        @Operation(summary = "Retrieve the biz-event given its id.", security = {
                        @SecurityRequirement(name = "ApiKey") }, operationId = "getBizEvent")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Obtained biz-event.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(name = "BizEvent", implementation = BizEvent.class))),
                        @ApiResponse(responseCode = "401", description = "Wrong or missing function key.", content = @Content(schema = @Schema())),
                        @ApiResponse(responseCode = "404", description = "Not found the biz-event.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
                        @ApiResponse(responseCode = "422", description = "Unable to process the request.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
                        @ApiResponse(responseCode = "429", description = "Too many requests.", content = @Content(schema = @Schema())),
                        @ApiResponse(responseCode = "500", description = "Service unavailable.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class))) })
        @GetMapping(value = "/events/{biz-event-id}", produces = MediaType.APPLICATION_JSON_VALUE)
        ResponseEntity<BizEvent> getBizEvent(
                        @Parameter(description = "The id of the biz-event.", required = true) @NotBlank @PathVariable("biz-event-id") String bizEventId);

        @Operation(summary = "Retrieve the biz-event given the organization fiscal code and IUV.", security = {
                        @SecurityRequirement(name = "ApiKey") }, operationId = "getBizEventByOrganizationFiscalCodeAndIuv")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Obtained biz-event.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(name = "BizEvent", implementation = BizEvent.class))),
                        @ApiResponse(responseCode = "401", description = "Wrong or missing function key.", content = @Content(schema = @Schema())),
                        @ApiResponse(responseCode = "404", description = "Not found the biz-event.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
                        @ApiResponse(responseCode = "422", description = "Unable to process the request.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
                        @ApiResponse(responseCode = "429", description = "Too many requests.", content = @Content(schema = @Schema())),
                        @ApiResponse(responseCode = "500", description = "Service unavailable.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class))) })
        @GetMapping(value = "/events/organizations/{organization-fiscal-code}/iuvs/{iuv}", produces = MediaType.APPLICATION_JSON_VALUE)
        ResponseEntity<BizEvent> getBizEventByOrganizationFiscalCodeAndIuv(
                        @Parameter(description = "The fiscal code of the Organization.", required = true) @NotBlank @PathVariable("organization-fiscal-code") String organizationFiscalCode,
                        @Parameter(description = "The unique payment identification. Alphanumeric code that uniquely associates and identifies three key elements of a payment: reason, payer, amount", required = true) @NotBlank @PathVariable("iuv") String iuv);
}
