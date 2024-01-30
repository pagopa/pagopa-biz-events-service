package it.gov.pagopa.bizeventsservice.util;

import it.gov.pagopa.bizeventsservice.entity.*;
import it.gov.pagopa.bizeventsservice.model.response.enumeration.StatusType;
import it.gov.pagopa.bizeventsservice.entity.view.enumeration.OriginType;
import it.gov.pagopa.bizeventsservice.entity.view.enumeration.PaymentMethodType;

import java.util.ArrayList;
import java.util.List;

public class BizEventGenerator {

    public static final String COMPANY_NAME = "PA paolo";
    public static final String ID_PSP = "ID_PSP";
    public static final String DEBTOR_FULL_NAME = "John Doe";
    public static final String DEBTOR_FULL_NAME_INVALID = "-- --";
    public static final String DEBTOR_FULL_NAME_SPECIAL_CHAR = "John,Doe:Megacorp;SRL::Avenue;Street/";
    public static final String DEBTOR_FULL_NAME_SPECIAL_CHAR_FORMATTED = "John Doe Megacorp SRL Avenue Street ";
    public static final String DEBTOR_VALID_CF = "CF_DEBTOR";
    public static final String PAYER_FULL_NAME = "John Doe PAYER";
    public static final String PAYER_VALID_CF = "CF_PAYER";
    public static final String HOLDER_FULL_NAME = "John Doe HOLDER";
    public static final String PAYMENT_TOKEN = "9a9bad2caf604b86a339476373c659b0";
    public static final String AMOUNT_WITHOUT_CENTS = "7000";
    public static final String FEE_WITH_SINGLE_DIGIT_CENTS = "77.7";
    public static final long AMOUNT_LONG = 700000L;
    public static final long FEE_LONG = 7770L;
    public static final long GRAND_TOTAL_LONG = 707770L;
    public static final String FORMATTED_AMOUNT = "7.000,00";
    public static final String FORMATTED_FEE = "77,70";
    public static final String FORMATTED_GRAND_TOTAL = "7.077,70";
    public static final String REMITTANCE_INFORMATION = "TARI 2021";
    public static final String REMITTANCE_INFORMATION_TRANSFER_LIST = "EXAMPLE/TXT/TARI 2021/EXAMPLE";
    public static final String REMITTANCE_INFORMATION_TRANSFER_LIST_FORMATTED = "TARI 2021/EXAMPLE";
    public static final String IUR = "IUR";
    public static final String BRAND = "MASTER";
    public static final String ID_TRANSACTION = "1";
    public static final String RRN = "rrn";
    public static final String AUTH_CODE = "authCode";
    public static final String DATE_TIME_TIMESTAMP_MILLISECONDS_DST_WINTER = "2023-11-14T19:31:55.484065";
    public static final String DATE_TIME_TIMESTAMP_MILLISECONDS_DST_SUMMER = "2023-08-05T11:11:54.484065";
    public static final String DATE_TIME_TIMESTAMP_ZONED_DST_WINTER = "2023-11-14T18:31:55Z";
    public static final String DATE_TIME_TIMESTAMP_ZONED_DST_SUMMER = "2023-08-05T09:11:54Z";
    public static final String DATE_TIME_TIMESTAMP_ZONED_MILLISECONDS_DST_WINTER = "2023-11-14T18:31:55.306516999Z";
    public static final String DATE_TIME_TIMESTAMP_ZONED_MILLISECONDS_DST_SUMMER = "2023-08-05T09:11:54.306516999Z";
    public static final boolean GENERATED_BY_DEBTOR = true;
    public static final boolean GENERATED_BY_PAYER = false;
    public static final String PSP_NAME = "name";
    public static final String PSP_LOGO = "logo";
    public static final String PSP_COMPANY = "companyName";
    public static final String PSP_CITY = "city";
    public static final String PSP_POSTAL_CODE = "postalCode";
    public static final String PSP_ADDRESS = "address";
    public static final String PSP_BUILDING_NUMBER = "buildingNumber";
    public static final String PSP_PROVINCE = "province";
    public static final String BRAND_ASSET_URL = "/asset";
    public static final String IUV = "02119891614290410";
    public static final String NOTICE_NUMBER = "valid notice number";
    public static final String BIZ_EVENT_ID = "biz-event-id";
    public static final String MODEL_TYPE_IUV_CODE = "1";
    public static final String MODEL_TYPE_NOTICE_CODE = "2";
    public static final String MODEL_TYPE_NOTICE_TEXT = "codiceAvviso";
    public static final String MODEL_TYPE_IUV_TEXT = "IUV";
    public static final String DATE_TIME_TIMESTAMP_FORMATTED_DST_WINTER = "14 novembre 2023, 19:31:55";
    public static final String DATE_TIME_TIMESTAMP_FORMATTED_DST_SUMMER = "05 agosto 2023, 11:11:54";
    public static final String PAGO_PA_CHANNEL_IO = "IO";
    public static final String PAGO_PA_CHANNEL_IO_PAY = "IO-PAY";
    public static final String NOT_PAGO_PA_CHANNEL = "NOT_PAGO_PA_CHANNEL";
    public static final String ID_PA = "idPa";
    public static final String USER_NAME = "user_name";
    public static final String USER_SURNAME = "user_surname";
    public static final String USER_FORMATTED_FULL_NAME = "user_name user_surname";
    public static final String USER_TAX_CODE = "user tax code";
    public static final String TRANSACTION_ORIGIN = OriginType.PM.name();
    public static final String TRANSFER_AMOUNT_HIGHEST = "10000.00";
    public static final String TRANSFER_AMOUNT_MEDIUM = "20.00";
    public static final String TRANSFER_AMOUNT_LOWEST = "10.00";

