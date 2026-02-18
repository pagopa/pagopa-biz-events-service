package it.gov.pagopa.bizeventsservice.model.response.paidnotice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import it.gov.pagopa.bizeventsservice.entity.view.UserDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

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

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String subject;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String amount;
    private UserDetail payee;
    private UserDetail debtor;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String refNumberValue;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String refNumberType;
}
