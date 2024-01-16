package it.gov.pagopa.bizeventsservice.service;

import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListItem;

import java.util.List;

public interface ITransactionService {
    List<TransactionListItem> getTransactionList(String fiscalCode, Integer start, Integer size);

}