    public static BizEvent generateValidBizEvent(int index){
        return BizEvent.builder()
                .id(BIZ_EVENT_ID+index)
                .idPaymentManager(BIZ_EVENT_ID)
                .debtorPosition(DebtorPosition.builder()
                        .iuv(IUV+index)
                        .modelType(MODEL_TYPE_IUV_CODE)
                        .build())
                .creditor(Creditor.builder()
                        .companyName(COMPANY_NAME+index)
                        .idPA(ID_PA+index)
                        .build())
                .psp(Psp.builder()
                        .idPsp(ID_PSP)
                        .psp(PSP_NAME)
                        .build())
                .debtor(Debtor.builder()
                        .fullName(DEBTOR_FULL_NAME+index)
                        .entityUniqueIdentifierValue(DEBTOR_VALID_CF+index)
                        .build())
                .payer(Payer.builder().fullName(PAYER_FULL_NAME).entityUniqueIdentifierValue(PAYER_VALID_CF).build())
                .paymentInfo(PaymentInfo.builder()
                        .paymentDateTime(DATE_TIME_TIMESTAMP_MILLISECONDS_DST_WINTER)
                        .paymentToken(PAYMENT_TOKEN)
                        .amount(AMOUNT_WITHOUT_CENTS)
                        .fee(FEE_WITH_SINGLE_DIGIT_CENTS)
                        .remittanceInformation(REMITTANCE_INFORMATION)
                        .IUR(IUR)
                        .paymentMethod(PaymentMethodType.AD.name())
                        .build())
                .transactionDetails(TransactionDetails.builder()
                        .wallet(WalletItem.builder()
                                .info(Info.builder().brand(BRAND).holder(HOLDER_FULL_NAME).build())
                                .onboardingChannel(PAGO_PA_CHANNEL_IO)
                                .build())
                        .transaction(Transaction.builder()
                                .idTransaction(ID_TRANSACTION)
                                .transactionId(ID_TRANSACTION)
                                .grandTotal(GRAND_TOTAL_LONG)
                                .amount(AMOUNT_LONG)
                                .fee(FEE_LONG)
                                .rrn(RRN)
                                .numAut(AUTH_CODE)
                                .creationDate(DATE_TIME_TIMESTAMP_ZONED_DST_WINTER)
                                .psp(TransactionPsp.builder()
                                        .businessName(PSP_NAME)
                                        .build())
                                .origin(TRANSACTION_ORIGIN)
                                .build())
                        .info(InfoTransaction.builder().clientId(TRANSACTION_ORIGIN).build())
                        .build())
                .transferList(
                        List.of(
                                Transfer.builder()
                                        .amount(TRANSFER_AMOUNT_LOWEST)
                                        .remittanceInformation("not to show")
                                        .build(),
                                Transfer.builder()
                                        .amount(TRANSFER_AMOUNT_MEDIUM)
                                        .remittanceInformation("not to show")
                                        .build(),
                                Transfer.builder()
                                        .amount(TRANSFER_AMOUNT_HIGHEST)
                                        .remittanceInformation(REMITTANCE_INFORMATION_TRANSFER_LIST)
                                        .build()
                        )
                )
                .eventStatus(StatusType.DONE)
                .build();
    }

