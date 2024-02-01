package it.gov.pagopa.bizeventsservice.mapper;

import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewCart;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewGeneral;
import it.gov.pagopa.bizeventsservice.model.response.transaction.CartItem;
import it.gov.pagopa.bizeventsservice.model.response.transaction.InfoTransaction;

import it.gov.pagopa.bizeventsservice.model.response.transaction.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class ConvertViewsToTransactionDetailResponse {
    private ConvertViewsToTransactionDetailResponse(){}

    public static TransactionDetailResponse convertTransactionDetails(BizEventsViewGeneral bizEventsViewGeneral, List<BizEventsViewCart> listOfCartViews) {
        List<CartItem> listOfCartItems = new ArrayList<>();
        AtomicReference<BigDecimal> totalAmount = new AtomicReference<>(BigDecimal.ZERO);

        for (BizEventsViewCart bizEventsViewCart : listOfCartViews) {

            listOfCartItems.add(
                    CartItem.builder()
                            .subject(bizEventsViewCart.getSubject())
                            .amount(currencyFormat(String.valueOf(bizEventsViewCart.getAmount())))
                            .debtor(bizEventsViewCart.getDebtor())
                            .payee(bizEventsViewCart.getPayee())
                            .refNumberType(bizEventsViewCart.getRefNumberType())
                            .refNumberValue(bizEventsViewCart.getRefNumberValue())
                            .build()
            );
            BigDecimal amountExtracted = new BigDecimal(bizEventsViewCart.getAmount());
            totalAmount.updateAndGet(v -> v.add(amountExtracted));
        }

        return TransactionDetailResponse.builder()
                .infoTransaction(
                        InfoTransaction.builder()
                                .transactionId(bizEventsViewGeneral.getTransactionId())
                                .authCode(bizEventsViewGeneral.getAuthCode())
                                .rrn(bizEventsViewGeneral.getRrn())
                                .transactionDate(bizEventsViewGeneral.getTransactionDate())
                                .pspName(bizEventsViewGeneral.getPspName())
                                .walletInfo(bizEventsViewGeneral.getWalletInfo())
                                .payer(bizEventsViewGeneral.getPayer())
                                .amount(currencyFormat(totalAmount.get().toString()))
                                .fee(bizEventsViewGeneral.getFee())
                                .paymentMethod(bizEventsViewGeneral.getPaymentMethod())
                                .origin(bizEventsViewGeneral.getOrigin())
                                .build()
                )
                .carts(listOfCartItems)
                .build();
    }

    private static String currencyFormat(String value) {
        BigDecimal valueToFormat = new BigDecimal(value);
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.ITALY);
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumFractionDigits(2);
        return numberFormat.format(valueToFormat);
    }
}
