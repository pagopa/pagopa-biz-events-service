package it.gov.pagopa.bizeventsservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static it.gov.pagopa.bizeventsservice.util.Constants.BIZ_NOT_FOUND_HEADER;
import static it.gov.pagopa.bizeventsservice.util.Constants.BIZ_NOT_UNIQUE_HEADER;


@Getter
public enum AppError {
	BIZ_EVENT_NOT_FOUND_IUV_IUR(HttpStatus.NOT_FOUND, BIZ_NOT_FOUND_HEADER, "Not found a biz event for the Organization Fiscal Code %s and IUR %s and IUV %s"),
    BIZ_EVENT_NOT_FOUND_IUR(HttpStatus.NOT_FOUND, BIZ_NOT_FOUND_HEADER, "Not found a biz event for the Organization Fiscal Code %s and IUR %s"),
	BIZ_EVENT_NOT_FOUND_WITH_ID(HttpStatus.NOT_FOUND, BIZ_NOT_FOUND_HEADER, "Not found a biz event with id %s"),
	BIZ_EVENT_NOT_FOUND_WITH_ORG_CF_AND_IUV(HttpStatus.NOT_FOUND, BIZ_NOT_FOUND_HEADER, "Not found a biz event for the Organization Fiscal Code %s and IUV %s"),
	BIZ_EVENT_NOT_UNIQUE_IUV_IUR(HttpStatus.UNPROCESSABLE_ENTITY, BIZ_NOT_UNIQUE_HEADER, "More than one biz event was found for the Organization Fiscal Code %s and IUR %s and IUV %s"),
    BIZ_EVENT_NOT_UNIQUE_IUR(HttpStatus.UNPROCESSABLE_ENTITY, BIZ_NOT_UNIQUE_HEADER, "More than one biz event was found for the Organization Fiscal Code %s and IUR %s"),
	BIZ_EVENT_NOT_UNIQUE_WITH_ORG_CF_AND_IUV(HttpStatus.UNPROCESSABLE_ENTITY, BIZ_NOT_UNIQUE_HEADER, "More than one biz event was found for the Organization Fiscal Code %s and IUV %s"),
    INVALID_FISCAL_CODE(HttpStatus.BAD_REQUEST, "Bad Request", "The given fiscal code %s is not a valid fiscal code"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "Something was wrong");

    public final HttpStatus httpStatus;
    public final String title;
    public final String details;


    AppError(HttpStatus httpStatus, String title, String details) {
        this.httpStatus = httpStatus;
        this.title = title;
        this.details = details;
    }
}


