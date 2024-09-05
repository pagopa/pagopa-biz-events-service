package it.gov.pagopa.bizeventsservice.util;

import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewUser;
import it.gov.pagopa.bizeventsservice.model.filterandorder.Order.TransactionListOrder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort.Direction;

import java.util.ArrayList;
import java.util.List;

class UtilTest {

    @Test
    void checkListSortingByOrderAndDirection() {
        List<BizEventsViewUser> listOfViewUser = ViewGenerator.generateListOf95MixedBizEventsViewUser();
        List<BizEventsViewUser> mergedListOfViewUser = new ArrayList<>(Util.getMergedListByTID(listOfViewUser));
        Util.getSortedList(mergedListOfViewUser, TransactionListOrder.TRANSACTION_DATE, Direction.ASC);
        Assertions.assertEquals("2024-06-07T11:07:46Z", mergedListOfViewUser.get(0).getTransactionDate()); // first element
        Assertions.assertEquals("2024-06-12T11:07:46Z", mergedListOfViewUser.get(mergedListOfViewUser.size() - 1).getTransactionDate()); // last element
        Util.getSortedList(mergedListOfViewUser, TransactionListOrder.TRANSACTION_DATE, Direction.DESC);
        Assertions.assertEquals("2024-06-12T11:07:46Z", mergedListOfViewUser.get(0).getTransactionDate()); // first element
        Assertions.assertEquals("2024-06-07T11:07:46Z", mergedListOfViewUser.get(mergedListOfViewUser.size() - 1).getTransactionDate()); // last element
    }

}
