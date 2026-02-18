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
import it.gov.pagopa.bizeventsservice.model.response.CtReceiptModelResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.constraints.NotBlank;


@Tag(name = "Payment Receipts REST APIs")
@RequestMapping
@Validated
public interface IPaymentsController {

    // TODO: this API is included in the one using the path /organizations/{organizationfiscalcode}/receipts/{iur}, will be removed
    @Deprecated
    /**
     * @deprecated (API to be removed after the next SANP release)
     */
    @Operation(summary = "The organization get the receipt for the creditor institution using IUV and IUR.", security = {@SecurityRequirement(name = "ApiKey")}, operationId = "getOrganizationReceiptIuvIur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obtained receipt.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(name = "CtReceipt", implementation = CtReceiptModelResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "401", description = "Wrong or missing function key.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Not found the receipt.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "422", description = "Unable to process the request.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Service unavailable.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))})
    @GetMapping(value = "/organizations/{organizationfiscalcode}/receipts/{iur}/paymentoptions/{iuv}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CtReceiptModelResponse> getOrganizationReceipt(
            @Parameter(description = "The fiscal code of the Organization.", required = true)
            @NotBlank @PathVariable("organizationfiscalcode") String organizationFiscalCode,
            @Parameter(description = "The unique reference of the operation assigned to the payment (Payment Token).", required = true)
            @NotBlank @PathVariable("iur") String iur,
            @Parameter(description = "The unique payment identification. Alphanumeric code that uniquely associates and identifies three key elements of a payment: reason, payer, amount", required = true)
            @NotBlank @PathVariable("iuv") String iuv);

    @Operation(summary = "The organization get the receipt for the creditor institution using IUR.", security = {@SecurityRequirement(name = "ApiKey")}, operationId = "getOrganizationReceiptIur")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obtained receipt.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(name = "CtReceipt", implementation = CtReceiptModelResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "401", description = "Wrong or missing function key.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Not found the receipt.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "422", description = "Unable to process the request.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Service unavailable.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))})
    @GetMapping(value = "/organizations/{organizationfiscalcode}/receipts/{iur}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CtReceiptModelResponse> getOrganizationReceipt(
            @Parameter(description = "The fiscal code of the Organization.", required = true)
            @NotBlank @PathVariable("organizationfiscalcode") String organizationFiscalCode,
            @Parameter(description = "The unique reference of the operation assigned to the payment (Payment Token).", required = true)
            @NotBlank @PathVariable("iur") String iur);
}
