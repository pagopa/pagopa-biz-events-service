package it.gov.pagopa.bizeventsservice.mapper;

import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewCart;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewGeneral;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewUser;
import it.gov.pagopa.bizeventsservice.model.response.transaction.CartItem;
import it.gov.pagopa.bizeventsservice.model.response.transaction.InfoTransaction;

import it.gov.pagopa.bizeventsservice.model.response.transaction.*;

import org.apache.commons.validator.GenericValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class ConvertViewsToTransactionDetailResponse {
    private ConvertViewsToTransactionDetailResponse(){}
    
    private static String PAYEE_CART_NAME;
    
    private static final String RECEIPT_DATE_FORMAT_IN = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS";
    private static final String RECEIPT_DATE_FORMAT_OUT = "yyyy-MM-dd'T'HH:mm:ssX";
    
    @Value("${transaction.payee.cartName:Pagamento Multiplo}")
    public void setPayeeCartName(String payeeCartName){
        PAYEE_CART_NAME = payeeCartName;
    }

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
                                .transactionDate(dateFormatZoned(bizEventsViewGeneral.getTransactionDate()))
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

    public static TransactionListItem convertTransactionListItem(BizEventsViewUser viewUser, List<BizEventsViewCart> listOfCartViews){
        AtomicReference<BigDecimal> totalAmount = new AtomicReference<>(BigDecimal.ZERO);
        for (BizEventsViewCart bizEventsViewCart : listOfCartViews) {
            BigDecimal amountExtracted = new BigDecimal(bizEventsViewCart.getAmount());
            totalAmount.updateAndGet(v -> v.add(amountExtracted));
        }
        return TransactionListItem.builder()
                .transactionId(viewUser.getTransactionId())
                .payeeName(listOfCartViews.size() > 1 ? PAYEE_CART_NAME : listOfCartViews.get(0).getPayee().getName())
                .payeeTaxCode(listOfCartViews.size() > 1 ? "" : listOfCartViews.get(0).getPayee().getTaxCode())
                .amount(currencyFormat(totalAmount.get().toString()))
                .transactionDate(dateFormatZoned(viewUser.getTransactionDate()))
                .isCart(listOfCartViews.size() > 1)
                .build();
    }

    private static String currencyFormat(String value) {
        BigDecimal valueToFormat = new BigDecimal(value);
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.ITALY);
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumFractionDigits(2);
        return numberFormat.format(valueToFormat);
    }
    
    private static String dateFormatZoned(String date) {
    	if (!GenericValidator.isDate(date, RECEIPT_DATE_FORMAT_OUT, false)) {
    		return dateFormat(date);
    	} 	
    	return date;
    }
    
   
    private static String dateFormat(String date) {
    	LocalDateTime ldt = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(RECEIPT_DATE_FORMAT_IN));

    	ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneOffset.UTC);        
    	return DateTimeFormatter.ofPattern(RECEIPT_DATE_FORMAT_OUT).format(zdt);
    }
}
