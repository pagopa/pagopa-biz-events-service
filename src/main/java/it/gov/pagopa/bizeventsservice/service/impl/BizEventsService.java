package it.gov.pagopa.bizeventsservice.service.impl;

import com.azure.cosmos.models.PartitionKey;
import it.gov.pagopa.bizeventsservice.entity.BizEvent;
import it.gov.pagopa.bizeventsservice.exception.AppError;
import it.gov.pagopa.bizeventsservice.exception.AppException;
import it.gov.pagopa.bizeventsservice.model.response.CtReceiptModelResponse;
import it.gov.pagopa.bizeventsservice.repository.primary.BizEventsPrimaryRepository;
import it.gov.pagopa.bizeventsservice.repository.replica.BizEventsRepository;
import it.gov.pagopa.bizeventsservice.service.IBizEventsService;
import it.gov.pagopa.bizeventsservice.util.TransactionIdFactory;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BizEventsService implements IBizEventsService {

    private final BizEventsRepository bizEventsRepository;
    private final BizEventsPrimaryRepository bizEventsPrimaryRepository;

    private final ModelMapper modelMapper;

    @Autowired
    public BizEventsService(BizEventsRepository bizEventsRepository, BizEventsPrimaryRepository bizEventsPrimaryRepository, ModelMapper modelMapper) {
        this.bizEventsRepository = bizEventsRepository;
        this.bizEventsPrimaryRepository = bizEventsPrimaryRepository;
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
        // get biz event
        Optional<BizEvent> optionalBizEvent = bizEventsRepository.findById(id, new PartitionKey(id));

        if (optionalBizEvent.isEmpty()) {
            throw new AppException(AppError.BIZ_EVENT_NOT_FOUND_WITH_ID, id);
        }
        return optionalBizEvent.get();
    }

    @Override
    public BizEvent getBizEventFromLAPId(String id) {
        Optional<BizEvent> optionalBizEvent;

        if (TransactionIdFactory.isCart(id)) {
            TransactionIdFactory.ViewTransactionId viewTransactionId = TransactionIdFactory.extract(id);
            String cartId = viewTransactionId.transactionId();
            String eventId = viewTransactionId.eventId();
            boolean isPayer = eventId == null;
            if (isPayer) {
                // if it's a payer cart biz event, get by cart id because biz event id is not available
                optionalBizEvent = this.bizEventsPrimaryRepository.findByCartId(cartId).stream().findFirst();
            } else {
                // is a debtor cart biz event, so get biz event by id
                optionalBizEvent = this.bizEventsPrimaryRepository.findById(eventId, new PartitionKey(eventId));
            }
        } else {
            // is a single payment
            optionalBizEvent = this.bizEventsPrimaryRepository.findById(id, new PartitionKey(id));
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
            // get multiple event id to enrich details info
            String eventsId = bizEventEntityList.stream()
                    .map(event -> "[" + event.getId() + "]")
                    .collect(Collectors.joining());
            throw new AppException(AppError.BIZ_EVENT_NOT_UNIQUE_WITH_ORG_CF_AND_IUV, organizationFiscalCode, iuv, eventsId);
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