    public static BizEvent generateValidBizEventModelTypeNotice(){
        return BizEvent.builder()
                .id(BIZ_EVENT_ID)
                .idPaymentManager(BIZ_EVENT_ID)
                .debtorPosition(DebtorPosition.builder()
                        .iuv(IUV)
                        .modelType(MODEL_TYPE_NOTICE_CODE)
                        .build())
                .creditor(Creditor.builder()
                        .companyName(COMPANY_NAME)
                        .idPA(ID_PA)
                        .build())
                .psp(Psp.builder()
                        .idPsp(ID_PSP)
                        .psp(PSP_NAME)
                        .build())
                .debtor(Debtor.builder()
                        .fullName(DEBTOR_FULL_NAME)
                        .entityUniqueIdentifierValue(DEBTOR_VALID_CF)
                        .build())
                .payer(Payer.builder().fullName(PAYER_FULL_NAME).entityUniqueIdentifierValue(PAYER_VALID_CF).build())
                .paymentInfo(PaymentInfo.builder()
                        .paymentDateTime(DATE_TIME_TIMESTAMP_MILLISECONDS_DST_WINTER)
                        .paymentToken(PAYMENT_TOKEN)
                        .amount(AMOUNT_WITHOUT_CENTS)
                        .fee(FEE_WITH_SINGLE_DIGIT_CENTS)
                        .remittanceInformation(REMITTANCE_INFORMATION)
                        .IUR(IUR)
                        .paymentMethod(PaymentMethodType.AD.name())
                        .build())
                .transactionDetails(TransactionDetails.builder()
                        .wallet(WalletItem.builder()
                                .info(Info.builder().brand(BRAND).holder(HOLDER_FULL_NAME).build())
                                .onboardingChannel(PAGO_PA_CHANNEL_IO)
                                .build())
                        .transaction(Transaction.builder()
                                .idTransaction(ID_TRANSACTION)
                                .transactionId(ID_TRANSACTION)
                                .grandTotal(GRAND_TOTAL_LONG)
                                .amount(AMOUNT_LONG)
                                .fee(FEE_LONG)
                                .rrn(RRN)
                                .numAut(AUTH_CODE)
                                .creationDate(DATE_TIME_TIMESTAMP_ZONED_DST_WINTER)
                                .psp(TransactionPsp.builder()
                                        .businessName(PSP_NAME)
                                        .build())
                                .origin(TRANSACTION_ORIGIN)
                                .build())
                        .build())
                .eventStatus(StatusType.DONE)
                .build();
    }

    public static BizEvent generateValidBizEventTimestampDSTSummer(){
        return BizEvent.builder()
                .id(BIZ_EVENT_ID)
                .idPaymentManager(BIZ_EVENT_ID)
                .debtorPosition(DebtorPosition.builder()
                        .iuv(IUV)
                        .modelType(MODEL_TYPE_IUV_CODE)
                        .build())
                .creditor(Creditor.builder()
                        .companyName(COMPANY_NAME)
                        .idPA(ID_PA)
                        .build())
                .psp(Psp.builder()
                        .idPsp(ID_PSP)
                        .psp(PSP_NAME)
                        .build())
                .debtor(Debtor.builder()
                        .fullName(DEBTOR_FULL_NAME)
                        .entityUniqueIdentifierValue(DEBTOR_VALID_CF)
                        .build())
                .payer(Payer.builder().fullName(PAYER_FULL_NAME).entityUniqueIdentifierValue(PAYER_VALID_CF).build())
                .paymentInfo(PaymentInfo.builder()
                        .paymentDateTime(DATE_TIME_TIMESTAMP_MILLISECONDS_DST_WINTER)
                        .paymentToken(PAYMENT_TOKEN)
                        .amount(AMOUNT_WITHOUT_CENTS)
                        .fee(FEE_WITH_SINGLE_DIGIT_CENTS)
                        .remittanceInformation(REMITTANCE_INFORMATION)
                        .IUR(IUR)
                        .build())
                .transactionDetails(TransactionDetails.builder()
                        .wallet(WalletItem.builder()
                                .info(Info.builder().brand(BRAND).holder(HOLDER_FULL_NAME).build())
                                .onboardingChannel(PAGO_PA_CHANNEL_IO)
                                .build())
                        .transaction(Transaction.builder()
                                .idTransaction(ID_TRANSACTION)
                                .transactionId(ID_TRANSACTION)
                                .grandTotal(GRAND_TOTAL_LONG)
                                .amount(AMOUNT_LONG)
                                .fee(FEE_LONG)
                                .rrn(RRN)
                                .numAut(AUTH_CODE)
                                .creationDate(DATE_TIME_TIMESTAMP_ZONED_DST_SUMMER)
                                .psp(TransactionPsp.builder()
                                        .businessName(PSP_NAME)
                                        .build())
                                .origin(TRANSACTION_ORIGIN)
                                .build())
                        .build())
                .eventStatus(StatusType.DONE)
                .build();
    }

