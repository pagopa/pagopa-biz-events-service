package it.gov.pagopa.bizeventsservice.util;

import it.gov.pagopa.bizeventsservice.entity.view.*;
import it.gov.pagopa.bizeventsservice.entity.view.enumeration.OriginType;
import it.gov.pagopa.bizeventsservice.entity.view.enumeration.PaymentMethodType;

import java.util.ArrayList;
import java.util.List;

public class ViewGenerator {
    public static final String USER_TAX_CODE_WITH_TX = "AAAAAA00A00A000A";
    public static final String TRANSACTION_ID = "transactionId";
    public static final String AUTH_CODE = "authCode";
    public static final String RRN = "rrn";
    public static final String PSP_NAME = "pspName";
    public static final String TRANSACTION_DATE = "2024-03-22T10:23:46.157186";
    public static final String TRANSACTION_DATE_ZULU = "2024-03-22T10:23:46Z";
    public static final String BRAND = "brand";
    public static final String ACCOUNT_HOLDER = "accountHolder";
    public static final String BLURRED_NUMBER = "blurredNumber";
    public static final String PAYER_NAME = "payerName";
    public static final String EVENT_ID = "eventId";
    public static final String SUBJECT = "subject";
    public static final String PAYEE_NAME = "payeeName";
    public static final String AMOUNT = "100";
    public static final String FORMATTED_AMOUNT = "100,00";
    public static final String FORMATTED_GRAND_TOTAL = "500,00";
    public static final String PAYEE_TAX_CODE = "payeeTaxCode";
    public static final String DEBTOR_NAME = "debtorName";
    public static final String REF_NUMBER_TYPE = "refNumberType";
    public static final String REF_NUMBER_VALUE = "refNumberValue";

    public static BizEventsViewUser generateBizEventsViewUser(){
        return BizEventsViewUser.builder()
                .taxCode(USER_TAX_CODE_WITH_TX)
                .transactionId(TRANSACTION_ID)
                .transactionDate(TRANSACTION_DATE)
                .hidden(false)
                .isPayer(Boolean.FALSE)
                .build();
    }

    public static List<BizEventsViewUser> generateListOfFiveBizEventsViewUser(){
        List<BizEventsViewUser> listOfViewUser = new ArrayList<>();
        for(int i = 0; i < 5; i++){
            BizEventsViewUser viewUser = generateBizEventsViewUser();
            viewUser.setTransactionId(viewUser.getTransactionId()+i);
            listOfViewUser.add(viewUser);
        }
        return listOfViewUser;
    }

    public static BizEventsViewGeneral generateBizEventsViewGeneral(){
        return BizEventsViewGeneral.builder()
                .transactionId(TRANSACTION_ID)
                .authCode(AUTH_CODE)
                .paymentMethod(PaymentMethodType.AD)
                .rrn(RRN)
                .pspName(PSP_NAME)
                .transactionDate(TRANSACTION_DATE_ZULU)
                .walletInfo(WalletInfo.builder()
                        .brand(BRAND)
                        .accountHolder(ACCOUNT_HOLDER)
                        .blurredNumber(BLURRED_NUMBER)
                        .build())
                .payer(UserDetail.builder()
                        .name(PAYER_NAME)
                        .taxCode(USER_TAX_CODE_WITH_TX)
                        .build())
                .isCart(true)
                .origin(OriginType.PM)
                .build();
    }

    public static BizEventsViewCart generateBizEventsViewCart(){
        return BizEventsViewCart.builder()
                .transactionId(TRANSACTION_ID)
                .eventId(EVENT_ID)
                .subject(SUBJECT)
                .amount(AMOUNT)
                .payee(UserDetail.builder()
                        .name(PAYEE_NAME)
                        .taxCode(PAYEE_TAX_CODE)
                        .build())
                .debtor(UserDetail.builder()
                        .name(DEBTOR_NAME)
                        .taxCode(USER_TAX_CODE_WITH_TX)
                        .build())
                .refNumberType(REF_NUMBER_TYPE)
                .refNumberValue(REF_NUMBER_VALUE)
                .build();
    }

    public static List<BizEventsViewCart> generateListOfFiveViewCart() {
        List<BizEventsViewCart> listOfCartView = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            BizEventsViewCart viewCart = generateBizEventsViewCart();
            viewCart.setTransactionId(viewCart.getTransactionId() + i);
            listOfCartView.add(viewCart);
        }
        return listOfCartView;
    }
}
