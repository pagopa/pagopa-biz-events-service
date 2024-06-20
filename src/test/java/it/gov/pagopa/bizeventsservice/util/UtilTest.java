package it.gov.pagopa.bizeventsservice.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort.Direction;

import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewUser;
import it.gov.pagopa.bizeventsservice.model.filterandorder.Order.TransactionListOrder;

class UtilTest {
	
	@Test
	void checkListSortingByOrderAndDirection()  {
		List<BizEventsViewUser> listOfViewUser = ViewGenerator.generateListOf95MixedBizEventsViewUser();
		List<BizEventsViewUser> mergedListOfViewUser = new ArrayList<>(Util.getMergedListByTID(listOfViewUser));
		Util.getSortedList(mergedListOfViewUser, TransactionListOrder.TRANSACTION_DATE, Direction.ASC);
		Assertions.assertEquals("2024-06-07T11:07:46Z", mergedListOfViewUser.get(0).getTransactionDate()); // first element
		Assertions.assertEquals("2024-06-12T11:07:46Z", mergedListOfViewUser.get(mergedListOfViewUser.size()-1).getTransactionDate()); // last element
		Util.getSortedList(mergedListOfViewUser, TransactionListOrder.TRANSACTION_DATE, Direction.DESC);
		Assertions.assertEquals("2024-06-12T11:07:46Z", mergedListOfViewUser.get(0).getTransactionDate()); // first element
		Assertions.assertEquals("2024-06-07T11:07:46Z", mergedListOfViewUser.get(mergedListOfViewUser.size()-1).getTransactionDate()); // last element
		Util.getSortedList(mergedListOfViewUser, TransactionListOrder.TAX_CODE, Direction.ASC);
		Assertions.assertEquals(ViewGenerator.USER_TAX_CODE_WITH_TX, mergedListOfViewUser.get(0).getTaxCode()); // first element
		Assertions.assertEquals(ViewGenerator.USER_TAX_CODE_WITH_TX, mergedListOfViewUser.get(mergedListOfViewUser.size()-1).getTaxCode()); // last element
		Util.getSortedList(mergedListOfViewUser, TransactionListOrder.TAX_CODE, Direction.DESC);
		Assertions.assertEquals(ViewGenerator.USER_TAX_CODE_WITH_TX, mergedListOfViewUser.get(0).getTaxCode()); // first element
		Assertions.assertEquals(ViewGenerator.USER_TAX_CODE_WITH_TX, mergedListOfViewUser.get(mergedListOfViewUser.size()-1).getTaxCode()); // last element
    }

}
