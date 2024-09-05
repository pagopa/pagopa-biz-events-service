package it.gov.pagopa.bizeventsservice.controller.impl;

import it.gov.pagopa.bizeventsservice.controller.IPaidNoticeController;
import it.gov.pagopa.bizeventsservice.model.filterandorder.Order;
import it.gov.pagopa.bizeventsservice.model.response.paidnotice.NoticeListWrapResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListResponse;
import it.gov.pagopa.bizeventsservice.service.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import static it.gov.pagopa.bizeventsservice.mapper.ConvertViewsToTransactionDetailResponse.convertToNoticeList;

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
    public ResponseEntity<NoticeListWrapResponse> getPaidNotices(String fiscalCode,
                                                                 String continuationToken,
                                                                 Integer size,
                                                                 Boolean isPayer,
                                                                 Boolean isDebtor,
                                                                 Order.TransactionListOrder orderBy,
                                                                 Sort.Direction ordering) {
        TransactionListResponse transactionListResponse = transactionService.getTransactionList(fiscalCode, isPayer, isDebtor,
                continuationToken, size, orderBy, ordering);


        return ResponseEntity.ok()
                .header(X_CONTINUATION_TOKEN, transactionListResponse.getContinuationToken())
                .body(NoticeListWrapResponse.builder().notices(convertToNoticeList(transactionListResponse)).build());
    }

    @Override
    public ResponseEntity<Void> disablePaidNotice(String fiscalCode, String transactionId) {
        transactionService.disableTransaction(fiscalCode, transactionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
