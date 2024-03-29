package it.gov.pagopa.bizeventsservice.model.response.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Response model for transaction list API
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionListItem implements Serializable {
    private String transactionId;
    private String payeeName;
    private String payeeTaxCode;
    private String amount;
    private String transactionDate;
    private Boolean isCart;
}
