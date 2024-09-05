package it.gov.pagopa.bizeventsservice.model.response.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Response model for transaction detail API
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetailResponse implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 4998486115671575833L;
    private InfoTransactionView infoTransaction;
    private List<CartItem> carts;
}
