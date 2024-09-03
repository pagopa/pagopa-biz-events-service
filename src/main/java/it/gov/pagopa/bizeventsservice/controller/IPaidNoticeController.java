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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;


@Tag(name = "Paid Notice REST APIs")
@RequestMapping("/paids")
@Validated
public interface IPaidNoticeController {
    String X_FISCAL_CODE = "x-fiscal-code";

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
