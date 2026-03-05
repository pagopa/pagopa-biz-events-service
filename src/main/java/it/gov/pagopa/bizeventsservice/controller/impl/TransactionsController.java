package it.gov.pagopa.bizeventsservice.controller.impl;

import it.gov.pagopa.bizeventsservice.controller.ITransactionsController;
import it.gov.pagopa.bizeventsservice.model.response.paidnotice.CartItem;
import it.gov.pagopa.bizeventsservice.service.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

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
