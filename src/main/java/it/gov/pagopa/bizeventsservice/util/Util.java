package it.gov.pagopa.bizeventsservice.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewUser;

public class Util {
	
	private Util() {}
	
	public static List<List<BizEventsViewUser>> getPaginatedList(List<BizEventsViewUser> fullListOfViewUser, int pageSize) {
		Set<String> set = new HashSet<>(fullListOfViewUser.size());
    	// sorting based on the isDebtor field (true first) and then grouping by transactionId (the cart case requires that only one item be taken from those present)
        List<BizEventsViewUser> mergedListByTIDOfViewUser = fullListOfViewUser.stream()
        		.sorted(Comparator.comparing(BizEventsViewUser::getIsDebtor,Comparator.reverseOrder()))
        		.filter(p -> set.add(p.getTransactionId())).collect(Collectors.toList());
        
        // sorting by transactionDate as per business request
        Collections.sort(mergedListByTIDOfViewUser, 
        	     Comparator.comparing(BizEventsViewUser::getTransactionDate, 
        	        Comparator.nullsLast(Comparator.naturalOrder()))
        	     .reversed());
        
        return Util.getPages(mergedListByTIDOfViewUser, pageSize);
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
}
