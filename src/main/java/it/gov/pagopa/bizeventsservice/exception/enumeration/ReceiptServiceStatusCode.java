package it.gov.pagopa.bizeventsservice.exception.enumeration;

import lombok.Getter;

@Getter
public enum ReceiptServiceStatusCode {
    PDFS_706("PDFS_706", "Fiscal code not authorized to access the requested receipt document"),

    PDFS_714("PDFS_714", "The PDF has not been generated yet."),
    PDFS_715("PDFS_715", "The PDF generation failed. A retry is possible."),
    PDFS_716("PDFS_716", "The PDF generation failed. Manual review is required."),

    PDFS_800("PDFS_800", "Receipt not found with the provided third party id"),
    PDFS_801("PDFS_801", "Cart not found with the provided third party id");

    private final String errorCode;
    private final String errorMessage;

    ReceiptServiceStatusCode(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
