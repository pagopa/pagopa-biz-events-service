package it.gov.pagopa.bizeventsservice.service;

import org.springframework.data.domain.Sort.Direction;

import it.gov.pagopa.bizeventsservice.model.filterandorder.Order.TransactionListOrder;
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
    TransactionListResponse getTransactionList(String fiscalCode, String continuationToken, Integer size);
    TransactionListResponse getCachedTransactionList(String fiscalCode, Boolean isPayer, Boolean isDebtor, Integer page, Integer size, TransactionListOrder orderBy, Direction ordering);
    TransactionDetailResponse getTransactionDetails(String fiscalCode, String transactionId);
    byte[] getPDFReceipt (String fiscalCode, String eventId);

    void disableTransaction(String fiscalCode, String transactionId);
}
