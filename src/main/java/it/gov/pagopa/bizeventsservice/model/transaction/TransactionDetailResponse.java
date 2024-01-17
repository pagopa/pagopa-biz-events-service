package it.gov.pagopa.bizeventsservice.model.transaction;

import it.gov.pagopa.bizeventsservice.model.transaction.enumeration.OriginType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetailResponse {

    private InfoTransaction infoTransaction;
    private List<CartItem> carts;
    private OriginType origin;
}
