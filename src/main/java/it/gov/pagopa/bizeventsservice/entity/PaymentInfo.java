package it.gov.pagopa.bizeventsservice.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.gov.pagopa.bizeventsservice.model.MapEntry;
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
public class PaymentInfo {
	private String paymentDateTime;
	private String applicationDate;
	private String transferDate;
	private String dueDate;
	private String paymentToken;
	private String amount;
	private String fee;
	private String primaryCiIncurredFee;
	private String idBundle;
	private String idCiBundle;
	private String totalNotice;
	private String paymentMethod;
	private String touchpoint;
	private String remittanceInformation;
	private String description;
	private List<MapEntry> metadata;
	@JsonProperty(value="IUR")
	private String IUR;
}
