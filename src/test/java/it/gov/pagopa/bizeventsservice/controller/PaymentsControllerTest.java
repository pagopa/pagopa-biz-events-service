package it.gov.pagopa.bizeventsservice.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import it.gov.pagopa.bizeventsservice.model.response.CtReceiptModelResponse;
import it.gov.pagopa.bizeventsservice.service.IBizEventsService;
import it.gov.pagopa.bizeventsservice.util.Utility;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentsControllerTest {
		
	@Autowired
    private MockMvc mvc;
	
	@MockBean
    private IBizEventsService bizEventsService;
	
	@BeforeEach
    void setUp() throws IOException {
		// precondition
		CtReceiptModelResponse ctReceiptModel = Utility.readModelFromFile("receipts/getOrganizationReceipt.json", CtReceiptModelResponse.class);
        when(bizEventsService.getOrganizationReceipt(anyString(), anyString(), anyString())).thenReturn(ctReceiptModel);
        when(bizEventsService.getOrganizationReceipt(anyString(), anyString())).thenReturn(ctReceiptModel);
    }

    @ParameterizedTest
    @CsvSource({
            "/organizations/mock_organizationfiscalcode/receipts/mock_iur/paymentoptions/mock_iuv",
            "/organizations/mock_organizationfiscalcode/receipts/mock_iur"
    })
    void getOrganizationReceipt(String url) throws Exception {
        MvcResult result = mvc.perform(get(url).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        assertNotNull(result.getResponse().getContentAsString());
        // check receiptId
        assertTrue(result.getResponse().getContentAsString().contains("5dcd115a-c18d-4197-a33b-681767e82999"));
        // check fiscalCode
        assertTrue(result.getResponse().getContentAsString().contains("66666666666"));
    }
}
