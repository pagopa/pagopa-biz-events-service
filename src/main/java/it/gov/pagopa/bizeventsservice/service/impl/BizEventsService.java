package it.gov.pagopa.bizeventsservice.service.impl;

import com.azure.cosmos.models.PartitionKey;
import it.gov.pagopa.bizeventsservice.entity.BizEvent;
import it.gov.pagopa.bizeventsservice.exception.AppError;
import it.gov.pagopa.bizeventsservice.exception.AppException;
import it.gov.pagopa.bizeventsservice.model.response.CtReceiptModelResponse;
import it.gov.pagopa.bizeventsservice.repository.replica.BizEventsRepository;
import it.gov.pagopa.bizeventsservice.service.IBizEventsService;
import it.gov.pagopa.bizeventsservice.util.TransactionIdFactory;
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
        List<BizEvent> bizEventEntityList = bizEventsRepository.getBizEventByOrgFiscCodeIuvAndIur(organizationFiscalCode,
                iur, iuv);

        if (bizEventEntityList.isEmpty()) {
            throw new AppException(AppError.BIZ_EVENT_NOT_FOUND_IUV_IUR, organizationFiscalCode, iur, iuv);
        }
        // the query should always return only one element
        else if (bizEventEntityList.size() > 1) {
            throw new AppException(AppError.BIZ_EVENT_NOT_UNIQUE_IUV_IUR, organizationFiscalCode, iur, iuv);
        }

        // the bizEventEntityList has only one element
        return modelMapper.map(bizEventEntityList.get(0), CtReceiptModelResponse.class);
    }

    @Override
    public BizEvent getBizEvent(String id) {
        Optional<BizEvent> optionalBizEvent;

        if (TransactionIdFactory.isCart(id)) {

            TransactionIdFactory.ViewTransactionId viewTransactionId = TransactionIdFactory.extract(id);
            String cartId = viewTransactionId.transactionId();

            boolean isDebtor = viewTransactionId.eventId() != null;

            if (isDebtor) {
                // is a debtor cart biz event, so get by biz event id
                String bizId = viewTransactionId.eventId();
                optionalBizEvent = bizEventsRepository.findById(bizId, new PartitionKey(bizId));

            } else {
                // is a payer cart biz event, so get by cart id because biz event id is not available
                optionalBizEvent = bizEventsRepository.findByCartId(cartId).stream().findFirst();
            }
        } else {
            // is a single payment, so get biz event by id
            optionalBizEvent = bizEventsRepository.findById(id, new PartitionKey(id));
        }

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

    @Override
    public CtReceiptModelResponse getOrganizationReceipt(String organizationFiscalCode,
                                                         String iur) {
        // get biz event
        List<BizEvent> bizEventEntityList = bizEventsRepository.getBizEventByOrgFiscCodeAndIur(organizationFiscalCode,
                iur);

        if (bizEventEntityList.isEmpty()) {
            throw new AppException(AppError.BIZ_EVENT_NOT_FOUND_IUR, organizationFiscalCode, iur);
        }

        // the query should always return only one element
        else if (bizEventEntityList.size() > 1) {
            throw new AppException(AppError.BIZ_EVENT_NOT_UNIQUE_IUR, organizationFiscalCode, iur);
        }

        // the bizEventEntityList has only one element
        return modelMapper.map(bizEventEntityList.get(0), CtReceiptModelResponse.class);
    }

}
