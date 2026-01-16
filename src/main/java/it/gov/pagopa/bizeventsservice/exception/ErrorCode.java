package it.gov.pagopa.bizeventsservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // --- BIZ EVENT ---
    BZ_404_001("BZ_404_001", "NOT FOUND", "biz event", "Biz Event not found with IUR and IUV"),
    BZ_404_002("BZ_404_002", "NOT FOUND", "biz event", "Biz Event not found with IUR"),
    BZ_404_003("BZ_404_003", "NOT FOUND", "biz event", "Biz Event not found with ID"),
    BZ_404_004("BZ_404_004", "NOT FOUND", "biz event", "Biz Event not found with CF and IUV"),
    BZ_422_001("BZ_422_001", "Unprocessable Entity", "biz event", "Multiple BizEvents found with IUR and IUV"),
    BZ_422_002("BZ_422_002", "Unprocessable Entity", "biz event", "Multiple BizEvents found with CF and IUR"),
    BZ_422_003("BZ_422_003", "Unprocessable Entity", "biz event", "Multiple BizEvents found with CF and IUV"),

    // --- GENERIC ---
    GN_400_001("GN_400_001", "BAD REQUEST", "generic", "-"),
    GN_400_002("GN_400_002", "BAD REQUEST", "generic", "Invalid input"),
    GN_400_003("GN_400_003", "BAD REQUEST", "generic", "Invalid CF (Tax Code)"),
    GN_400_004("GN_400_004", "BAD REQUEST", "generic", "Invalid input type"),
    GN_400_005("GN_400_005", "BAD REQUEST", "generic", "Invalid input parameter constraints"),
    GN_500_001("GN_500_001", "Internal Server Error", "generic", "Generic Error"),
    GN_500_002("GN_500_002", "Internal Server Error", "generic", "Generic Error"),
    GN_500_003("GN_500_003", "Internal Server Error", "generic", "Generic Error"),
    GN_500_004("GN_500_004", "Internal Server Error", "generic", "Generic Error"),

    // --- FEIGN CLIENT ---
    FG_000_001("FG_000_001", "Variable", "feign client", "Error occurred during call to underlying services"),

    // --- VIEW USER ---
    VU_404_001("VU_404_001", "NOT FOUND", "view user", "View User not found with CF"),
    VU_404_002("VU_404_002", "NOT FOUND", "view user", "View User not found with CF and filters"),
    VU_404_003("VU_404_003", "NOT FOUND", "view user", "View User not found with ID"),

    // --- VIEW GENERAL ---
    VG_404_001("VG_404_001", "NOT FOUND", "view general", "View General not found with ID"),

    // --- VIEW CART ---
    VC_404_001("VC_404_001", "NOT FOUND", "view cart", "View Cart not found with ID and CF"),

    // --- ATTACHMENT ---
    AT_404_001("AT_404_001", "NOT FOUND", "attachment", "Attachment not found"),
    AT_404_002("AT_404_002", "NOT FOUND", "attachment", "Attachment not found because it is currently being generated"),

    // --- UNKNOWN ---
    UN_500_000("UN_500_000", "Internal Server Error", "unknown", "Unexpected error"),

    TS_000_000("TS_000_000", "test", "test", "used for testing");

    private final String code;
    private final String group;
    private final String domain;
    private final String description;

}