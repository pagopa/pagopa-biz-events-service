package it.gov.pagopa.bizeventsservice.service.impl;

import it.gov.pagopa.bizeventsservice.entity.BizEvent;
import it.gov.pagopa.bizeventsservice.exception.AppError;
import it.gov.pagopa.bizeventsservice.exception.AppException;
import it.gov.pagopa.bizeventsservice.mapper.ConvertBizEventListToTransactionDetailResponse;
import it.gov.pagopa.bizeventsservice.model.transaction.TransactionDetailResponse;
import it.gov.pagopa.bizeventsservice.repository.BizEventsRepository;
import it.gov.pagopa.bizeventsservice.service.ITransactionService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransactionService implements ITransactionService {


    private final BizEventsRepository bizEventsRepository;

    @Autowired
    public TransactionService(BizEventsRepository bizEventsRepository) {
        this.bizEventsRepository = bizEventsRepository;
    }

    @Override
    public TransactionDetailResponse getTransactionDetails(String fiscalCode, boolean isCart, String eventReference){
        List<BizEvent> bizEventEntityList;
        if(!isValidFiscalCode(fiscalCode)){
            throw new AppException(AppError.INVALID_FISCAL_CODE, fiscalCode);
        }

        if(isCart){
            bizEventEntityList = this.bizEventsRepository.getBizEventByFiscalCodeAndTransactionId(eventReference, fiscalCode);
        } else {
            bizEventEntityList = this.bizEventsRepository.getBizEventByFiscalCodeAndId(eventReference, fiscalCode);
        }
        if (bizEventEntityList.isEmpty()) {
            throw new AppException(AppError.BIZ_EVENT_NOT_FOUND_WITH_ID, eventReference);
        }

        return ConvertBizEventListToTransactionDetailResponse.convert(bizEventEntityList);
    }

    private boolean isValidFiscalCode(String fiscalCode) {
        if (fiscalCode != null && !fiscalCode.isEmpty()) {
            Pattern pattern = Pattern.compile("^[A-Z]{6}[0-9LMNPQRSTUV]{2}[ABCDEHLMPRST][0-9LMNPQRSTUV]{2}[A-Z][0-9LMNPQRSTUV]{3}[A-Z]$");
            Matcher matcher = pattern.matcher(fiscalCode);
            return matcher.find();
        }
        return false;
    }

}
