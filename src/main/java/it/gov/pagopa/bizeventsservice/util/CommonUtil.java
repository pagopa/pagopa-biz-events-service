package it.gov.pagopa.bizeventsservice.util;

import java.util.Optional;

public class CommonUtil {

	private CommonUtil() {
	}

	/**
	 * @param value value to deNullify.
	 * @return return empty string if value is null
	 */
	public static String deNull(Object value) {
		return Optional.ofNullable(value).orElse("").toString();
	}
}