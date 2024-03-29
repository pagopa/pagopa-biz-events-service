package it.gov.pagopa.bizeventsservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
