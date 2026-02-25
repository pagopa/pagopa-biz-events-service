package it.gov.pagopa.bizeventsservice.model.response.paidnotice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import it.gov.pagopa.bizeventsservice.entity.view.UserDetail;
import it.gov.pagopa.bizeventsservice.entity.view.WalletInfo;
import it.gov.pagopa.bizeventsservice.entity.view.enumeration.OriginType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Response model for transaction detail API
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class InfoNotice implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -5306955320137743890L;
    @Schema(required = true)
    @NotNull
    private String eventId;
    private String authCode;
    @Schema(required = true)
    @NotNull
    private String rrn;
    @Schema(required = true)
    @NotNull
    private String noticeDate;
    @Schema(required = true)
    @NotNull
    private String pspName;
    private WalletInfo walletInfo;
    @Schema(example = "APPL, BPAY, RBPS, RBPR, RBPB, RICO, RPIC, GOOG, MYBK, RBPP, PPAL, SATY, KLRN, BBT, BP, AD, CP, PO, OBEP, JIF, MYBK, PPAL, UNKNOWN")
    private String paymentMethod;
    private UserDetail payer;
    @Schema(required = true)
    @NotNull
    private String amount;
    private String fee;
    @Schema(required = true)
    @NotNull
    private OriginType origin;
}
