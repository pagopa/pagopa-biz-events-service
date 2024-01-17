package it.gov.pagopa.bizeventsservice.service;

import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListItem;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionDetailResponse;


import java.util.List;

public interface ITransactionService {
    List<TransactionListItem> getTransactionList(String fiscalCode, Integer start, Integer size);
    TransactionDetailResponse getTransactionDetails(String fiscalCode, boolean isCart, String transactionId);

}
