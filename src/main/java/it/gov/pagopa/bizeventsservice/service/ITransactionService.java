package it.gov.pagopa.bizeventsservice.service;

import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionDetailResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListResponse;

public interface ITransactionService {

    /**
     * Retrieves paged transaction list given a valid fiscal code, offset and page size
     * @param fiscalCode fiscal code to filter transaction list
     * @param continuationToken continuation token for paginated query
     * @param size offset size
     * @return transaction list
     */
    List<TransactionListItem> getTransactionList(String fiscalCode, String continuationToken, Integer size);
    TransactionDetailResponse getTransactionDetails(String fiscalCode, String transactionId);

    void disableTransaction(String fiscalCode, String transactionId);
}
