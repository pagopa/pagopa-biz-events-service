package it.gov.pagopa.bizeventsservice.model.response.paidnotice;

import it.gov.pagopa.bizeventsservice.entity.view.UserDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;

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
	@Schema(required = true)
    @NotNull
	private String subject;
	@Schema(required = true)
    @NotNull
    private String amount;
    private UserDetail payee;
    private UserDetail debtor;
    @Schema(required = true)
    @NotNull
    private String refNumberValue;
    @Schema(required = true)
    @NotNull
    private String refNumberType;
}
