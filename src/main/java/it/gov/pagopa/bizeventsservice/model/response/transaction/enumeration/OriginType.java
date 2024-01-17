package it.gov.pagopa.bizeventsservice.model.response.transaction.enumeration;

import java.util.Arrays;

public enum OriginType {
    INTERNAL, PM, NDP001PROD , NDP002PROD, NDP003PROD, UNKNOWN;

    public static boolean isValidOrigin(String origin) {
        return Arrays.stream(values()).anyMatch(it -> it.name().equalsIgnoreCase(origin));
    }
}
