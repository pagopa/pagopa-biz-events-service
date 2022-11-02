package it.gov.pagopa.bizeventsservice.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import it.gov.pagopa.bizeventsservice.initializer.Initializer;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {Initializer.class})
class BaseControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @Test
  void shouldRespondOKtoHeartBeat() throws Exception {
    mockMvc.perform(get("/info")).andExpect(status().isOk());
  }
}