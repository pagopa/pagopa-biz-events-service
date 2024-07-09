package it.gov.pagopa.bizeventsservice.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Sort.Direction;

import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewUser;
import it.gov.pagopa.bizeventsservice.model.filterandorder.Order.TransactionListOrder;

public class Util {
	
	private Util() {}
	
	public static List<List<BizEventsViewUser>> getPaginatedList(List<BizEventsViewUser> mergedListByTIDOfViewUser, Boolean isPayer, Boolean isDebtor, 
			int pageSize, TransactionListOrder order, Direction direction) {
		List<BizEventsViewUser> filteredListOfViewUser = new ArrayList<>(Util.getFilteredList(mergedListByTIDOfViewUser, isPayer, isDebtor));
		Util.getSortedList(filteredListOfViewUser, order, direction);		
        return Util.getPages(filteredListOfViewUser, pageSize);
	}

	
	public static List<BizEventsViewUser> getMergedListByTID(List<BizEventsViewUser> fullListOfViewUser) {
		Set<String> set = new HashSet<>(fullListOfViewUser.size());
    	// sorting based on the isDebtor field (true first) and then grouping by transactionId (the cart case requires that only one item be taken from those present)
		return fullListOfViewUser.stream()
        		.sorted(Comparator.comparing(BizEventsViewUser::getIsDebtor,Comparator.reverseOrder()))
        		.filter(p -> set.add(p.getTransactionId())).toList();
	}
	
    public static <T> List<List<T>> getPages(Collection<T> c, Integer pageSize) {
        
    	List<T> list = new ArrayList<>(c);
        
    	if (pageSize == null || pageSize <= 0 || pageSize > list.size()) { 
    		pageSize = list.size(); 
    	}
    	
        int numPages = (int) Math.ceil((double)list.size() / (double)pageSize);
        List<List<T>> pages = new ArrayList<>(numPages);
        for (int pageNum = 0; pageNum < numPages; pageNum++) {
        	int toIndex = pageNum;
        	// subList() method is an instance of 'RandomAccessSubList' which is not serializable --> create a new ArrayList which is
            pages.add(new ArrayList<>(list.subList(pageNum * pageSize, Math.min(++toIndex * pageSize, list.size()))));
        }
        return pages;
    }
    
	public static void getSortedList(List<BizEventsViewUser> listToSort, TransactionListOrder order,
			Direction direction) {
		if (TransactionListOrder.TRANSACTION_DATE.equals(order)) {
			switch (direction) {
			case ASC:
				Collections.sort(listToSort, Comparator.comparing(BizEventsViewUser::getTransactionDateAsLocalDateTime,
						Comparator.nullsLast(Comparator.naturalOrder())));
				break;
			case DESC:
			default:
				Collections.sort(listToSort, Comparator.comparing(BizEventsViewUser::getTransactionDateAsLocalDateTime,
						Comparator.nullsLast(Comparator.naturalOrder())).reversed());
				break;
			}
		} else {
			// the default sorting is by transaction date and DESC direction
			Collections.sort(listToSort, Comparator
					.comparing(BizEventsViewUser::getTransactionDateAsLocalDateTime, Comparator.nullsLast(Comparator.naturalOrder()))
					.reversed());
		}
	}
	
	public static List<BizEventsViewUser> getFilteredList(List<BizEventsViewUser> listToFilter, Boolean isPayer, Boolean isDebtor) {
		return listToFilter.stream()
		        .filter(u -> (isPayer == null || u.getIsPayer().equals(isPayer)) && (isDebtor == null || u.getIsDebtor().equals(isDebtor)))
		        .toList();
	}
}
