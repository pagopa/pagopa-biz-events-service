package it.gov.pagopa.bizeventsservice.mapper;

import it.gov.pagopa.bizeventsservice.entity.BizEvent;
import it.gov.pagopa.bizeventsservice.entity.Transfer;
import it.gov.pagopa.bizeventsservice.model.transaction.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.*;
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

        for (BizEvent bizEvent : listOfBizEvents) {
            listOfCartItems.add(
                    CartItem.builder()
                            .subject(getItemSubject(bizEvent))
                            .amount(getItemAmount(bizEvent))
                            .debtor(bizEvent.getDebtor() != null ?
                                    UserDetail.builder()
                                            .name(bizEvent.getDebtor().getFullName())
                                            .taxCode(bizEvent.getDebtor().getEntityUniqueIdentifierValue())
                                            .build() :
                                    UserDetail.builder().build()
                            )
                            .payee(
                                    UserDetail.builder()
                                            .name(bizEvent.getCreditor() != null ? bizEvent.getCreditor().getCompanyName() : null)
                                            .taxCode(getPayeeTaxCode(bizEvent))
                                            .build()
                            )
                            .refNumberType(getRefNumberType(bizEvent))
                            .refNumberValue(getRefNumberValue(bizEvent))
                            .build()
            );
        }

        //TODO VERIFY FIELDS' MAPPING
        return TransactionDetailResponse.builder()
                .infoTransaction(
                        InfoTransaction.builder()
                                .transactionId(getTransactionId(listOfBizEvents, firstBizEvent))
                                .authCode(getAuthCode(firstBizEvent))
                                .rnn(getRnn(firstBizEvent))
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
                                .amount(getAmount(firstBizEvent))
                                .fee(getFee(firstBizEvent))
                                .build()
                )
                .carts(listOfCartItems)
                .origin(getOrigin(firstBizEvent))
                .build();
    }

    private static String getOrigin(BizEvent event) {
        if (event.getTransactionDetails() != null) {
            if (event.getTransactionDetails().getTransaction() != null &&
                    event.getTransactionDetails().getTransaction().getOrigin() != null) {
                return event.getTransactionDetails().getTransaction().getOrigin();
            }
            if (event.getTransactionDetails().getInfo() != null &&
                    event.getTransactionDetails().getInfo().getClientId() != null) {
                return event.getTransactionDetails().getInfo().getClientId();
            }
        }
        return null;
    }

    private static String getTransactionId(List<BizEvent> listOfBizEvents, BizEvent bizEvent) {
        if (listOfBizEvents.size() > 1 && bizEvent.getTransactionDetails() != null && bizEvent.getTransactionDetails().getTransaction() != null) {
            return bizEvent.getTransactionDetails().getTransaction().getTransactionId();
        }
        return bizEvent.getId();
    }

    private static String getAuthCode(BizEvent event) {
        if (event.getTransactionDetails() != null && event.getTransactionDetails().getTransaction() != null) {
            return event.getTransactionDetails().getTransaction().getNumAut();
        }
        return null;
    }

    private static String getRnn(BizEvent event) {
        if (
                event.getTransactionDetails() != null &&
                        event.getTransactionDetails().getTransaction() != null &&
                        event.getTransactionDetails().getTransaction().getRrn() != null
        ) {
            return event.getTransactionDetails().getTransaction().getRrn(); //TODO RNN OR RRN?
        }
        if (event.getPaymentInfo() != null) {
            if (event.getPaymentInfo().getPaymentToken() != null) {
                return event.getPaymentInfo().getPaymentToken();
            }
            if (event.getPaymentInfo().getIUR() != null) {
                return event.getPaymentInfo().getIUR();
            }
        }
        return null;
    }

    private static String getPspName(BizEvent event) {
        if (event.getTransactionDetails() != null && event.getTransactionDetails().getTransaction() != null && event.getTransactionDetails().getTransaction().getPsp() != null && event.getTransactionDetails().getTransaction().getPsp().getBusinessName() != null) {
            return event.getTransactionDetails().getTransaction().getPsp().getBusinessName();
        }
        return event.getPsp() != null && event.getPsp().getPsp() != null ? event.getPsp().getPsp() : null;
    }

    private static String getCreationDate(BizEvent event) {
        if (
                event.getTransactionDetails() != null &&
                        event.getTransactionDetails().getTransaction() != null &&
                        event.getTransactionDetails().getTransaction().getCreationDate() != null
        ) {
            return dateFormatZoned(event.getTransactionDetails().getTransaction().getCreationDate());
        }
        if (event.getPaymentInfo() != null && event.getPaymentInfo().getPaymentDateTime() != null) {
            return dateFormat(event.getPaymentInfo().getPaymentDateTime());
        }
        return null;
    }

    private static String getPaymentMethodAccountHolder(BizEvent event) {
        if (
                event.getTransactionDetails() != null &&
                        event.getTransactionDetails().getWallet() != null &&
                        event.getTransactionDetails().getWallet().getInfo() != null &&
                        event.getTransactionDetails().getWallet().getInfo().getHolder() != null
        ) {
            return event.getTransactionDetails().getWallet().getInfo().getHolder();
        }
        return null;
    }

    private static String getBrand(BizEvent event) {
        if (
                event.getTransactionDetails() != null &&
                        event.getTransactionDetails().getWallet() != null &&
                        event.getTransactionDetails().getWallet().getInfo() != null &&
                        event.getTransactionDetails().getWallet().getInfo().getBrand() != null
        ) {
            return event.getTransactionDetails().getWallet().getInfo().getBrand();
        }
        return null;
    }

    private static String getBlurredNumber(BizEvent event) {
        if(event.getTransactionDetails() != null && event.getTransactionDetails().getWallet() != null && event.getTransactionDetails().getWallet().getInfo() != null){
            return event.getTransactionDetails().getWallet().getInfo().getBlurredNumber();
        }
        return null;
    }

    private static long getAmount(BizEvent bizEvent) {
        if (bizEvent.getTransactionDetails() != null && bizEvent.getTransactionDetails().getTransaction() != null) {
            return Long.parseLong( formatAmount(bizEvent.getTransactionDetails().getTransaction().getGrandTotal()));
        }
        if (bizEvent.getPaymentInfo() != null && bizEvent.getPaymentInfo().getAmount() != null) {
            return Long.parseLong( bizEvent.getPaymentInfo().getAmount());
        }
        return 0L;
    }

    private static long getFee(BizEvent event) {
        if (
                event.getTransactionDetails() != null &&
                        event.getTransactionDetails().getTransaction() != null &&
                        event.getTransactionDetails().getTransaction().getFee() != 0L
        ) {
            return event.getTransactionDetails().getTransaction().getFee();
        }
        return 0L;
    }

    private static UserDetail getPayer(BizEvent bizEvent) {
        if (bizEvent.getPayer() != null) {
            return UserDetail.builder()
                    .name(bizEvent.getPayer().getFullName())
                    .taxCode(bizEvent.getPayer().getEntityUniqueIdentifierType())
                    .build();
        }
        return UserDetail.builder().build();
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
        return null;
    }

    private static long getItemAmount(BizEvent event) {
        if (event.getPaymentInfo() != null && event.getPaymentInfo().getAmount() != null) {
            return Long.parseLong(event.getPaymentInfo().getAmount());
        }
        return 0L;
    }

    private static String getPayeeTaxCode(BizEvent event) {
        if (event.getCreditor() != null && event.getCreditor().getIdPA() != null) {
            return event.getCreditor().getIdPA();
        }
        return null;
    }

    private static String getRefNumberType(BizEvent event) {
        if (event.getDebtorPosition() != null && event.getDebtorPosition().getModelType() != null) {
            if (event.getDebtorPosition().getModelType().equals(MODEL_TYPE_IUV)) {
                return REF_TYPE_IUV;
            }
            if (event.getDebtorPosition().getModelType().equals(MODEL_TYPE_NOTICE)) {
                return REF_TYPE_NOTICE;
            }
        }
        return null;
    }

    private static String getRefNumberValue(BizEvent event) {
        if (event.getDebtorPosition() != null && event.getDebtorPosition().getModelType() != null) {
            if (event.getDebtorPosition().getModelType().equals(MODEL_TYPE_IUV) && event.getDebtorPosition().getIuv() != null) {
                return event.getDebtorPosition().getIuv();
            }
            if (event.getDebtorPosition().getModelType().equals(MODEL_TYPE_NOTICE) && event.getDebtorPosition().getNoticeNumber() != null) {
                return event.getDebtorPosition().getNoticeNumber();
            }
        }
        return null;
    }

    private static String formatAmount(long grandTotal) {
        BigDecimal amount = new BigDecimal(grandTotal);
        BigDecimal divider = new BigDecimal(100);
        return amount.divide(divider, 2, RoundingMode.UNNECESSARY).toString();
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
}
