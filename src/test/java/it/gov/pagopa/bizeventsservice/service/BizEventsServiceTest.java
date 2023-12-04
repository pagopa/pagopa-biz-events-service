package it.gov.pagopa.bizeventsservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.azure.cosmos.models.PartitionKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import it.gov.pagopa.bizeventsservice.entity.BizEvent;
import it.gov.pagopa.bizeventsservice.exception.AppException;
import it.gov.pagopa.bizeventsservice.model.response.CtReceiptModelResponse;
import it.gov.pagopa.bizeventsservice.repository.BizEventsRepository;
import it.gov.pagopa.bizeventsservice.service.impl.BizEventsService;
import it.gov.pagopa.bizeventsservice.util.TestUtil;

@TestMethodOrder(OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
class BizEventsServiceTest {

    private static final String ORGANIZATION_FISCAL_CODE = "66666666666";
    private static final String IUV = "112006686812600";
    private static final String BIZ_EVENT_ID = "b77d4987-a3e4-48d4-a2fd-af504f8b79e9";
    private static final String IUR = "CCD01";

    @Mock
    private BizEventsRepository bizEventsRepository;

    @Autowired
    private ModelMapper modelMapper;

    private IBizEventsService bizEventsService;

    private BizEvent bizEventEntity;

    private BizEvent bizEventEntityDuplicated;

    @BeforeAll
    public void beforeAll() throws IOException {
        bizEventEntity = TestUtil.readModelFromFile("biz-events/bizEvent.json", BizEvent.class);
        bizEventEntityDuplicated = TestUtil.readModelFromFile("biz-events/bizEvent_duplicate.json", BizEvent.class);
    }

    @BeforeEach
    void setUp() {
        bizEventsService = spy(new BizEventsService(bizEventsRepository, modelMapper));
    }

    @Test
    void getOrganizationReceipt() {
        when(bizEventsRepository.getBizEventByOrgFiscCodeAndIur(ORGANIZATION_FISCAL_CODE, IUR, IUV))
                .thenReturn(List.of(bizEventEntity));

        CtReceiptModelResponse ctReceipt = bizEventsService.getOrganizationReceipt(ORGANIZATION_FISCAL_CODE, IUR,
                IUV);
        assertEquals("85570ffebb13411b80d79f415641ec55", ctReceipt.getReceiptId());
        assertEquals("cash", ctReceipt.getPaymentMethod());
    }

    @Test
    void getOrganizationReceipt_404() throws IOException {
        when(bizEventsRepository.getBizEventByOrgFiscCodeAndIur(ORGANIZATION_FISCAL_CODE, IUR, IUV))
                .thenReturn(List.of(bizEventEntity));

        AppException e = assertThrows(AppException.class, () -> bizEventsService.getOrganizationReceipt(ORGANIZATION_FISCAL_CODE, IUR, "fake_iuv"));
        assertEquals(HttpStatus.NOT_FOUND, e.getHttpStatus());
    }

    @Test
    void getOrganizationReceipt_422() throws IOException {
        // mocking a fake save for duplicated entity
        when(bizEventsRepository.getBizEventByOrgFiscCodeAndIur(ORGANIZATION_FISCAL_CODE, IUR, IUV))
                .thenReturn(List.of(bizEventEntity, bizEventEntityDuplicated));

        AppException e = assertThrows(AppException.class, () -> bizEventsService.getOrganizationReceipt(ORGANIZATION_FISCAL_CODE, IUR, IUV));
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, e.getHttpStatus());
    }

    @Test
    void getBizEventSuccess() {
        when(bizEventsRepository.findById(BIZ_EVENT_ID, new PartitionKey(BIZ_EVENT_ID)))
                .thenReturn(Optional.of(bizEventEntity));

        BizEvent bizEvent = bizEventsService.getBizEvent(BIZ_EVENT_ID);
        assertEquals(bizEvent, bizEventEntity);
    }

	@Test
    void getBizEventFailNotFound() {
        when(bizEventsRepository.findById("fake id", new PartitionKey("fake id")))
                .thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> bizEventsService.getBizEvent("fake id"));
        assertEquals(HttpStatus.NOT_FOUND, e.getHttpStatus());
    }

	@Test
    void getBizEventByOrgFiscalCodeAndIuvSuccess() {
        when(bizEventsRepository.getBizEventByOrgFiscalCodeAndIuv(ORGANIZATION_FISCAL_CODE, IUV))
                .thenReturn(List.of(bizEventEntity));

        BizEvent bizEvent = bizEventsService.getBizEventByOrgFiscalCodeAndIuv(ORGANIZATION_FISCAL_CODE, IUV);
        assertEquals(bizEvent.getId(), bizEventEntity.getId());
        assertEquals(bizEvent.getCreditor().getIdPA(), bizEventEntity.getCreditor().getIdPA());
        assertEquals(bizEvent.getDebtorPosition().getIuv(), bizEventEntity.getDebtorPosition().getIuv());
    }

	@Test
    void getBizEventByOrgFiscalCodeAndIuvFailNotFound() {
        when(bizEventsRepository.getBizEventByOrgFiscalCodeAndIuv(ORGANIZATION_FISCAL_CODE, IUV))
                .thenReturn(Collections.emptyList());

        AppException e = assertThrows(AppException.class, () -> bizEventsService.getBizEventByOrgFiscalCodeAndIuv(ORGANIZATION_FISCAL_CODE, "fake iuv"));
        assertEquals(HttpStatus.NOT_FOUND, e.getHttpStatus());
    }

	@Test
    void getBizEventByOrgFiscalCodeAndIuvFailNotUnique() {
        when(bizEventsRepository.getBizEventByOrgFiscalCodeAndIuv(ORGANIZATION_FISCAL_CODE, IUV))
                .thenReturn(List.of(bizEventEntity, bizEventEntityDuplicated));

        AppException e = assertThrows(AppException.class, () -> bizEventsService.getBizEventByOrgFiscalCodeAndIuv(ORGANIZATION_FISCAL_CODE, IUV));
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, e.getHttpStatus());
    }
}
