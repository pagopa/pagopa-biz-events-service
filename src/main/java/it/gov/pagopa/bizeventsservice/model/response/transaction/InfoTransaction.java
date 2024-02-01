package it.gov.pagopa.bizeventsservice.model.response.transaction;

import it.gov.pagopa.bizeventsservice.entity.view.UserDetail;
import it.gov.pagopa.bizeventsservice.entity.view.WalletInfo;
import it.gov.pagopa.bizeventsservice.entity.view.enumeration.OriginType;
import it.gov.pagopa.bizeventsservice.entity.view.enumeration.PaymentMethodType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Response model for transaction detail API
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfoTransaction implements Serializable {
    private String transactionId;
    private String authCode;
    private String rrn;
    private String transactionDate;
    private String pspName;
    private WalletInfo walletInfo;
    private PaymentMethodType paymentMethod;
    private UserDetail payer;
    private String amount;
    private String fee;
    private OriginType origin;
}
