package it.gov.pagopa.bizeventsservice.model.response.transaction;

import it.gov.pagopa.bizeventsservice.entity.view.UserDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Response model for transaction detail API
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem implements Serializable {

    private String subject;
    private String amount;
    private UserDetail payee;
    private UserDetail debtor;
    private String refNumberValue;
    private String refNumberType;
}
