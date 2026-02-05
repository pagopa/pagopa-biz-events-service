package it.gov.pagopa.bizeventsservice.service;

import com.azure.cosmos.models.PartitionKey;
import it.gov.pagopa.bizeventsservice.entity.BizEvent;
import it.gov.pagopa.bizeventsservice.exception.AppException;
import it.gov.pagopa.bizeventsservice.model.response.CtReceiptModelResponse;
import it.gov.pagopa.bizeventsservice.repository.primary.BizEventsPrimaryRepository;
import it.gov.pagopa.bizeventsservice.repository.replica.BizEventsRepository;
import it.gov.pagopa.bizeventsservice.service.impl.BizEventsService;
import it.gov.pagopa.bizeventsservice.util.Utility;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

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
    @Mock
    private BizEventsPrimaryRepository bizEventsPrimaryRepository;

    @Autowired
    private ModelMapper modelMapper;

    private IBizEventsService bizEventsService;

    private BizEvent bizEventEntity;
    private BizEvent bizEventEntityDuplicated;
    private BizEvent bizEventEntityWithMbd;

    @BeforeAll
    void beforeAll() throws IOException {
        bizEventEntity = Utility.readModelFromFile("biz-events/bizEvent.json", BizEvent.class);
        bizEventEntityDuplicated = Utility.readModelFromFile("biz-events/bizEvent_duplicate.json", BizEvent.class);
        bizEventEntityWithMbd = Utility.readModelFromFile("biz-events/bizEvent_with_MBD.json", BizEvent.class);

    }

    @BeforeEach
    void setUp() {
        bizEventsService = spy(new BizEventsService(bizEventsRepository, bizEventsPrimaryRepository, modelMapper));
    }

    @Test
    void getOrganizationReceiptIuvIur() {
        when(bizEventsRepository.getBizEventByOrgFiscCodeIuvAndIur(ORGANIZATION_FISCAL_CODE, IUR, IUV))
                .thenReturn(List.of(bizEventEntity));

        CtReceiptModelResponse ctReceipt = bizEventsService.getOrganizationReceipt(ORGANIZATION_FISCAL_CODE, IUR,
                IUV);
        assertEquals("85570ffebb13411b80d79f415641ec55", ctReceipt.getReceiptId());
        assertEquals("cash", ctReceipt.getPaymentMethod());
    }

    @Test
    void getOrganizationReceiptIuvIur_404() {
        when(bizEventsRepository.getBizEventByOrgFiscCodeIuvAndIur(ORGANIZATION_FISCAL_CODE, IUR, IUV))
                .thenReturn(List.of(bizEventEntity));

        AppException e = assertThrows(AppException.class, () -> bizEventsService.getOrganizationReceipt(ORGANIZATION_FISCAL_CODE, IUR, "fake_iuv"));
        assertEquals(HttpStatus.NOT_FOUND, e.getHttpStatus());
    }

    @Test
    void getOrganizationReceiptIuvIur_422() {
        // mocking a fake save for duplicated entity
        when(bizEventsRepository.getBizEventByOrgFiscCodeIuvAndIur(ORGANIZATION_FISCAL_CODE, IUR, IUV))
                .thenReturn(List.of(bizEventEntity, bizEventEntityDuplicated));

        AppException e = assertThrows(AppException.class, () -> bizEventsService.getOrganizationReceipt(ORGANIZATION_FISCAL_CODE, IUR, IUV));
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, e.getHttpStatus());
    }

    @Test
    void getOrganizationReceiptIur() {
        when(bizEventsRepository.getBizEventByOrgFiscCodeAndIur(ORGANIZATION_FISCAL_CODE, IUR))
                .thenReturn(List.of(bizEventEntity));

        CtReceiptModelResponse ctReceipt = bizEventsService.getOrganizationReceipt(ORGANIZATION_FISCAL_CODE, IUR);
        assertEquals("85570ffebb13411b80d79f415641ec55", ctReceipt.getReceiptId());
        assertEquals("cash", ctReceipt.getPaymentMethod());
    }

    @Test
    void getOrganizationReceiptIur_404() {
        when(bizEventsRepository.getBizEventByOrgFiscCodeAndIur(ORGANIZATION_FISCAL_CODE, IUR))
                .thenReturn(List.of(bizEventEntity));

        AppException e = assertThrows(AppException.class, () -> bizEventsService.getOrganizationReceipt(ORGANIZATION_FISCAL_CODE, "fake_iur"));
        assertEquals(HttpStatus.NOT_FOUND, e.getHttpStatus());
    }

    @Test
    void getOrganizationReceiptIur_422() {
        // mocking a fake save for duplicated entity
        when(bizEventsRepository.getBizEventByOrgFiscCodeAndIur(ORGANIZATION_FISCAL_CODE, IUR))
                .thenReturn(List.of(bizEventEntity, bizEventEntityDuplicated));

        AppException e = assertThrows(AppException.class, () -> bizEventsService.getOrganizationReceipt(ORGANIZATION_FISCAL_CODE, IUR));
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
    void getBizEventFromLAPIdSuccess() {
        when(bizEventsPrimaryRepository.findById(BIZ_EVENT_ID, new PartitionKey(BIZ_EVENT_ID)))
                .thenReturn(Optional.of(bizEventEntity));

        BizEvent bizEvent = bizEventsService.getBizEventFromLAPId(BIZ_EVENT_ID);
        assertEquals(bizEvent, bizEventEntity);
    }

    @Test
    void getBizEventFromLAPIdFailNotFound() {
        when(bizEventsRepository.findById("fake id", new PartitionKey("fake id")))
                .thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> bizEventsService.getBizEventFromLAPId("fake id"));
        assertEquals(HttpStatus.NOT_FOUND, e.getHttpStatus());
    }


    @Test
    void getBizEventFromLAPIdFailNotFoundCartPayer() {
        when(bizEventsRepository.findById("fake id", new PartitionKey("fake id")))
                .thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> bizEventsService.getBizEventFromLAPId("id_CART_"));
        assertEquals(HttpStatus.NOT_FOUND, e.getHttpStatus());
    }


    @Test
    void getBizEventFromLAPIdFailNotFoundCartDebtor() {
        when(bizEventsRepository.findById("bizeventid", new PartitionKey("bizeventid")))
                .thenReturn(Optional.empty());

        AppException e = assertThrows(AppException.class, () -> bizEventsService.getBizEventFromLAPId("cartid_CART_bizeventid"));
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
    
    @Test
    void getOrganizationReceiptIuvIur_shouldMapMbdAttachmentStringAndFallbackToMbdObject() {
        when(bizEventsRepository.getBizEventByOrgFiscCodeIuvAndIur(ORGANIZATION_FISCAL_CODE, IUR, IUV))
                .thenReturn(List.of(bizEventEntityWithMbd));

        CtReceiptModelResponse ctReceipt = bizEventsService.getOrganizationReceipt(ORGANIZATION_FISCAL_CODE, IUR, IUV);

        // assert transfer list
        Assertions.assertNotNull(ctReceipt.getTransferList());
        assertEquals(2, ctReceipt.getTransferList().size());

        // 1. MBDAttachment string for the first transfer
        assertEquals("FROM_STRING", ctReceipt.getTransferList().get(0).getMbdAttachment());

        // 2. MBDAttachment is null for the second transfer
        assertNull(ctReceipt.getTransferList().get(1).getMbdAttachment());

        assertEquals("50.0", ctReceipt.getTransferList().get(0).getTransferAmount().toPlainString());
        assertEquals("51.0", ctReceipt.getTransferList().get(1).getTransferAmount().toPlainString());
    }
    
    @Test
    void getOrganizationReceiptIur_shouldMapMbdAttachmentString() {
        when(bizEventsRepository.getBizEventByOrgFiscCodeAndIur(ORGANIZATION_FISCAL_CODE, IUR))
                .thenReturn(List.of(bizEventEntityWithMbd));

        CtReceiptModelResponse ctReceipt = bizEventsService.getOrganizationReceipt(ORGANIZATION_FISCAL_CODE, IUR);

        Assertions.assertNotNull(ctReceipt.getTransferList());
        assertEquals(2, ctReceipt.getTransferList().size());

        assertEquals("FROM_STRING", ctReceipt.getTransferList().get(0).getMbdAttachment());
        assertNull(ctReceipt.getTransferList().get(1).getMbdAttachment());
    }

}
