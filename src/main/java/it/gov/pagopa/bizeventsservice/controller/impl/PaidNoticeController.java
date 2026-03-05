package it.gov.pagopa.bizeventsservice.controller.impl;

import it.gov.pagopa.bizeventsservice.controller.IPaidNoticeController;
import it.gov.pagopa.bizeventsservice.model.filterandorder.Order;
import it.gov.pagopa.bizeventsservice.model.response.paidnotice.NoticeDetailResponse;
import it.gov.pagopa.bizeventsservice.model.response.paidnotice.NoticeListWrapResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListResponse;
import it.gov.pagopa.bizeventsservice.service.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;

import static it.gov.pagopa.bizeventsservice.mapper.ConvertViewsToTransactionDetailResponse.convertToNoticeList;
import static it.gov.pagopa.bizeventsservice.util.Constants.X_CONTINUATION_TOKEN;

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
    public ResponseEntity<NoticeDetailResponse> getPaidNoticeDetail(@NotBlank String fiscalCode,
                                                                    @NotBlank String eventId) {
        return new ResponseEntity<>(
                transactionService.getPaidNoticeDetail(fiscalCode, eventId),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<NoticeListWrapResponse> getPaidNotices(String fiscalCode,
                                                                 String continuationToken,
                                                                 Integer size,
                                                                 Boolean isPayer,
                                                                 Boolean isDebtor,
                                                                 Boolean hidden,
                                                                 Order.TransactionListOrder orderBy,
                                                                 Sort.Direction ordering) {
        TransactionListResponse transactionListResponse = transactionService.getTransactionList(fiscalCode, isPayer, isDebtor,
                continuationToken, hidden, size, orderBy, ordering);

        return ResponseEntity.ok()
                .header(X_CONTINUATION_TOKEN, transactionListResponse.getContinuationToken())
                .body(NoticeListWrapResponse.builder().notices(convertToNoticeList(transactionListResponse)).build());
    }

    @Override
    public ResponseEntity<Void> disablePaidNotice(String fiscalCode, String eventId) {
        transactionService.updateBizEventVisibility(fiscalCode, eventId, true);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> enablePaidNotice(String fiscalCode, String eventId) {
        transactionService.updateBizEventVisibility(fiscalCode, eventId, false);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Resource> generatePDF(@NotBlank String fiscalCode, @NotBlank String eventId) {
        // to check if is an OLD event present only on the PM --> the receipt is not available for events present exclusively on the PM
        return transactionService.getPDFReceiptResponse(fiscalCode, eventId);
    }

}
