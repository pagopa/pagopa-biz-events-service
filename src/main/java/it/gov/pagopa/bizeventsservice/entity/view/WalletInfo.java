package it.gov.pagopa.bizeventsservice.entity.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class WalletInfo implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -6409303257722729484L;
	private String accountHolder;
    private String brand;
    private String blurredNumber;
    private String maskedEmail;
}
