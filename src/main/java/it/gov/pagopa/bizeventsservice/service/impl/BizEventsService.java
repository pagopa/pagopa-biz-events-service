package it.gov.pagopa.bizeventsservice.service.impl;

import com.azure.cosmos.models.PartitionKey;
import it.gov.pagopa.bizeventsservice.entity.BizEvent;
import it.gov.pagopa.bizeventsservice.exception.AppError;
import it.gov.pagopa.bizeventsservice.exception.AppException;
import it.gov.pagopa.bizeventsservice.model.response.CtReceiptModelResponse;
import it.gov.pagopa.bizeventsservice.repository.BizEventsRepository;
import it.gov.pagopa.bizeventsservice.service.IBizEventsService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BizEventsService implements IBizEventsService {

    private final BizEventsRepository bizEventsRepository;

    private final ModelMapper modelMapper;

    @Autowired
    public BizEventsService(BizEventsRepository bizEventsRepository, ModelMapper modelMapper) {
        this.bizEventsRepository = bizEventsRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public CtReceiptModelResponse getOrganizationReceipt(String organizationFiscalCode,
            String iur, String iuv) {
        // get biz event
        List<BizEvent> bizEventEntityList = bizEventsRepository.getBizEventByOrgFiscCodeAndIur(organizationFiscalCode,
                iur, iuv);

        if (bizEventEntityList.isEmpty()) {
            throw new AppException(AppError.BIZ_EVENT_NOT_FOUND, organizationFiscalCode, iur, iuv);
        }
        // the query should always return only one element
        else if (bizEventEntityList.size() > 1) {
            throw new AppException(AppError.BIZ_EVENT_NOT_UNIQUE, organizationFiscalCode, iur, iuv);
        }

        // the bizEventEntityList has only one element
        return modelMapper.map(bizEventEntityList.get(0), CtReceiptModelResponse.class);
    }

    @Override
    public BizEvent getBizEvent(String id) {
        // get biz event
        Optional<BizEvent> optionalBizEvent = bizEventsRepository.findById(id, new PartitionKey(id));

        if (optionalBizEvent.isEmpty()) {
            throw new AppException(AppError.BIZ_EVENT_NOT_FOUND_WITH_ID, id);
        }
        return optionalBizEvent.get();
    }

    @Override
    public BizEvent getBizEventByOrgFiscalCodeAndIuv(String organizationFiscalCode, String iuv) {
        // get biz event
        List<BizEvent> bizEventEntityList = bizEventsRepository.getBizEventByOrgFiscalCodeAndIuv(organizationFiscalCode,
                iuv);

        if (bizEventEntityList.isEmpty()) {
            throw new AppException(AppError.BIZ_EVENT_NOT_FOUND_WITH_ORG_CF_AND_IUV, organizationFiscalCode, iuv);
        }
        // the query should always return only one element
        else if (bizEventEntityList.size() > 1) {
            throw new AppException(AppError.BIZ_EVENT_NOT_UNIQUE_WITH_ORG_CF_AND_IUV, organizationFiscalCode, iuv);
        }
        return bizEventEntityList.get(0);
    }
}
