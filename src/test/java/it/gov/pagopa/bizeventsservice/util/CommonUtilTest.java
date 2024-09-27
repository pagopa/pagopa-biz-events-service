package it.gov.pagopa.bizeventsservice.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CommonUtilTest {
	
	@Test
	void deNullTest() {
		assertEquals("",CommonUtil.deNull(null));
		assertEquals("String",CommonUtil.deNull("String"));
	}

}
