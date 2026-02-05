package it.gov.pagopa.bizeventsservice.controller.impl;

import it.gov.pagopa.bizeventsservice.controller.IBizEventController;
import it.gov.pagopa.bizeventsservice.entity.BizEvent;
import it.gov.pagopa.bizeventsservice.model.filterandorder.Order;
import it.gov.pagopa.bizeventsservice.model.response.paidnotice.NoticeListWrapResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListResponse;
import it.gov.pagopa.bizeventsservice.service.IBizEventsService;
import it.gov.pagopa.bizeventsservice.service.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;

import static it.gov.pagopa.bizeventsservice.mapper.ConvertViewsToTransactionDetailResponse.convertToNoticeList;
import static it.gov.pagopa.bizeventsservice.util.Constants.X_CONTINUATION_TOKEN;

@RestController
public class BizEventController implements IBizEventController {

    private final IBizEventsService bizEventsService;
    private final ITransactionService transactionService;

    @Autowired
    public BizEventController(IBizEventsService bizEventsService, ITransactionService transactionService) {
        this.bizEventsService = bizEventsService;
        this.transactionService = transactionService;
    }

    @Override
    public ResponseEntity<BizEvent> getBizEvent(@NotBlank String bizEventId) {
        return new ResponseEntity<>(bizEventsService.getBizEvent(bizEventId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<BizEvent> getBizEventByOrganizationFiscalCodeAndIuv(
            @NotBlank String organizationFiscalCode, @NotBlank String iuv) {
        return new ResponseEntity<>(bizEventsService.getBizEventByOrgFiscalCodeAndIuv(organizationFiscalCode, iuv),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<NoticeListWrapResponse> getPaidNoticesWithHiddenParam(String fiscalCode,
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
    public ResponseEntity<Void> enablePaidNotice(String fiscalCode, String transactionId) {
        transactionService.enablePaidNotice(fiscalCode, transactionId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
