package it.gov.pagopa.bizeventsservice.model.response.transaction.enumeration;

import java.util.Arrays;

public enum PaymentMethodType {

    BBT, BP, AD, CP, PO, OBEP, JIF, MYBK, PPAL, UNKNOWN;

    public static boolean isValidPaymentMethod(String origin) {
        return Arrays.stream(values()).anyMatch(it -> it.name().equalsIgnoreCase(origin));
    }
}
