package it.gov.pagopa.bizeventsservice.mapper;

import it.gov.pagopa.bizeventsservice.entity.BizEvent;
import it.gov.pagopa.bizeventsservice.entity.Transfer;
import it.gov.pagopa.bizeventsservice.model.response.transaction.CartItem;
import it.gov.pagopa.bizeventsservice.model.response.transaction.InfoTransaction;
import it.gov.pagopa.bizeventsservice.entity.view.UserDetail;
import it.gov.pagopa.bizeventsservice.entity.view.WalletInfo;
import it.gov.pagopa.bizeventsservice.exception.AppError;
import it.gov.pagopa.bizeventsservice.exception.AppException;
import it.gov.pagopa.bizeventsservice.model.response.transaction.*;
import it.gov.pagopa.bizeventsservice.entity.view.enumeration.OriginType;
import it.gov.pagopa.bizeventsservice.entity.view.enumeration.PaymentMethodType;

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

    private static final String[] UNWANTED_REMITTANCE_INFO = System.getenv().getOrDefault("UNWANTED_REMITTANCE_INFO", "pagamento multibeneficiario").split(",");

    private ConvertBizEventListToTransactionDetailResponse(){}

    public static TransactionDetailResponse convert(List<BizEvent> listOfBizEvents) {
        BizEvent firstBizEvent = listOfBizEvents.get(0);

        List<CartItem> listOfCartItems = new ArrayList<>();
        AtomicReference<BigDecimal> amount = new AtomicReference<>(BigDecimal.ZERO);

        int index = 0;
        for (BizEvent bizEvent : listOfBizEvents) {

            listOfCartItems.add(
                    CartItem.builder()
                            .subject(getItemSubject(bizEvent, index))
                            .amount(getItemAmount(bizEvent, index))
                            .debtor(getDebtor(bizEvent, index))
                            .payee(getPayee(bizEvent, index))
                            .refNumberType(getRefNumberType(bizEvent, index))
                            .refNumberValue(getRefNumberValue(bizEvent, index))
                            .build()
            );
            BigDecimal amountExtracted = getAmount(bizEvent);
            amount.updateAndGet(v -> v.add(amountExtracted));
            index++;
        }

        return TransactionDetailResponse.builder()
                .infoTransaction(
                        InfoTransaction.builder()
                                .transactionId(getTransactionId(firstBizEvent))
                                .authCode(getAuthCode(firstBizEvent))
                                .rrn(getRrn(firstBizEvent))
                                .transactionDate(getTransactionDate(firstBizEvent))
                                .pspName(getPspName(firstBizEvent))
                                .walletInfo(
                                        WalletInfo.builder()
                                                .accountHolder(getPaymentMethodAccountHolder(firstBizEvent))
                                                .brand(getBrand(firstBizEvent))
                                                .blurredNumber(getBlurredNumber(firstBizEvent))
                                                .build()
                                )
                                .payer(getPayer(firstBizEvent))
                                .amount(currencyFormat(amount.get().toString()))
                                .fee(getFee(firstBizEvent))
                                .paymentMethod(getPaymentMethod(firstBizEvent))
                                .origin(getOrigin(firstBizEvent))
                                .build()
                )
                .carts(listOfCartItems)
                .build();
    }

    private static UserDetail getDebtor(BizEvent bizEvent, int index) {
        if(bizEvent.getDebtor() != null){
            if(bizEvent.getDebtor().getFullName() == null) {
                throw new AppException(AppError.ERROR_MAPPING_BIZ_EVENT_TO_TRANSACTION_DETAIL, String.format("carts[%s].debtor.name", index), bizEvent.getId());
            }
            if(bizEvent.getDebtor().getEntityUniqueIdentifierValue() == null){
                throw new AppException(AppError.ERROR_MAPPING_BIZ_EVENT_TO_TRANSACTION_DETAIL, String.format("carts[%s].debtor.taxCode", index), bizEvent.getId());
            }
            return UserDetail.builder()
                            .name(bizEvent.getDebtor().getFullName())
                            .taxCode(bizEvent.getDebtor().getEntityUniqueIdentifierValue())
                            .build();
        }

        throw new AppException(AppError.ERROR_MAPPING_BIZ_EVENT_TO_TRANSACTION_DETAIL, String.format("carts[%s].debtor", index), bizEvent.getId());
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
        if (bizEvent.getTransactionDetails() != null && bizEvent.getTransactionDetails().getTransaction() != null && bizEvent.getTransactionDetails().getTransaction().getTransactionId() != null) {
            return bizEvent.getTransactionDetails().getTransaction().getTransactionId();
        }
        throw new AppException(AppError.ERROR_MAPPING_BIZ_EVENT_TO_TRANSACTION_DETAIL, "infoTransaction.transactionId", bizEvent.getId());
    }

    private static String getAuthCode(BizEvent bizEvent) {
        if (bizEvent.getTransactionDetails() != null && bizEvent.getTransactionDetails().getTransaction() != null && bizEvent.getTransactionDetails().getTransaction().getNumAut() != null) {
            return bizEvent.getTransactionDetails().getTransaction().getNumAut();
        }
        throw new AppException(AppError.ERROR_MAPPING_BIZ_EVENT_TO_TRANSACTION_DETAIL, "infoTransaction.authCode", bizEvent.getId());
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
        throw new AppException(AppError.ERROR_MAPPING_BIZ_EVENT_TO_TRANSACTION_DETAIL, "infoTransaction.rrn", bizEvent.getId());
    }

    private static String getPspName(BizEvent bizEvent) {
        if (bizEvent.getTransactionDetails() != null && bizEvent.getTransactionDetails().getTransaction() != null && bizEvent.getTransactionDetails().getTransaction().getPsp() != null && bizEvent.getTransactionDetails().getTransaction().getPsp().getBusinessName() != null) {
            return bizEvent.getTransactionDetails().getTransaction().getPsp().getBusinessName();
        }
        if(bizEvent.getPsp() != null && bizEvent.getPsp().getPsp() != null){
            return bizEvent.getPsp().getPsp();
        }
        throw new AppException(AppError.ERROR_MAPPING_BIZ_EVENT_TO_TRANSACTION_DETAIL, "infoTransaction.pspName", bizEvent.getId());
    }

    private static String getTransactionDate(BizEvent bizEvent) {
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
        throw new AppException(AppError.ERROR_MAPPING_BIZ_EVENT_TO_TRANSACTION_DETAIL, "infoTransaction.transactionDate", bizEvent.getId());
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
        if (bizEvent.getTransactionDetails() != null && bizEvent.getTransactionDetails().getTransaction() != null && bizEvent.getTransactionDetails().getTransaction().getGrandTotal() != 0L) {
            return formatAmount(bizEvent.getTransactionDetails().getTransaction().getGrandTotal()); //TODO GRANDTOTAL CONTAINS FEE, IS WRONG TO ADD IT FOR CART ITEMS
        }
        if (bizEvent.getPaymentInfo() != null && bizEvent.getPaymentInfo().getAmount() != null) {
            return new BigDecimal(bizEvent.getPaymentInfo().getAmount());
        }
        throw new AppException(AppError.ERROR_MAPPING_BIZ_EVENT_TO_TRANSACTION_DETAIL, "infoTransaction.amount", bizEvent.getId());
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
                throw new AppException(AppError.ERROR_MAPPING_BIZ_EVENT_TO_TRANSACTION_DETAIL, "infoTransaction.payer.name", bizEvent.getId());
            }
            if(bizEvent.getPayer().getEntityUniqueIdentifierValue() == null){
                throw new AppException(AppError.ERROR_MAPPING_BIZ_EVENT_TO_TRANSACTION_DETAIL, "infoTransaction.payer.taxCode", bizEvent.getId());
            }
            return UserDetail.builder()
                    .name(bizEvent.getPayer().getFullName())
                    .taxCode(bizEvent.getPayer().getEntityUniqueIdentifierValue())
                    .build();
        }
        throw new AppException(AppError.ERROR_MAPPING_BIZ_EVENT_TO_TRANSACTION_DETAIL, "infoTransaction.payer", bizEvent.getId());
    }

    private static String getItemSubject(BizEvent bizEvent, int index) {
        if (
                bizEvent.getPaymentInfo() != null &&
                        bizEvent.getPaymentInfo().getRemittanceInformation() != null &&
                        !Arrays.asList(UNWANTED_REMITTANCE_INFO).contains(bizEvent.getPaymentInfo().getRemittanceInformation())
        ) {
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
        throw new AppException(AppError.ERROR_MAPPING_BIZ_EVENT_TO_TRANSACTION_DETAIL, String.format("carts[%s].subject", index), bizEvent.getId());
    }

    private static String getItemAmount(BizEvent bizEvent, int index) {
        if (bizEvent.getPaymentInfo() != null && bizEvent.getPaymentInfo().getAmount() != null) {
            return bizEvent.getPaymentInfo().getAmount();
        }
        throw new AppException(AppError.ERROR_MAPPING_BIZ_EVENT_TO_TRANSACTION_DETAIL, String.format("carts[%s].amount",index), bizEvent.getId());
    }

    private static UserDetail getPayee(BizEvent bizEvent, int index){
        if(bizEvent.getCreditor() != null){
            if(bizEvent.getCreditor().getCompanyName() == null) {
                throw new AppException(AppError.ERROR_MAPPING_BIZ_EVENT_TO_TRANSACTION_DETAIL, String.format("carts[%s].payee.name",index), bizEvent.getId());
            }
            if(bizEvent.getCreditor().getIdPA() == null){
                throw new AppException(AppError.ERROR_MAPPING_BIZ_EVENT_TO_TRANSACTION_DETAIL, String.format("carts[%s].payee.taxCode",index), bizEvent.getId());
            }
            return UserDetail.builder()
                    .name(bizEvent.getCreditor().getCompanyName())
                    .taxCode(bizEvent.getCreditor().getIdPA())
                    .build();
        }

        throw new AppException(AppError.ERROR_MAPPING_BIZ_EVENT_TO_TRANSACTION_DETAIL, String.format("carts[%s].payee",index), bizEvent.getId());
    }

    private static String getRefNumberType(BizEvent bizEvent, int index) {
        if (bizEvent.getDebtorPosition() != null && bizEvent.getDebtorPosition().getModelType() != null) {
            if (bizEvent.getDebtorPosition().getModelType().equals(MODEL_TYPE_IUV)) {
                return REF_TYPE_IUV;
            }
            if (bizEvent.getDebtorPosition().getModelType().equals(MODEL_TYPE_NOTICE)) {
                return REF_TYPE_NOTICE;
            }
        }
        throw new AppException(AppError.ERROR_MAPPING_BIZ_EVENT_TO_TRANSACTION_DETAIL, String.format("carts[%s].refNumberType",index), bizEvent.getId());
    }

    private static String getRefNumberValue(BizEvent bizEvent, int index) {
        if (bizEvent.getDebtorPosition() != null && bizEvent.getDebtorPosition().getModelType() != null) {
            if (bizEvent.getDebtorPosition().getModelType().equals(MODEL_TYPE_IUV) && bizEvent.getDebtorPosition().getIuv() != null) {
                return bizEvent.getDebtorPosition().getIuv();
            }
            if (bizEvent.getDebtorPosition().getModelType().equals(MODEL_TYPE_NOTICE) && bizEvent.getDebtorPosition().getNoticeNumber() != null) {
                return bizEvent.getDebtorPosition().getNoticeNumber();
            }
        }
        throw new AppException(AppError.ERROR_MAPPING_BIZ_EVENT_TO_TRANSACTION_DETAIL, String.format("carts[%s].refNumberValue",index), bizEvent.getId());
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
