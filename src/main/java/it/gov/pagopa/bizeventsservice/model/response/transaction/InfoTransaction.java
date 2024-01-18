package it.gov.pagopa.bizeventsservice.model.response.transaction;

import it.gov.pagopa.bizeventsservice.model.response.transaction.enumeration.OriginType;
import it.gov.pagopa.bizeventsservice.model.response.transaction.enumeration.PaymentMethodType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfoTransaction {

    private String transactionId;
    private String authCode;
    private String rrn;
    private String transactionDate;
    private String pspName;
    private WalletInfo walletInfo;
    private PaymentMethodType paymentMethod;
    private UserDetail payer;
    private String amount; //TODO verify amount type (string or long)
    private String fee; //TODO verify fee type (string or long)
    private OriginType origin;
}
