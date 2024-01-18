package it.gov.pagopa.bizeventsservice.service;

import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListItem;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionDetailResponse;


import java.util.List;

public interface ITransactionService {

    /**
     * Retrieves paged transaction list given a valid fiscal code, offset and page size
     * @param fiscalCode fiscal code to filter transaction list
     * @param start offset page start
     * @param size offset size
     * @return transaction list
     */
    List<TransactionListItem> getTransactionList(String fiscalCode, Integer start, Integer size);
    TransactionDetailResponse getTransactionDetails(String fiscalCode, boolean isCart, String transactionId);

}
