package it.gov.pagopa.bizeventsservice.mapper;

import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewCart;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewGeneral;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewUser;
import it.gov.pagopa.bizeventsservice.model.response.paidnotice.InfoNotice;
import it.gov.pagopa.bizeventsservice.model.response.paidnotice.NoticeDetailResponse;
import it.gov.pagopa.bizeventsservice.model.response.paidnotice.NoticeListItem;
import it.gov.pagopa.bizeventsservice.model.response.transaction.*;
import it.gov.pagopa.bizeventsservice.util.DateValidator;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class ConvertViewsToTransactionDetailResponse {
    private static final List<String> LIST_RECEIPT_DATE_FORMAT_IN = List.of("yyyy-MM-dd'T'HH:mm:ss");
    private static final String RECEIPT_DATE_FORMAT_OUT = "yyyy-MM-dd'T'HH:mm:ssX";

    private ConvertViewsToTransactionDetailResponse() {
    }

    public static TransactionDetailResponse convertTransactionDetails(String taxCode, BizEventsViewGeneral bizEventsViewGeneral, List<BizEventsViewCart> listOfCartViews) {
        List<CartItem> listOfCartItems = new ArrayList<>();
        AtomicReference<BigDecimal> totalAmount = new AtomicReference<>(BigDecimal.ZERO);

        for (BizEventsViewCart bizEventsViewCart : listOfCartViews) {

            listOfCartItems.add(
                    CartItem.builder()
                            .subject(bizEventsViewCart.getSubject())
                            .amount(new BigDecimal(bizEventsViewCart.getAmount()).setScale(2, RoundingMode.UNNECESSARY).toString())
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
                                .walletInfo(isDebtor ? null : bizEventsViewGeneral.getWalletInfo())
                                .payer(isDebtor ? null : bizEventsViewGeneral.getPayer())
                                .amount(totalAmount.get().setScale(2, RoundingMode.UNNECESSARY).toString())
                                .fee(StringUtils.isNotEmpty(bizEventsViewGeneral.getFee()) ? bizEventsViewGeneral.getFee().replace(',', '.') : bizEventsViewGeneral.getFee())
                                .paymentMethod(isDebtor ? null : bizEventsViewGeneral.getPaymentMethod())
                                .origin(bizEventsViewGeneral.getOrigin())
                                .build()
                )
                .carts(listOfCartItems)
                .build();
    }

    public static NoticeDetailResponse convertPaidNoticeDetails(String taxCode, BizEventsViewGeneral bizEventsViewGeneral, List<BizEventsViewCart> listOfCartViews) {
        List<it.gov.pagopa.bizeventsservice.model.response.paidnotice.CartItem> listOfCartItems = new ArrayList<>();
        AtomicReference<BigDecimal> totalAmount = new AtomicReference<>(BigDecimal.ZERO);

        for (BizEventsViewCart bizEventsViewCart : listOfCartViews) {

            listOfCartItems.add(
                    it.gov.pagopa.bizeventsservice.model.response.paidnotice.CartItem.builder()
                            .subject(bizEventsViewCart.getSubject())
                            .amount(new BigDecimal(bizEventsViewCart.getAmount()).setScale(2, RoundingMode.UNNECESSARY).toString())
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
        return NoticeDetailResponse.builder()
                .infoNotice(
                        InfoNotice.builder()
                                .eventId(bizEventsViewGeneral.getTransactionId())
                                .authCode(bizEventsViewGeneral.getAuthCode())
                                .rrn(bizEventsViewGeneral.getRrn())
                                .noticeDate(dateFormatZoned(bizEventsViewGeneral.getTransactionDate()))
                                .pspName(bizEventsViewGeneral.getPspName())
                                .walletInfo(isDebtor ? null : bizEventsViewGeneral.getWalletInfo())
                                .payer(isDebtor ? null : bizEventsViewGeneral.getPayer())
                                .amount(totalAmount.get().setScale(2, RoundingMode.UNNECESSARY).toString())
                                .fee(StringUtils.isNotEmpty(bizEventsViewGeneral.getFee()) ? bizEventsViewGeneral.getFee().replace(',', '.') : bizEventsViewGeneral.getFee())
                                .paymentMethod(isDebtor ? null : bizEventsViewGeneral.getPaymentMethod())
                                .origin(bizEventsViewGeneral.getOrigin())
                                .build()
                )
                .carts(listOfCartItems)
                .build();
    }

    public static List<NoticeListItem> convertToNoticeList(TransactionListResponse transactionListResponse) {
        return transactionListResponse.getTransactionList().stream()
                .map(elem -> NoticeListItem.builder()
                        .eventId(elem.getTransactionId())
                        .payeeName(elem.getPayeeName())
                        .payeeTaxCode(elem.getPayeeTaxCode())
                        .amount(elem.getAmount())
                        .noticeDate(elem.getTransactionDate())
                        .isCart(elem.getIsCart())
                        .isPayer(elem.getIsPayer())
                        .isDebtor(elem.getIsDebtor())
                        .build())
                .toList();
    }

    public static TransactionListItem convertTransactionListItem(BizEventsViewUser viewUser, BizEventsViewCart bizEventsViewCart, BizEventsViewGeneral bizEventsViewGeneral) {
        AtomicReference<BigDecimal> totalAmount = new AtomicReference<>(BigDecimal.ZERO);
        BigDecimal amountExtracted = new BigDecimal(bizEventsViewCart.getAmount());
        totalAmount.updateAndGet(v -> v.add(amountExtracted));

        return TransactionListItem.builder()
                .transactionId(viewUser.getId().substring(0, viewUser.getId().length() - 2)) // eventId stripped of the -d or -p suffix
                .payeeName(bizEventsViewCart.getPayee().getName())
                .payeeTaxCode(bizEventsViewCart.getPayee().getTaxCode())
                .amount(totalAmount.get().setScale(2, RoundingMode.UNNECESSARY).toString()) 
                .transactionDate(dateFormatZoned(viewUser.getTransactionDate()))
                .isCart(bizEventsViewGeneral.getIsCart())
                .isPayer(BooleanUtils.isTrue(viewUser.getIsPayer()))
                .isDebtor(BooleanUtils.isTrue(viewUser.getIsDebtor()))
                .build();
    }
    
    private static String dateFormatZoned(String date) {
        boolean isUtc = date.endsWith("Z");
        int dotIndex = date.lastIndexOf('.');

        // milliseconds removed if present
        String dateSub = (dotIndex != -1) ? date.substring(0, dotIndex) : date;
        // if UTC I add a trailing 'Z' which may have been removed with the millisecond trim
        if (isUtc && !dateSub.endsWith("Z")) {
            dateSub += "Z";
        }

        // If it was already a date in the expected UTC format I return it as it is otherwise it is formatted
        return DateValidator.isValid(dateSub, RECEIPT_DATE_FORMAT_OUT)
                ? dateSub
                : dateFormat(dateSub);
    }

    private static String dateFormat(String date) {
        for (String format : LIST_RECEIPT_DATE_FORMAT_IN) {
            if (DateValidator.isValid(date, format)) {
                LocalDateTime ldt = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(format));
                // Convert from local to UTC
                ZonedDateTime zdt = ldt.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC);
                return DateTimeFormatter.ofPattern(RECEIPT_DATE_FORMAT_OUT).format(zdt);
            }
        }
        throw new DateTimeException("The date [" + date + "] is not in one of the expected formats "
            + LIST_RECEIPT_DATE_FORMAT_IN + " and cannot be parsed");
    }
}
