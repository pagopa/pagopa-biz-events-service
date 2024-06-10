package it.gov.pagopa.bizeventsservice.service.impl;

import com.azure.spring.data.cosmos.core.query.CosmosPageRequest;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewCart;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewGeneral;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewUser;
import it.gov.pagopa.bizeventsservice.exception.AppError;
import it.gov.pagopa.bizeventsservice.exception.AppException;
import it.gov.pagopa.bizeventsservice.mapper.ConvertViewsToTransactionDetailResponse;
import it.gov.pagopa.bizeventsservice.model.PageInfo;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionDetailResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListItem;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListResponse;
import it.gov.pagopa.bizeventsservice.repository.BizEventsViewCartRepository;
import it.gov.pagopa.bizeventsservice.repository.BizEventsViewGeneralRepository;
import it.gov.pagopa.bizeventsservice.repository.BizEventsViewUserRepository;
import it.gov.pagopa.bizeventsservice.repository.redis.RedisRepository;
import it.gov.pagopa.bizeventsservice.service.ITransactionService;

import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TransactionService implements ITransactionService {

    private final BizEventsViewGeneralRepository bizEventsViewGeneralRepository;
    private final BizEventsViewCartRepository bizEventsViewCartRepository;
    private final BizEventsViewUserRepository bizEventsViewUserRepository;
    private final RedisRepository redisRepository;
    
    @Value("${spring.redis.ttl}")
    private long redisTTL;

    @Autowired
    public TransactionService(BizEventsViewGeneralRepository bizEventsViewGeneralRepository, BizEventsViewCartRepository bizEventsViewCartRepository, 
    		BizEventsViewUserRepository bizEventsViewUserRepository, RedisRepository redisRepository) {
        this.bizEventsViewGeneralRepository = bizEventsViewGeneralRepository;
        this.bizEventsViewCartRepository = bizEventsViewCartRepository;
        this.bizEventsViewUserRepository = bizEventsViewUserRepository;
        this.redisRepository = redisRepository;
    }

    @Override
    public TransactionListResponse getTransactionList(
            String taxCode, String continuationToken, Integer size) {
        List<TransactionListItem> listOfTransactionListItem = new ArrayList<>();

        final Sort sort = Sort.by(Sort.Direction.DESC, "transactionDate");
        final CosmosPageRequest pageRequest = new CosmosPageRequest(0, size, continuationToken, sort);
        final Page<BizEventsViewUser> page = this.bizEventsViewUserRepository.getBizEventsViewUserByTaxCode(taxCode, pageRequest);
        Set<String> set = new HashSet<>(page.getContent().size());
        List<BizEventsViewUser> listOfViewUser = page.getContent().stream()
        		.sorted(Comparator.comparing(BizEventsViewUser::getIsDebtor,Comparator.reverseOrder()))
        		.filter(p -> set.add(p.getTransactionId())).toList();

        if(listOfViewUser.isEmpty()){
            throw new AppException(AppError.VIEW_USER_NOT_FOUND_WITH_TAX_CODE, taxCode);
        }
        for (BizEventsViewUser viewUser : listOfViewUser) {
            List<BizEventsViewCart> listOfViewCart;
            if(Boolean.TRUE.equals(viewUser.getIsPayer())){
                listOfViewCart = this.bizEventsViewCartRepository.getBizEventsViewCartByTransactionId(viewUser.getTransactionId());
            } else {
                listOfViewCart = this.bizEventsViewCartRepository.getBizEventsViewCartByTransactionIdAndFilteredByTaxCode(viewUser.getTransactionId(), taxCode);
            }

            if (!listOfViewCart.isEmpty()) {
                TransactionListItem transactionListItem = ConvertViewsToTransactionDetailResponse.convertTransactionListItem(viewUser, listOfViewCart);
                listOfTransactionListItem.add(transactionListItem);
            }
        }

        CosmosPageRequest pageResponse = (CosmosPageRequest) page.getPageable().next();
        String nextToken = pageResponse.getRequestContinuation();

        return TransactionListResponse.builder()
                .transactionList(listOfTransactionListItem)
                .continuationToken(nextToken)
                .build();
    }
    
    @Override
	public TransactionListResponse getCachedTransactionList(String taxCode, Integer page, Integer size) {
    	
    	List<TransactionListItem> listOfTransactionListItem = new ArrayList<>();
        
        List<List<BizEventsViewUser>> pagedListOfViewUser = this.getPaginatedList(taxCode, size);
            
        for (BizEventsViewUser viewUser : pagedListOfViewUser.get(0)) {
            List<BizEventsViewCart> listOfViewCart;
            if(Boolean.TRUE.equals(viewUser.getIsPayer())){
                listOfViewCart = this.bizEventsViewCartRepository.getBizEventsViewCartByTransactionId(viewUser.getTransactionId());
            } else {
                listOfViewCart = this.bizEventsViewCartRepository.getBizEventsViewCartByTransactionIdAndFilteredByTaxCode(viewUser.getTransactionId(), taxCode);
            }

            if (!listOfViewCart.isEmpty()) {
                TransactionListItem transactionListItem = ConvertViewsToTransactionDetailResponse.convertTransactionListItem(viewUser, listOfViewCart);
                listOfTransactionListItem.add(transactionListItem);
            }
        }

        return TransactionListResponse.builder()
                .transactionList(listOfTransactionListItem)
                .pageInfo(PageInfo.builder()
                		.limit(size)
                		.page(page)
                		.itemsFound(pagedListOfViewUser.stream().mapToInt(i -> i.size()).sum())
                		.totalPages(pagedListOfViewUser.size())
                		.build())
                .build();
	}

    @Override
    public TransactionDetailResponse getTransactionDetails(String taxCode, String eventReference) {
        List<BizEventsViewGeneral> bizEventsViewGeneral = this.bizEventsViewGeneralRepository.findByTransactionId(eventReference);
        if (bizEventsViewGeneral.isEmpty()) {
            throw new AppException(AppError.VIEW_GENERAL_NOT_FOUND_WITH_TRANSACTION_ID, eventReference);
        }

        List<BizEventsViewCart> listOfCartViews;
        if(bizEventsViewGeneral.get(0).getPayer() != null && bizEventsViewGeneral.get(0).getPayer().getTaxCode().equals(taxCode)){
            listOfCartViews = this.bizEventsViewCartRepository.getBizEventsViewCartByTransactionId(eventReference);
        } else {
            listOfCartViews = this.bizEventsViewCartRepository.getBizEventsViewCartByTransactionIdAndFilteredByTaxCode(eventReference, taxCode);
        }
        if (listOfCartViews.isEmpty()) {
            throw new AppException(AppError.VIEW_CART_NOT_FOUND_WITH_TRANSACTION_ID_AND_TAX_CODE, eventReference);
        }

        return ConvertViewsToTransactionDetailResponse.convertTransactionDetails(taxCode, bizEventsViewGeneral.get(0), listOfCartViews);
    }

    @Override
    public void disableTransaction(String fiscalCode, String transactionId) {
        List<BizEventsViewUser> listOfViewUser = this.bizEventsViewUserRepository
                .getBizEventsViewUserByTaxCodeAndTransactionId(fiscalCode, transactionId);
        if (listOfViewUser.size() != 1) {
            throw new AppException(AppError.VIEW_USER_NOT_FOUND_WITH_TRANSACTION_ID, fiscalCode, transactionId);
        }
        BizEventsViewUser bizEventsViewUser = listOfViewUser.get(0);
        bizEventsViewUser.setHidden(true);
        bizEventsViewUserRepository.save(bizEventsViewUser);
    }
    
    private List<List<BizEventsViewUser>> getPaginatedList (String taxCode, Integer size) {
    	List<List<BizEventsViewUser>> pagedListOfViewUser = null;
    	byte [] data;
    	// read from the REDIS cache for the paginated list
    	if ((data=redisRepository.get(taxCode)) != null){
    		pagedListOfViewUser = SerializationUtils.deserialize(data);
        } else {
        	List<BizEventsViewUser> fullListOfViewUser = this.bizEventsViewUserRepository.getBizEventsViewUserByTaxCode(taxCode);
        	if(CollectionUtils.isEmpty(fullListOfViewUser)){
               throw new AppException(AppError.VIEW_USER_NOT_FOUND_WITH_TAX_CODE, taxCode);
            }
        	Set<String> set = new HashSet<>(fullListOfViewUser.size());
            List<BizEventsViewUser> mergedListByTIDOfViewUser = fullListOfViewUser.stream()
            		.sorted(Comparator.comparing(BizEventsViewUser::getIsDebtor,Comparator.reverseOrder()))
            		.filter(p -> set.add(p.getTransactionId())).toList();
            pagedListOfViewUser = TransactionService.getPages(mergedListByTIDOfViewUser, size);
            // write in the REDIS cache the paginated list
        	redisRepository.save(taxCode, SerializationUtils.serialize((Serializable)pagedListOfViewUser), redisTTL);
        }
    	return pagedListOfViewUser;
    }
    
    private static <T> List<List<T>> getPages(Collection<T> c, Integer pageSize) {
        
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
