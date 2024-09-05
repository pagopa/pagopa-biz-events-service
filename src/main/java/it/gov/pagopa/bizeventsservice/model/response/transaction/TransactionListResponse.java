package it.gov.pagopa.bizeventsservice.model.response.transaction;

import it.gov.pagopa.bizeventsservice.model.PageInfo;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class TransactionListResponse {
    private List<TransactionListItem> transactionList;
    private String continuationToken;
    private PageInfo pageInfo;
}
