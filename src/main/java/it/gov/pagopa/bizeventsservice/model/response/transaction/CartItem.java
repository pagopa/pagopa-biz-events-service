package it.gov.pagopa.bizeventsservice.model.response.transaction;

import it.gov.pagopa.bizeventsservice.entity.view.UserDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Response model for transaction detail API
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class CartItem implements Serializable {

	private static final long serialVersionUID = -6391592801925923358L;
	
	private String subject;
    private String amount;
    private UserDetail payee;
    private UserDetail debtor;
    private String refNumberValue;
    private String refNumberType;
}
