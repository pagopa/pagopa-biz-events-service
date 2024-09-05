package it.gov.pagopa.bizeventsservice.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDetails {
    private User user;
    private PaymentAuthorizationRequest paymentAuthorizationRequest;
    private WalletItem wallet;
    private String origin;
    private Transaction transaction;
    private InfoTransaction info;
}
