package it.gov.pagopa.bizeventsservice.mapper;

import it.gov.pagopa.bizeventsservice.entity.BizEvent;
import it.gov.pagopa.bizeventsservice.entity.Transfer;
import it.gov.pagopa.bizeventsservice.model.response.transaction.*;
import it.gov.pagopa.bizeventsservice.model.response.transaction.enumeration.OriginType;
import it.gov.pagopa.bizeventsservice.model.response.transaction.enumeration.PaymentMethodType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConvertBizEventListToTransactionDetailResponse {
    private static final String REMITTANCE_INFORMATION_REGEX = "/TXT/(.*)";
    public static final String MODEL_TYPE_IUV = "1";
    public static final String MODEL_TYPE_NOTICE = "2";
    private static final String REF_TYPE_NOTICE = "codiceAvviso";
    private static final String REF_TYPE_IUV = "IUV";
    private static final String RECEIPT_DATE_FORMAT = "dd MMMM yyyy, HH:mm:ss";

    private ConvertBizEventListToTransactionDetailResponse(){}

    public static TransactionDetailResponse convert(List<BizEvent> listOfBizEvents) {
        BizEvent firstBizEvent = listOfBizEvents.get(0);

        List<CartItem> listOfCartItems = new ArrayList<>();
        AtomicReference<BigDecimal> amount = new AtomicReference<>(BigDecimal.ZERO);

        for (BizEvent bizEvent : listOfBizEvents) {

            listOfCartItems.add(
                    CartItem.builder()
                            .subject(getItemSubject(bizEvent))
                            .amount(getItemAmount(bizEvent))
                            .debtor(getDebtor(bizEvent))
                            .payee(getPayee(bizEvent))
                            .refNumberType(getRefNumberType(bizEvent))
                            .refNumberValue(getRefNumberValue(bizEvent))
                            .build()
            );
            BigDecimal amountExtracted = getAmount(bizEvent);
            amount.updateAndGet(v -> v.add(amountExtracted));
        }

        //TODO VERIFY FIELDS' MAPPING
        return TransactionDetailResponse.builder()
                .infoTransaction(
                        InfoTransaction.builder()
                                .transactionId(getTransactionId(firstBizEvent))
                                .authCode(getAuthCode(firstBizEvent))
                                .rrn(getRrn(firstBizEvent))
                                .transactionDate(getCreationDate(firstBizEvent))
                                .pspName(getPspName(firstBizEvent))
                                .walletInfo(
                                        WalletInfo.builder()
                                                .accountHolder(getPaymentMethodAccountHolder(firstBizEvent))
                                                .brand(getBrand(firstBizEvent))
                                                .blurredNumber(getBlurredNumber(firstBizEvent))
                                                .build()
                                )
                                .payer(getPayer(firstBizEvent))
                                .amount(amount.get().toString())
                                .fee(getFee(firstBizEvent))
                                .paymentMethod(getPaymentMethod(firstBizEvent))
                                .origin(getOrigin(firstBizEvent))
                                .build()
                )
                .carts(listOfCartItems)
                .build();
    }

    private static UserDetail getDebtor(BizEvent bizEvent) {
        if(bizEvent.getDebtor() != null){
            if(bizEvent.getDebtor().getFullName() == null) {
                //TODO THROW EXCEPTION
            }
            if(bizEvent.getDebtor().getEntityUniqueIdentifierValue() == null){
                //TODO THROW EXCEPTION
            }
            return UserDetail.builder()
                            .name(bizEvent.getDebtor().getFullName())
                            .taxCode(bizEvent.getDebtor().getEntityUniqueIdentifierValue())
                            .build();
        }

        return null; //TODO THROW EXCEPTION
    }

    private static PaymentMethodType getPaymentMethod(BizEvent bizEvent){
        if(bizEvent.getPaymentInfo() != null &&
            bizEvent.getPaymentInfo().getPaymentMethod() != null &&
                PaymentMethodType.isValidPaymentMethod(bizEvent.getPaymentInfo().getPaymentMethod())
        ){
            return PaymentMethodType.valueOf(bizEvent.getPaymentInfo().getPaymentMethod());
        }

        return PaymentMethodType.UNKNOWN;
    }

    private static OriginType getOrigin(BizEvent bizEvent) {
        if (bizEvent.getTransactionDetails() != null) {
            if (bizEvent.getTransactionDetails().getTransaction() != null &&
                    bizEvent.getTransactionDetails().getTransaction().getOrigin() != null &&
                    OriginType.isValidOrigin(bizEvent.getTransactionDetails().getTransaction().getOrigin())
            ) {
                return OriginType.valueOf(bizEvent.getTransactionDetails().getTransaction().getOrigin());
            }
            if (bizEvent.getTransactionDetails().getInfo() != null &&
                    bizEvent.getTransactionDetails().getInfo().getClientId() != null &&
                    OriginType.isValidOrigin(bizEvent.getTransactionDetails().getInfo().getClientId() )
            ) {
                return OriginType.valueOf(bizEvent.getTransactionDetails().getInfo().getClientId());
            }
        }
        return OriginType.UNKNOWN;
    }

    private static String getTransactionId(BizEvent bizEvent) {
        if (bizEvent.getTransactionDetails() != null && bizEvent.getTransactionDetails().getTransaction() != null) {
            return bizEvent.getTransactionDetails().getTransaction().getTransactionId();
        }
        return null;//TODO THROW EXCEPTION
    }

    private static String getAuthCode(BizEvent bizEvent) {
        if (bizEvent.getTransactionDetails() != null && bizEvent.getTransactionDetails().getTransaction() != null) {
            return bizEvent.getTransactionDetails().getTransaction().getNumAut();
        }
        return null; //TODO THROW EXCEPTION
    }

    private static String getRrn(BizEvent bizEvent) {
        if (
                bizEvent.getTransactionDetails() != null &&
                        bizEvent.getTransactionDetails().getTransaction() != null &&
                        bizEvent.getTransactionDetails().getTransaction().getRrn() != null
        ) {
            return bizEvent.getTransactionDetails().getTransaction().getRrn();
        }
        if (bizEvent.getPaymentInfo() != null) {
            if (bizEvent.getPaymentInfo().getPaymentToken() != null) {
                return bizEvent.getPaymentInfo().getPaymentToken();
            }
            if (bizEvent.getPaymentInfo().getIUR() != null) {
                return bizEvent.getPaymentInfo().getIUR();
            }
        }
        return null; //TODO THROW EXCEPTION
    }

    private static String getPspName(BizEvent bizEvent) {
        if (bizEvent.getTransactionDetails() != null && bizEvent.getTransactionDetails().getTransaction() != null && bizEvent.getTransactionDetails().getTransaction().getPsp() != null && bizEvent.getTransactionDetails().getTransaction().getPsp().getBusinessName() != null) {
            return bizEvent.getTransactionDetails().getTransaction().getPsp().getBusinessName();
        }
        return bizEvent.getPsp() != null && bizEvent.getPsp().getPsp() != null ? bizEvent.getPsp().getPsp() : null; //TODO THROW EXCEPTION
    }

    private static String getCreationDate(BizEvent bizEvent) {
        if (
                bizEvent.getTransactionDetails() != null &&
                        bizEvent.getTransactionDetails().getTransaction() != null &&
                        bizEvent.getTransactionDetails().getTransaction().getCreationDate() != null
        ) {
            return dateFormatZoned(bizEvent.getTransactionDetails().getTransaction().getCreationDate());
        }
        if (bizEvent.getPaymentInfo() != null && bizEvent.getPaymentInfo().getPaymentDateTime() != null) {
            return dateFormat(bizEvent.getPaymentInfo().getPaymentDateTime());
        }
        return null; //TODO THROW EXCEPTION
    }

    private static String getPaymentMethodAccountHolder(BizEvent bizEvent) {
        if (
                bizEvent.getTransactionDetails() != null &&
                        bizEvent.getTransactionDetails().getWallet() != null &&
                        bizEvent.getTransactionDetails().getWallet().getInfo() != null
        ) {
            return bizEvent.getTransactionDetails().getWallet().getInfo().getHolder();
        }
        return null;
    }

    private static String getBrand(BizEvent bizEvent) {
        if (
                bizEvent.getTransactionDetails() != null &&
                        bizEvent.getTransactionDetails().getWallet() != null &&
                        bizEvent.getTransactionDetails().getWallet().getInfo() != null
        ) {
            return bizEvent.getTransactionDetails().getWallet().getInfo().getBrand();
        }
        return null;
    }

    private static String getBlurredNumber(BizEvent bizEvent) {
        if(bizEvent.getTransactionDetails() != null && bizEvent.getTransactionDetails().getWallet() != null && bizEvent.getTransactionDetails().getWallet().getInfo() != null){
            return bizEvent.getTransactionDetails().getWallet().getInfo().getBlurredNumber();
        }
        return null;
    }

    private static BigDecimal getAmount(BizEvent bizEvent) {
        if (bizEvent.getTransactionDetails() != null && bizEvent.getTransactionDetails().getTransaction() != null) {
            return formatAmount(bizEvent.getTransactionDetails().getTransaction().getGrandTotal()); //TODO GRANDTOTAL CONTAINS FEE, IS WRONG TO ADD IT FOR CART ITEMS
        }
        if (bizEvent.getPaymentInfo() != null && bizEvent.getPaymentInfo().getAmount() != null) {
            return new BigDecimal(bizEvent.getPaymentInfo().getAmount());
        }
        return BigDecimal.ZERO; //TODO THROW EXCEPTION
    }

    private static String getFee(BizEvent bizEvent) {
        if (
                bizEvent.getTransactionDetails() != null &&
                        bizEvent.getTransactionDetails().getTransaction() != null &&
                        bizEvent.getTransactionDetails().getTransaction().getFee() != 0L
        ) {
            return currencyFormat(String.valueOf(bizEvent.getTransactionDetails().getTransaction().getFee() / 100.00));
        }
        return null;
    }

    private static UserDetail getPayer(BizEvent bizEvent) {
        if (bizEvent.getPayer() != null) {
            if(bizEvent.getPayer().getFullName() == null) {
                //TODO THROW EXCEPTION
            }
            if(bizEvent.getPayer().getEntityUniqueIdentifierType() == null){
                //TODO THROW EXCEPTION
            }
            return UserDetail.builder()
                    .name(bizEvent.getPayer().getFullName())
                    .taxCode(bizEvent.getPayer().getEntityUniqueIdentifierType())
                    .build();
        }
        return null; //TODO THROW EXCEPTION
    }

    private static String getItemSubject(BizEvent bizEvent) {
        if (bizEvent.getPaymentInfo() != null && bizEvent.getPaymentInfo().getRemittanceInformation() != null) {
            return bizEvent.getPaymentInfo().getRemittanceInformation();
        }
        List<Transfer> transferList = bizEvent.getTransferList();
        if (transferList != null && !transferList.isEmpty()) {
            double amount = 0;
            String remittanceInformation = null;
            for (Transfer transfer : transferList) {
                double transferAmount;
                try {
                    transferAmount = Double.parseDouble(transfer.getAmount());
                } catch (Exception ignored) {
                    continue;
                }
                if (amount < transferAmount) {
                    amount = transferAmount;
                    remittanceInformation = transfer.getRemittanceInformation();
                }
            }
            return formatRemittanceInformation(remittanceInformation);
        }
        return null; //TODO THROW EXCEPTION
    }

    private static String getItemAmount(BizEvent bizEvent) {
        if (bizEvent.getPaymentInfo() != null && bizEvent.getPaymentInfo().getAmount() != null) {
            return bizEvent.getPaymentInfo().getAmount();
        }
        return null; //TODO THROW EXCEPTION
    }

    private static UserDetail getPayee(BizEvent bizEvent){
        if(bizEvent.getCreditor() != null){
            if(bizEvent.getCreditor().getCompanyName() == null) {
                //TODO THROW EXCEPTION
            }
            if(bizEvent.getCreditor().getIdPA() == null){
                //TODO THROW EXCEPTION
            }
            return UserDetail.builder()
                    .name(bizEvent.getCreditor().getCompanyName())
                    .taxCode(bizEvent.getCreditor().getIdPA())
                    .build();
        }

        return null;//TODO THROW EXCEPTION
    }

    private static String getRefNumberType(BizEvent bizEvent) {
        if (bizEvent.getDebtorPosition() != null && bizEvent.getDebtorPosition().getModelType() != null) {
            if (bizEvent.getDebtorPosition().getModelType().equals(MODEL_TYPE_IUV)) {
                return REF_TYPE_IUV;
            }
            if (bizEvent.getDebtorPosition().getModelType().equals(MODEL_TYPE_NOTICE)) {
                return REF_TYPE_NOTICE;
            }
        }
        return null; //TODO THROW EXCEPTION
    }

    private static String getRefNumberValue(BizEvent bizEvent) {
        if (bizEvent.getDebtorPosition() != null && bizEvent.getDebtorPosition().getModelType() != null) {
            if (bizEvent.getDebtorPosition().getModelType().equals(MODEL_TYPE_IUV) && bizEvent.getDebtorPosition().getIuv() != null) {
                return bizEvent.getDebtorPosition().getIuv();
            }
            if (bizEvent.getDebtorPosition().getModelType().equals(MODEL_TYPE_NOTICE) && bizEvent.getDebtorPosition().getNoticeNumber() != null) {
                return bizEvent.getDebtorPosition().getNoticeNumber();
            }
        }
        return null; //TODO THROW EXCEPTION
    }

    private static BigDecimal formatAmount(long grandTotal) {
        BigDecimal amount = new BigDecimal(grandTotal);
        BigDecimal divider = new BigDecimal(100);
        return amount.divide(divider, 2, RoundingMode.UNNECESSARY);
    }

    private static String formatRemittanceInformation(String remittanceInformation) {
        if (remittanceInformation != null) {
            Pattern pattern = Pattern.compile(REMITTANCE_INFORMATION_REGEX);
            Matcher matcher = pattern.matcher(remittanceInformation);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return remittanceInformation;
    }

    private static String dateFormatZoned(String date) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .append(DateTimeFormatter.ofPattern(RECEIPT_DATE_FORMAT))
                .toFormatter(Locale.ITALY)
                .withZone(TimeZone.getTimeZone("Europe/Rome").toZoneId());
        try {
            return OffsetDateTime.parse(date).format(formatter);
        } catch (DateTimeException e) {
            return null;
        }
    }

    private static String dateFormat(String date) {
        DateTimeFormatter simpleDateFormat = DateTimeFormatter.ofPattern(RECEIPT_DATE_FORMAT).withLocale(Locale.ITALY);
        try {
            return LocalDateTime.parse(date).format(simpleDateFormat);
        } catch (DateTimeException e) {
            return null;
        }
    }

    private static String currencyFormat(String value) {
        BigDecimal valueToFormat = new BigDecimal(value);
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.ITALY);
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumFractionDigits(2);
        return numberFormat.format(valueToFormat);
    }
}
