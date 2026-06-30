package it.gov.pagopa.bizeventsservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import it.gov.pagopa.bizeventsservice.repository.primary.BizEventsViewUserQueryRepository;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ApplicationTest {
	
	@MockBean
	private BizEventsViewUserQueryRepository bizEventsViewUserQueryRepository;

    @Test
    void contextLoads() {
        // check only if the context is loaded
        assertTrue(true);
    }
}
