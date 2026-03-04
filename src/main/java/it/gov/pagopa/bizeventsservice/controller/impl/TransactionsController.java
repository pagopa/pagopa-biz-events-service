package it.gov.pagopa.bizeventsservice.controller.impl;

import it.gov.pagopa.bizeventsservice.controller.IPaidNoticeController;
import it.gov.pagopa.bizeventsservice.controller.ITransactionsController;
import it.gov.pagopa.bizeventsservice.model.filterandorder.Order;
import it.gov.pagopa.bizeventsservice.model.response.paidnotice.CartItem;
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
 * Implementation of {@link ITransactionsController} that contains the Rest Controller
 * for transactions events services
 */
@RestController
public class TransactionsController implements ITransactionsController {

    private final ITransactionService transactionService;

    @Autowired
    public TransactionsController(ITransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public ResponseEntity<CartItem> getPaidNoticeDetailByCfOrgAndNavAndDebtorFiscalCode(String organizationFiscalCode, String nav, String debtorFiscalCode) {
        return new ResponseEntity<>(transactionService.getCartItemByCfOrgAndNavAndDebtorFiscalCode(organizationFiscalCode, nav, debtorFiscalCode),
                HttpStatus.OK);
    }

}
