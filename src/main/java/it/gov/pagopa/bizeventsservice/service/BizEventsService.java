package it.gov.pagopa.bizeventsservice.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.gov.pagopa.bizeventsservice.entity.BizEvent;
import it.gov.pagopa.bizeventsservice.exception.AppError;
import it.gov.pagopa.bizeventsservice.exception.AppException;
import it.gov.pagopa.bizeventsservice.model.response.CtReceiptModelResponse;
import it.gov.pagopa.bizeventsservice.repository.BizEventsRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class BizEventsService {

    @Autowired
    private BizEventsRepository bizEventsRepository;

    @Autowired
    private ModelMapper modelMapper;

    public CtReceiptModelResponse getOrganizationReceipt(String organizationFiscalCode,
                                          String iur) {
        // get biz event
    	List<BizEvent> bizEventEntityList = bizEventsRepository.getBizEventByOrgFiscCodeAndIur(organizationFiscalCode, iur);
    	
    	if (bizEventEntityList.isEmpty()) {
            throw new AppException(AppError.BIZ_EVENT_NOT_FOUND, organizationFiscalCode, iur);
        }
    	// the query should always return only one element
    	else if (bizEventEntityList.size() > 1) {
    		throw new AppException(AppError.BIZ_EVENT_NOT_UNIQUE, organizationFiscalCode, iur);
        }

    	// the bizEventEntityList has only one element
    	return modelMapper.map(bizEventEntityList.get(0), CtReceiptModelResponse.class);
    }

    

}
