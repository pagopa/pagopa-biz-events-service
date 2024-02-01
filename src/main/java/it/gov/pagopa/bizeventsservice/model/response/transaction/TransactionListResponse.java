package it.gov.pagopa.bizeventsservice.model.response.transaction;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class TransactionListResponse {
    private List<TransactionListItem> transactionList;
    private String continuationToken;
}
