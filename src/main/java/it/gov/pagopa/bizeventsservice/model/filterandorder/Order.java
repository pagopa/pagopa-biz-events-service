package it.gov.pagopa.bizeventsservice.model.filterandorder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
@Builder
public class Order {

    @NotNull
    private OrderType orderBy;

    @Getter
    @AllArgsConstructor
    public enum TransactionListOrder implements OrderType {
        TRANSACTION_DATE("transactionDate");

        private final String columnName;

    }
}
