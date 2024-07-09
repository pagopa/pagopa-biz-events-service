package it.gov.pagopa.bizeventsservice.mapper;

import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewCart;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewGeneral;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewUser;
import it.gov.pagopa.bizeventsservice.model.response.transaction.CartItem;
import it.gov.pagopa.bizeventsservice.model.response.transaction.InfoTransactionView;
import it.gov.pagopa.bizeventsservice.util.DateValidator;
import it.gov.pagopa.bizeventsservice.model.response.transaction.*;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class ConvertViewsToTransactionDetailResponse {
    private ConvertViewsToTransactionDetailResponse(){}
    
    private static String payeeCartName;
    
    private static final List<String> LIST_RECEIPT_DATE_FORMAT_IN = Arrays.asList("yyyy-MM-dd'T'HH:mm:ss");
    private static final String RECEIPT_DATE_FORMAT_OUT = "yyyy-MM-dd'T'HH:mm:ssX";
    
    @Value("${transaction.payee.cartName:Pagamento Multiplo}")
    public void setPayeeCartName(String payeeCartNameValue){
        payeeCartName = payeeCartNameValue;
    }

    public static TransactionDetailResponse convertTransactionDetails(String taxCode, BizEventsViewGeneral bizEventsViewGeneral, List<BizEventsViewCart> listOfCartViews) {
        List<CartItem> listOfCartItems = new ArrayList<>();
        AtomicReference<BigDecimal> totalAmount = new AtomicReference<>(BigDecimal.ZERO);
        
        for (BizEventsViewCart bizEventsViewCart : listOfCartViews) {

            listOfCartItems.add(
                    CartItem.builder()
                            .subject(bizEventsViewCart.getSubject())
                            .amount(String.valueOf(bizEventsViewCart.getAmount()))
                            .debtor(bizEventsViewCart.getDebtor())
                            .payee(bizEventsViewCart.getPayee())
                            .refNumberType(bizEventsViewCart.getRefNumberType())
                            .refNumberValue(bizEventsViewCart.getRefNumberValue())
                            .build()
            );
            BigDecimal amountExtracted = new BigDecimal(bizEventsViewCart.getAmount());
            totalAmount.updateAndGet(v -> v.add(amountExtracted));
        }
        
        // PAGOPA-1763: if the tax code refers to a debtor, do not show the sections relating to the payer
        boolean isDebtor = bizEventsViewGeneral.getPayer() == null || !bizEventsViewGeneral.getPayer().getTaxCode().equals(taxCode);
        return TransactionDetailResponse.builder()
                .infoTransaction(
                        InfoTransactionView.builder()
                                .transactionId(bizEventsViewGeneral.getTransactionId())
                                .authCode(bizEventsViewGeneral.getAuthCode())
                                .rrn(bizEventsViewGeneral.getRrn())
                                .transactionDate(dateFormatZoned(bizEventsViewGeneral.getTransactionDate()))
                                .pspName(bizEventsViewGeneral.getPspName())
                                .walletInfo(isDebtor ? null:bizEventsViewGeneral.getWalletInfo())
                                .payer(isDebtor ? null:bizEventsViewGeneral.getPayer())
                                .amount(totalAmount.get().toString())
                                .fee(bizEventsViewGeneral.getFee())
                                .paymentMethod(isDebtor ? null:bizEventsViewGeneral.getPaymentMethod())
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
                .payeeName(listOfCartViews.size() > 1 ? payeeCartName : listOfCartViews.get(0).getPayee().getName())
                .payeeTaxCode(listOfCartViews.size() > 1 ? "" : listOfCartViews.get(0).getPayee().getTaxCode())
                // PAGOPA-1763: the amount value must be returned only if it is not a cart type transaction
                .amount(listOfCartViews.size() > 1 && BooleanUtils.isTrue(viewUser.getIsDebtor()) ? null : totalAmount.get().toString())
                .transactionDate(dateFormatZoned(viewUser.getTransactionDate()))
                .isCart(listOfCartViews.size() > 1)
                .isPayer(BooleanUtils.isTrue(viewUser.getIsPayer()))
                .isDebtor(BooleanUtils.isTrue(viewUser.getIsDebtor()))
                .build();
    }

    /*
    private static String currencyFormat(String value) {
        BigDecimal valueToFormat = new BigDecimal(value);
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.ENGLISH);
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumFractionDigits(2);
        String appo = numberFormat.format(valueToFormat);
        return numberFormat.format(valueToFormat);
    }*/
    
    private static String dateFormatZoned(String date) {
    	String dateSub = StringUtils.substringBeforeLast(date, ".");
    	if (!DateValidator.isValid(dateSub, RECEIPT_DATE_FORMAT_OUT)) {
    		return dateFormat(dateSub);
    	} 	
    	return dateSub;
    }
    
   
    private static String dateFormat(String date) { 	
    	for (String format: LIST_RECEIPT_DATE_FORMAT_IN) {
    		if (DateValidator.isValid(date, format)) {
    			LocalDateTime ldt = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(format));
        		ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneOffset.UTC);   
        		return DateTimeFormatter.ofPattern(RECEIPT_DATE_FORMAT_OUT).format(zdt);
    		}
    	}
    	throw new DateTimeException("The date ["+date+"] is not in one of the expected formats "+LIST_RECEIPT_DATE_FORMAT_IN+" and cannot be parsed");
    }
}
