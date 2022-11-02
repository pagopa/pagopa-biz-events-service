package it.gov.pagopa.bizeventsservice.model.response;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferPA implements Serializable{
	/**
	 * generated serialVersionUID
	 */
	private static final long serialVersionUID = 3529035729352848592L;
	
	private int idTransfer;
	@NotNull(message = "transferAmount is required")
    private BigDecimal transferAmount;
	@NotBlank(message = "fiscalCodePA is required")
    private String fiscalCodePA;
	@NotBlank(message = "iban is required")
    private String iban;
	@NotBlank(message = "remittanceInformation is required")
    private String remittanceInformation;
	@NotBlank(message = "transferCategory is required")
    private String transferCategory;
}