    public static BizEvent generateValidBizEventTimestampMillisecondsDSTWinter(){
        return BizEvent.builder()
                .id(BIZ_EVENT_ID)
                .idPaymentManager(BIZ_EVENT_ID)
                .debtorPosition(DebtorPosition.builder()
                        .iuv(IUV)
                        .modelType(MODEL_TYPE_IUV_CODE)
                        .build())
                .creditor(Creditor.builder()
                        .companyName(COMPANY_NAME)
                        .idPA(ID_PA)
                        .build())
                .psp(Psp.builder()
                        .idPsp(ID_PSP)
                        .psp(PSP_NAME)
                        .build())
                .debtor(Debtor.builder()
                        .fullName(DEBTOR_FULL_NAME)
                        .entityUniqueIdentifierValue(DEBTOR_VALID_CF)
                        .build())
                .payer(Payer.builder().fullName(PAYER_FULL_NAME).entityUniqueIdentifierValue(PAYER_VALID_CF).build())
                .paymentInfo(PaymentInfo.builder()
                        .paymentDateTime(DATE_TIME_TIMESTAMP_MILLISECONDS_DST_WINTER)
                        .paymentToken(PAYMENT_TOKEN)
                        .amount(AMOUNT_WITHOUT_CENTS)
                        .fee(FEE_WITH_SINGLE_DIGIT_CENTS)
                        .remittanceInformation(REMITTANCE_INFORMATION)
                        .IUR(IUR)
                        .build())
                .transactionDetails(TransactionDetails.builder()
                        .wallet(WalletItem.builder()
                                .info(Info.builder().brand(BRAND).holder(HOLDER_FULL_NAME).build())
                                .onboardingChannel(PAGO_PA_CHANNEL_IO)
                                .build())
                        .transaction(Transaction.builder()
                                .idTransaction(ID_TRANSACTION)
                                .transactionId(ID_TRANSACTION)
                                .grandTotal(GRAND_TOTAL_LONG)
                                .amount(AMOUNT_LONG)
                                .fee(FEE_LONG)
                                .rrn(RRN)
                                .numAut(AUTH_CODE)
                                .creationDate(DATE_TIME_TIMESTAMP_ZONED_MILLISECONDS_DST_WINTER)
                                .psp(TransactionPsp.builder()
                                        .businessName(PSP_NAME)
                                        .build())
                                .origin(TRANSACTION_ORIGIN)
                                .build())
                        .build())
                .eventStatus(StatusType.DONE)
                .build();
    }

    public static BizEvent generateValidBizEventTimestampMillisecondsDSTSummer(){
        return BizEvent.builder()
                .id(BIZ_EVENT_ID)
                .idPaymentManager(BIZ_EVENT_ID)
                .debtorPosition(DebtorPosition.builder()
                        .iuv(IUV)
                        .modelType(MODEL_TYPE_IUV_CODE)
                        .build())
                .creditor(Creditor.builder()
                        .companyName(COMPANY_NAME)
                        .idPA(ID_PA)
                        .build())
                .psp(Psp.builder()
                        .idPsp(ID_PSP)
                        .psp(PSP_NAME)
                        .build())
                .debtor(Debtor.builder()
                        .fullName(DEBTOR_FULL_NAME)
                        .entityUniqueIdentifierValue(DEBTOR_VALID_CF)
                        .build())
                .payer(Payer.builder().fullName(PAYER_FULL_NAME).entityUniqueIdentifierValue(PAYER_VALID_CF).build())
                .paymentInfo(PaymentInfo.builder()
                        .paymentDateTime(DATE_TIME_TIMESTAMP_MILLISECONDS_DST_WINTER)
                        .paymentToken(PAYMENT_TOKEN)
                        .amount(AMOUNT_WITHOUT_CENTS)
                        .fee(FEE_WITH_SINGLE_DIGIT_CENTS)
                        .remittanceInformation(REMITTANCE_INFORMATION)
                        .IUR(IUR)
                        .build())
                .transactionDetails(TransactionDetails.builder()
                        .wallet(WalletItem.builder()
                                .info(Info.builder().brand(BRAND).holder(HOLDER_FULL_NAME).build())
                                .onboardingChannel(PAGO_PA_CHANNEL_IO)
                                .build())
                        .transaction(Transaction.builder()
                                .idTransaction(ID_TRANSACTION)
                                .transactionId(ID_TRANSACTION)
                                .grandTotal(GRAND_TOTAL_LONG)
                                .amount(AMOUNT_LONG)
                                .fee(FEE_LONG)
                                .rrn(RRN)
                                .numAut(AUTH_CODE)
                                .creationDate(DATE_TIME_TIMESTAMP_ZONED_MILLISECONDS_DST_SUMMER)
                                .psp(TransactionPsp.builder()
                                        .businessName(PSP_NAME)
                                        .build())
                                .origin(TRANSACTION_ORIGIN)
                                .build())
                        .build())
                .eventStatus(StatusType.DONE)
                .build();
    }

    public static List<BizEvent> generateListOfValidBizEvents(int numberOfBizEvents){
        List<BizEvent> bizEventList = new ArrayList<>();
        for(int i = 0; i < numberOfBizEvents; i++){
            bizEventList.add(generateValidBizEvent(i));
        }

        return bizEventList;
    }
}
