package it.gov.pagopa.bizeventsservice.service;

import it.gov.pagopa.bizeventsservice.entity.BizEvent;
import it.gov.pagopa.bizeventsservice.model.response.CtReceiptModelResponse;

public interface IBizEventsService {

    CtReceiptModelResponse getOrganizationReceipt(String organizationFiscalCode,
                                                  String iur, String iuv);

    BizEvent getBizEvent(String id);

    BizEvent getBizEventFromLAPId(String id);

    BizEvent getBizEventByOrgFiscalCodeAndIuv(String organizationFiscalCode,
                                              String iuv);

    CtReceiptModelResponse getOrganizationReceipt(String organizationFiscalCode,
                                                  String iur);

}