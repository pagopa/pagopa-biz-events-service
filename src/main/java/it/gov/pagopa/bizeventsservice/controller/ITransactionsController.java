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
import it.gov.pagopa.bizeventsservice.model.response.paidnotice.CartItem;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;

import static it.gov.pagopa.bizeventsservice.util.Constants.X_FISCAL_CODE;

@Tag(name = "Transactions REST APIs")
@RequestMapping("/transactions")
@Validated
public interface ITransactionsController {

    /**
     * Retrieve the paid notice details given nav, organization-fiscal-code and debtorFiscalCode
     *
     * @param organizationFiscalCode
     * @param nav
     * @param debtorFiscalCode
     * @return
     */
    @Operation(summary = "Retrieve the paid notice details given nav, organization-fiscal-code and debtorFiscalCode.", security = {
            @SecurityRequirement(name = "ApiKey")}, operationId = "getPaidNoticeDetail")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Obtained paid notice detail.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(name = "CartItem", implementation = CartItem.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "401", description = "Wrong or missing function key.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Not found the transaction.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "429", description = "Too many requests.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "500", description = "Service unavailable.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))})
    @GetMapping(value = "/organizations/{organization-fiscal-code}/notices/{nav}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<CartItem> getPaidNoticeDetailByCfOrgAndNavAndDebtorFiscalCode(
            @Parameter(description = "The fiscal code of the Organization.", required = true) @NotBlank @PathVariable("organization-fiscal-code") String organizationFiscalCode,
            @Parameter(description = "The unique payment identification. Alphanumeric code that uniquely associates and identifies three key elements of a payment: reason, payer, amount", required = true) @NotBlank @PathVariable("nav") String nav,
            @Parameter(description = "Fiscal code of the citizen.", required = true) @NotBlank @RequestHeader(X_FISCAL_CODE) String debtorFiscalCode);

}
