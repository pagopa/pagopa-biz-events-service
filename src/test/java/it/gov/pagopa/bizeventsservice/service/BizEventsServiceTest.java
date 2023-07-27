package it.gov.pagopa.bizeventsservice.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
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
import it.gov.pagopa.bizeventsservice.util.TestUtil;

@TestMethodOrder(OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
class BizEventsServiceTest {


	@Mock
    private BizEventsRepository bizEventsRepository;
	
	@Autowired
    private ModelMapper modelMapper;

	private static BizEventsService bizEventsService;

	private BizEvent bizEventEntity;

	private BizEvent bizEventEntityDuplicated;

	@BeforeAll
	public void setUp() throws IOException {
		bizEventEntity = TestUtil.readModelFromFile("biz-events/bizEvent.json", BizEvent.class);
		bizEventEntityDuplicated = TestUtil.readModelFromFile("biz-events/bizEvent_duplicate.json", BizEvent.class);
	}

	@Test
	void getOrganizationReceipt() {
		bizEventsService = spy(new BizEventsService(bizEventsRepository, modelMapper));
		when(bizEventsRepository.getBizEventByOrgFiscCodeAndIur("66666666666", "CCD01", "112006686812600")).thenReturn(List.of(bizEventEntity));

		CtReceiptModelResponse ctReceipt = bizEventsService.getOrganizationReceipt("66666666666", "CCD01", "112006686812600");
		assertEquals("85570ffebb13411b80d79f415641ec55", ctReceipt.getReceiptId());
		assertEquals("cash", ctReceipt.getPaymentMethod());
	}
	
	@Test
	void getOrganizationReceipt_404() throws IOException {
		bizEventsService = spy(new BizEventsService(bizEventsRepository, modelMapper));
		when(bizEventsRepository.getBizEventByOrgFiscCodeAndIur("66666666666", "CCD01", "112006686812600")).thenReturn(List.of(bizEventEntity));

		try {
			// non-existent iuv -> raise a 404 exception
			bizEventsService.getOrganizationReceipt("66666666666", "CCD01", "fake_iuv");
			fail();
		} catch (AppException e) {
			assertEquals(HttpStatus.NOT_FOUND, e.getHttpStatus());
		} catch (Exception e) {
			fail();
		}
	}
	
	@Test
	void getOrganizationReceipt_422() throws IOException {

        // mocking a fake save for duplicated entity
		bizEventsService = spy(new BizEventsService(bizEventsRepository, modelMapper));
        when(bizEventsRepository.getBizEventByOrgFiscCodeAndIur("66666666666", "CCD01", "112006686812600")).thenReturn(List.of(bizEventEntity, bizEventEntityDuplicated));

        try {
			// more than one records, the payment must be unique -> raise a 422 exception
			bizEventsService.getOrganizationReceipt("66666666666", "CCD01", "112006686812600");
			fail();
		} catch (AppException e) {
			assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, e.getHttpStatus());
		} catch (Exception e) {
			fail();
		}
	}

	

}
