package it.gov.pagopa.bizeventsservice.controller;


import it.gov.pagopa.bizeventsservice.exception.AppError;
import it.gov.pagopa.bizeventsservice.exception.AppException;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListItem;
import it.gov.pagopa.bizeventsservice.service.ITransactionService;
import it.gov.pagopa.bizeventsservice.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TransactionControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ITransactionService transactionService;

    @BeforeEach
    void setUp() throws IOException {
        // precondition
        List<TransactionListItem> transactionListItems = TestUtil.readModelFromFile("biz-events/getTransactionList.json", List.class);
        when(transactionService.getTransactionList(anyString(), anyInt(), anyInt())).thenReturn(transactionListItems);
    }

    @Test
    void getListTransactionShouldReturnData() throws Exception {
        MvcResult result = mvc.perform(get("/transactions")
                        .header("x-fiscal-code", "TX_FISCAL_CODE")
                        .queryParam("start", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        assertNotNull(result.getResponse().getContentAsString());
        assertTrue(result.getResponse().getContentAsString().contains("b77d4987-a3e4-48d4-a2fd-af504f8b79e9"));
        assertTrue(result.getResponse().getContentAsString().contains("100.0"));
    }

    @Test
    void getListTransactionWithMissingFiscalCodeShouldReturnError() throws Exception {
        mvc.perform(get("/transactions")
                        .queryParam("start", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void getListTransactionWithMissingStartShouldReturnError() throws Exception {
        mvc.perform(get("/transactions")
                        .header("x-fiscal-code", "TX_FISCAL_CODE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void getListTransactionWithInvalidFiscalCodeShouldReturnError() throws Exception {
        when(transactionService.getTransactionList(anyString(), anyInt(), anyInt())).thenAnswer(x -> {
            throw new AppException(AppError.INVALID_FISCAL_CODE, "INVALID_TX_FISCAL_CODE");
        });
        mvc.perform(get("/transactions")
                        .header("x-fiscal-code", "INVALID_TX_FISCAL_CODE")
                        .queryParam("start", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }


}
