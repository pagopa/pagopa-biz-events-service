package it.gov.pagopa.bizeventsservice.service.impl;

import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListItem;
import it.gov.pagopa.bizeventsservice.repository.BizEventsRepository;
import it.gov.pagopa.bizeventsservice.service.ITransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TransactionService implements ITransactionService {

    private final BizEventsRepository bizEventsRepository;

    @Autowired
    public TransactionService(BizEventsRepository bizEventsRepository) {
        this.bizEventsRepository = bizEventsRepository;
    }

    @Override
    public List<TransactionListItem> getTransactionList(
            String fiscalCode, Integer start, Integer size) {
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
                        formatAmount(new BigDecimal(String.valueOf(x.get("amount"))))
                );
            } else {
                List<Map<String,Object>> cartData = bizEventsRepository.getCartData(transactionId);
                transactionListItem.setAmount(formatAmount(BigDecimal.ZERO));
                if (!cartData.isEmpty()) {
                    transactionListItem.setPayeeName(String.valueOf(
                            cartData.get(0).get("payeeName"))
                    );

                    AtomicReference<BigDecimal> amount = new AtomicReference<>(BigDecimal.ZERO);
                    cartData.forEach(cartItem -> {
                        amount.updateAndGet(v -> v.add(getAmount(cartItem)));
                    });
                    transactionListItem.setAmount(formatAmount(amount.get()));
                }
            }
            return transactionListItem;
        }).collect(Collectors.toList());
    }

    private String formatAmount(BigDecimal amount) {
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.ITALIAN);
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumFractionDigits(2);
        return numberFormat.format(amount);
    }

    private static BigDecimal getAmount(Map<String,Object> cartItem) {
        if (cartItem.get("grandTotal") != null) {
            return new BigDecimal(String.valueOf(cartItem.get("grandTotal")));
        }
        if (String.valueOf(cartItem.get("amount")) != null) {
            return new BigDecimal(String.valueOf(cartItem.get("amount")));
        }
        return BigDecimal.ZERO;
    }

}
