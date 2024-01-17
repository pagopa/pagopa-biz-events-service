package it.gov.pagopa.bizeventsservice.model.transaction;

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
    private String rnn;
    private String transactionDate;
    private String pspName;
    private WalletInfo walletInfo;
    private UserDetail payer;
    private long amount; //TODO verify amount type (string or long)
    private long fee; //TODO verify fee type (string or long)
}
