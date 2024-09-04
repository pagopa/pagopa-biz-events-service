package it.gov.pagopa.bizeventsservice.controller.impl;

import it.gov.pagopa.bizeventsservice.controller.IPaidNoticeController;
import it.gov.pagopa.bizeventsservice.model.filterandorder.Order;
import it.gov.pagopa.bizeventsservice.model.response.paidnotice.PaidNotice;
import it.gov.pagopa.bizeventsservice.model.response.paidnotice.PaidNoticesList;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListResponse;
import it.gov.pagopa.bizeventsservice.service.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Implementation of {@link IPaidNoticeController} that contains the Rest Controller
 * for events services
 */
@RestController
public class PaidNoticeController implements IPaidNoticeController {

    private final ITransactionService transactionService;

    @Autowired
    public PaidNoticeController(ITransactionService transactionService) {
        this.transactionService = transactionService;
    }


    @Override
    public ResponseEntity<PaidNoticesList> getPaidNotices(String fiscalCode, String continuationToken, Integer size, Boolean isPayer, Boolean isDebtor) {
        TransactionListResponse transactionListResponse = transactionService.getTransactionList(fiscalCode, isPayer, isDebtor,
                continuationToken, size, Order.TransactionListOrder.TRANSACTION_DATE, Sort.Direction.DESC);


        List<PaidNotice> paidNoticeList = transactionListResponse.getTransactionList().stream()
                .map(elem -> PaidNotice.builder()
                        .eventId(elem.getTransactionId())
                        .payeeName(elem.getPayeeName())
                        .payeeTaxCode(elem.getPayeeTaxCode())
                        .amount(elem.getAmount())
                        .noticeDate(elem.getTransactionDate())
                        .isCart(elem.getIsCart())
                        .isPayer(elem.getIsPayer())
                        .isDebtor(elem.getIsDebtor())
                        .build())
                .toList();

        return ResponseEntity.ok()
                .header(X_CONTINUATION_TOKEN, transactionListResponse.getContinuationToken())
                .body(PaidNoticesList.builder().paidNoticeList(paidNoticeList).build());
    }

    @Override
    public ResponseEntity<Void> disablePaidNotice(String fiscalCode, String transactionId) {
        transactionService.disableTransaction(fiscalCode, transactionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
