package it.gov.pagopa.bizeventsservice.service.impl;

import com.azure.spring.data.cosmos.core.query.CosmosPageRequest;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewCart;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewGeneral;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewUser;
import it.gov.pagopa.bizeventsservice.exception.AppError;
import it.gov.pagopa.bizeventsservice.exception.AppException;
import it.gov.pagopa.bizeventsservice.mapper.ConvertViewsToTransactionDetailResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionDetailResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListItem;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListResponse;
import it.gov.pagopa.bizeventsservice.repository.BizEventsViewCartRepository;
import it.gov.pagopa.bizeventsservice.repository.BizEventsViewGeneralRepository;
import it.gov.pagopa.bizeventsservice.repository.BizEventsViewUserRepository;
import it.gov.pagopa.bizeventsservice.service.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TransactionService implements ITransactionService {

    private final BizEventsViewGeneralRepository bizEventsViewGeneralRepository;
    private final BizEventsViewCartRepository bizEventsViewCartRepository;
    private final BizEventsViewUserRepository bizEventsViewUserRepository;

    @Autowired
    public TransactionService(BizEventsViewGeneralRepository bizEventsViewGeneralRepository, BizEventsViewCartRepository bizEventsViewCartRepository, BizEventsViewUserRepository bizEventsViewUserRepository) {
        this.bizEventsViewGeneralRepository = bizEventsViewGeneralRepository;
        this.bizEventsViewCartRepository = bizEventsViewCartRepository;
        this.bizEventsViewUserRepository = bizEventsViewUserRepository;
    }

    @Override
    public TransactionListResponse getTransactionList(
            String taxCode, String continuationToken, Integer size) {
        List<TransactionListItem> listOfTransactionListItem = new ArrayList<>();

        final Sort sort = Sort.by(Sort.Direction.DESC, "transactionDate");
        final CosmosPageRequest pageRequest = new CosmosPageRequest(0, size, continuationToken, sort);
        final Page<BizEventsViewUser> page = this.bizEventsViewUserRepository.getBizEventsViewUserByTaxCode(taxCode, pageRequest);
        Set<String> set = new HashSet<>(page.getContent().size());
        List<BizEventsViewUser> listOfViewUser = page.getContent().stream().filter(p -> set.add(p.getTransactionId())).toList();

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
            //TODO handle error in case a transaction is invalid (empty)
        }

        CosmosPageRequest pageResponse = (CosmosPageRequest) page.getPageable().next();
        String nextToken = pageResponse.getRequestContinuation();

        return TransactionListResponse.builder()
                .transactionList(listOfTransactionListItem)
                .continuationToken(nextToken)
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

        return ConvertViewsToTransactionDetailResponse.convertTransactionDetails(bizEventsViewGeneral.get(0), listOfCartViews);
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
}
