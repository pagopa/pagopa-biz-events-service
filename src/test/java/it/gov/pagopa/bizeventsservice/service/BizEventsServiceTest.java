package it.gov.pagopa.bizeventsservice.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.CosmosDBEmulatorContainer;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;

import it.gov.pagopa.bizeventsservice.entity.BizEvent;
import it.gov.pagopa.bizeventsservice.exception.AppException;
import it.gov.pagopa.bizeventsservice.initializer.Initializer;
import it.gov.pagopa.bizeventsservice.model.response.CtReceiptModelResponse;
import it.gov.pagopa.bizeventsservice.repository.BizEventsRepository;
import it.gov.pagopa.bizeventsservice.util.TestUtil;

@TestMethodOrder(OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@ContextConfiguration(initializers = {Initializer.class})
class BizEventsServiceTest {

	@Autowired
    private BizEventsRepository bizEventsRepository;
	
	@Autowired
    private ModelMapper modelMapper;
	
    private static final CosmosDBEmulatorContainer emulator = Initializer.getEmulator();

	private static BizEventsService bizEventsService;
	
	private BizEvent bizEventEntity;
	private BizEvent bizEventEntityDuplicated;

	@BeforeAll
	public void setUp() throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {

		bizEventsService = spy(new BizEventsService(bizEventsRepository, modelMapper));

		
		CosmosAsyncClient client = new CosmosClientBuilder().gatewayMode().endpointDiscoveryEnabled(false)
				.endpoint(emulator.getEmulatorEndpoint()).key(emulator.getEmulatorKey()).buildAsyncClient();

		// creation of the database and containers
		CosmosDatabaseResponse databaseResponse = client.createDatabaseIfNotExists("db").block();

		assertTrue(201 == databaseResponse.getStatusCode() || 200 == databaseResponse.getStatusCode());
		CosmosContainerResponse containerResponse = client.getDatabase("db")
				.createContainerIfNotExists("biz-events", "/id").block();
		assertTrue(201 == containerResponse.getStatusCode() || 200 == containerResponse.getStatusCode());
		

		// loading the database with test data
		bizEventEntity = TestUtil.readModelFromFile("biz-events/bizEvent.json", BizEvent.class);
		bizEventsRepository.save(bizEventEntity);
	}
	
	@AfterAll
	void teardown() {
		// clear the test data
		bizEventsRepository.delete(bizEventEntity);
		bizEventsRepository.delete(bizEventEntityDuplicated);
		
	}	

	@Test
	@Order(1)
	void getOrganizationReceipt() {
		assertTrue(emulator.isRunning());
		CtReceiptModelResponse ctReceipt = bizEventsService.getOrganizationReceipt("66666666666", "CCD01", "112006686812600");
		assertNull(ctReceipt.getReceiptId());
		assertEquals("BBT", ctReceipt.getPaymentMethod());
	}
	
	@Test
	@Order(2)
	void getOrganizationReceipt_404() {
		assertTrue(emulator.isRunning());
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
	@Order(3)
	void getOrganizationReceipt_422() throws IOException {
		assertTrue(emulator.isRunning());
		// save an entity with different id but with same payment info
		bizEventEntityDuplicated = TestUtil.readModelFromFile("biz-events/bizEvent_duplicate.json", BizEvent.class);
		bizEventsRepository.save(bizEventEntityDuplicated);
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
