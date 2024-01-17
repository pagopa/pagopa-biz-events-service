package it.gov.pagopa.bizeventsservice.service.impl;

import it.gov.pagopa.bizeventsservice.entity.BizEvent;
import it.gov.pagopa.bizeventsservice.exception.AppError;
import it.gov.pagopa.bizeventsservice.exception.AppException;
import it.gov.pagopa.bizeventsservice.mapper.ConvertBizEventListToTransactionDetailResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListItem;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionDetailResponse;
import it.gov.pagopa.bizeventsservice.repository.BizEventsRepository;
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

    @Value("transaction.payee.cartName")
    private String payeeCartName;

    @Autowired
    public TransactionService(BizEventsRepository bizEventsRepository) {
        this.bizEventsRepository = bizEventsRepository;
    }

    @Override
    public List<TransactionListItem> getTransactionList(
            String fiscalCode, Integer start, Integer size) {

        if (!isValidFiscalCode(fiscalCode)) {
            throw new AppException(AppError.INVALID_FISCAL_CODE, fiscalCode);
        }

        List<Map<String,Object>> transactionListItems =
                bizEventsRepository.getTransactionPagedIds(fiscalCode, start, size);
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
    public TransactionDetailResponse getTransactionDetails(String fiscalCode, boolean isCart, String eventReference){
        List<BizEvent> bizEventEntityList;
        if(!isValidFiscalCode(fiscalCode)){
            throw new AppException(AppError.INVALID_FISCAL_CODE, fiscalCode);
        }

        if(isCart){
            bizEventEntityList = this.bizEventsRepository.getBizEventByFiscalCodeAndTransactionId(fiscalCode,eventReference);
        } else {
            bizEventEntityList = this.bizEventsRepository.getBizEventByFiscalCodeAndId(fiscalCode,eventReference);
        }
        if (bizEventEntityList.isEmpty()) {
            throw new AppException(AppError.BIZ_EVENT_NOT_FOUND_WITH_ID, eventReference);
        }

        return ConvertBizEventListToTransactionDetailResponse.convert(bizEventEntityList);
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

    private boolean isValidFiscalCode(String fiscalCode) {
        if (fiscalCode != null && !fiscalCode.isEmpty()) {
            Pattern pattern = Pattern.compile("^[A-Z]{6}[0-9LMNPQRSTUV]{2}[ABCDEHLMPRST][0-9LMNPQRSTUV]{2}[A-Z][0-9LMNPQRSTUV]{3}[A-Z]$");
            Matcher matcher = pattern.matcher(fiscalCode);
            return matcher.find();
        }
        return false;
    }

    public void setPayeeCartName(String payeeCartName) {
        this.payeeCartName = payeeCartName;
    }

}
