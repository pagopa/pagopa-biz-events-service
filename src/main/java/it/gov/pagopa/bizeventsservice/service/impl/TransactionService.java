package it.gov.pagopa.bizeventsservice.service.impl;

import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewCart;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewGeneral;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewUser;
import it.gov.pagopa.bizeventsservice.exception.AppError;
import it.gov.pagopa.bizeventsservice.exception.AppException;
import it.gov.pagopa.bizeventsservice.mapper.ConvertViewsToTransactionDetailResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListItem;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionDetailResponse;
import it.gov.pagopa.bizeventsservice.repository.BizEventsRepository;
import it.gov.pagopa.bizeventsservice.repository.BizEventsViewGeneralRepository;
import it.gov.pagopa.bizeventsservice.repository.BizEventsViewCartRepository;
import it.gov.pagopa.bizeventsservice.repository.BizEventsViewUserRepository;
import it.gov.pagopa.bizeventsservice.service.ITransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransactionService implements ITransactionService {

    private final BizEventsRepository bizEventsRepository;
    private final BizEventsViewGeneralRepository bizEventsViewGeneralRepository;
    private final BizEventsViewCartRepository bizEventsViewCartRepository;

    private final BizEventsViewUserRepository bizEventsViewUserRepository;

    @Value("transaction.payee.cartName")
    private String payeeCartName;

    @Autowired
    public TransactionService(BizEventsRepository bizEventsRepository,
                              BizEventsViewGeneralRepository bizEventsViewGeneralRepository,
                              BizEventsViewCartRepository bizEventsViewCartRepository,
                              BizEventsViewUserRepository bizEventsViewUserRepository) {
        this.bizEventsRepository = bizEventsRepository;
        this.bizEventsViewGeneralRepository = bizEventsViewGeneralRepository;
        this.bizEventsViewCartRepository = bizEventsViewCartRepository;
        this.bizEventsViewUserRepository = bizEventsViewUserRepository;
    }

    @Override
    public List<TransactionListItem> getTransactionList(
            String fiscalCode, String continuationToken, Integer size) {

        if (isInvalidFiscalCode(fiscalCode)) {
            throw new AppException(AppError.INVALID_FISCAL_CODE, fiscalCode);
        }

        List<Map<String,Object>> transactionListItems =
                bizEventsRepository.getTransactionPagedIds(fiscalCode, 0, size);
        return transactionListItems.stream().map(x -> {
            Boolean isCart = Boolean.valueOf(String.valueOf(x.getOrDefault("isCart", "false")));
            String transactionId = String.valueOf(x.get("transactionId"));
            TransactionListItem transactionListItem = TransactionListItem
                    .builder()
                    .transactionId(transactionId)
                    .transactionDate(String.valueOf(x.get("transactionDate")))
                    .isCart(isCart)
                    .build();
            if (!isCart) {
                transactionListItem.setPayeeName(String.valueOf(x.get("payeeName")));
                transactionListItem.setPayeeTaxCode(String.valueOf(x.getOrDefault("payeeTaxCode", "")));
                transactionListItem.setAmount(
                        String.valueOf(x.get("grandTotal") != null ?
                                formatAmount(Long.parseLong(String.valueOf(x.get("grandTotal")))) :
                                new BigDecimal(String.valueOf(x.get("amount"))))
                );
            } else {
                List<Map<String,Object>> cartData = bizEventsRepository.getCartData(transactionId);
                transactionListItem.setPayeeName(payeeCartName);
                transactionListItem.setAmount(String.valueOf(BigDecimal.ZERO));
                if (!cartData.isEmpty()) {
                    AtomicReference<BigDecimal> amount = new AtomicReference<>(BigDecimal.ZERO);
                    cartData.forEach(cartItem -> {
                        amount.updateAndGet(v -> v.add(getAmount(cartItem)));
                    });
                    transactionListItem.setAmount(String.valueOf(amount.get()));
                }
            }
            return transactionListItem;
        }).collect(Collectors.toList());
    }

    @Override
    public TransactionDetailResponse getTransactionDetails(String fiscalCode, String eventReference){
        if(isInvalidFiscalCode(fiscalCode)){
            throw new AppException(AppError.INVALID_FISCAL_CODE, fiscalCode);
        }

        List<BizEventsViewGeneral> listOfGeneralViews = this.bizEventsViewGeneralRepository.getBizEventsViewGeneralByTransactionId(eventReference);
        if (listOfGeneralViews.isEmpty()) {
            throw new AppException(AppError.VIEW_GENERAL_NOT_FOUND_WITH_TRANSACTION_ID, eventReference);
        }
        BizEventsViewGeneral bizEventsViewGeneral = listOfGeneralViews.get(0);

        List<BizEventsViewCart> listOfCartViews = this.bizEventsViewCartRepository.getBizEventsViewCartByTransactionIdAndFilteredByFiscalCode(eventReference, fiscalCode);
        if(listOfCartViews.isEmpty()){
            throw new AppException(AppError.VIEW_CART_NOT_FOUND_WITH_TRANSACTION_ID_FOR_USER, eventReference);
        }

        return ConvertViewsToTransactionDetailResponse.convertTransactionDetails(bizEventsViewGeneral, listOfCartViews);
    }

    @Override
    public void disableTransaction(String fiscalCode, String transactionId) {
        if(isInvalidFiscalCode(fiscalCode)){
            throw new AppException(AppError.INVALID_FISCAL_CODE, fiscalCode);
        }

        BizEventsViewUser bizEventsViewUser = this.bizEventsViewUserRepository
                .getBizEventsViewUserByTaxCodeAndTransactionId(fiscalCode, transactionId);
        if (bizEventsViewUser == null) {
            throw new AppException(AppError.VIEW_GENERAL_NOT_FOUND_WITH_TRANSACTION_ID);
        }

        bizEventsViewUser.setHidden(true);
        bizEventsViewUserRepository.save(bizEventsViewUser);
    }

    private static BigDecimal getAmount(Map<String,Object> cartItem) {
        if (cartItem.get("grandTotal") != null) {
            return formatAmount(Long.parseLong(String.valueOf(cartItem.get("grandTotal"))));
        }
        if (String.valueOf(cartItem.get("amount")) != null) {
            return new BigDecimal(String.valueOf(cartItem.get("amount")));
        }
        return BigDecimal.ZERO;
    }

    public static BigDecimal formatAmount(long grandTotal) {
        BigDecimal amount = new BigDecimal(grandTotal);
        BigDecimal divider = new BigDecimal(100);
        return amount.divide(divider, 2, RoundingMode.UNNECESSARY);
    }

    private boolean isInvalidFiscalCode(String fiscalCode) {
        if (fiscalCode != null && !fiscalCode.isEmpty()) {
            Pattern pattern = Pattern.compile("^[A-Z]{6}[0-9LMNPQRSTUV]{2}[ABCDEHLMPRST][0-9LMNPQRSTUV]{2}[A-Z][0-9LMNPQRSTUV]{3}[A-Z]$");
            Matcher matcher = pattern.matcher(fiscalCode);
            return !matcher.find();
        }
        return true;
    }

    public void setPayeeCartName(String payeeCartName) {
        this.payeeCartName = payeeCartName;
    }

}
