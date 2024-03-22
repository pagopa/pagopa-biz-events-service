package it.gov.pagopa.bizeventsservice.util;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateValidator {
	
	private DateValidator() {}

	public static boolean isValid(final String value, final String datePattern) {
		try {
			LocalDateTime.parse(value, DateTimeFormatter.ofPattern(datePattern));
		} catch (DateTimeException e) {
			return false;
		}
		return true;
	}
}
