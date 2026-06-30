package it.gov.pagopa.bizeventsservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import it.gov.pagopa.bizeventsservice.repository.primary.BizEventsViewUserQueryRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BaseControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private BizEventsViewUserQueryRepository bizEventsViewUserQueryRepository;

    @Test
    void shouldRespondOKtoHeartBeat() throws Exception {
        mockMvc.perform(get("/info")).andExpect(status().isOk());
    }
}