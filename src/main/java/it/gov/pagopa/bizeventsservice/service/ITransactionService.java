package it.gov.pagopa.bizeventsservice.service;

import it.gov.pagopa.bizeventsservice.model.transaction.TransactionDetailResponse;

public interface ITransactionService {

    TransactionDetailResponse getTransactionDetails(String fiscalCode, boolean isCart, String transactionId);

}
