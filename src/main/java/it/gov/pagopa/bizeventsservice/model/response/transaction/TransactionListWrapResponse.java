package it.gov.pagopa.bizeventsservice.model.response.transaction;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class TransactionListWrapResponse {
    private List<TransactionListItem> transactions;
}
