package it.gov.pagopa.bizeventsservice.controller;


import it.gov.pagopa.bizeventsservice.exception.AppError;
import it.gov.pagopa.bizeventsservice.exception.AppException;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionDetailResponse;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TransactionControllerTest {

    public static final String INVALID_FISCAL_CODE = "INVALID_TX_FISCAL_CODE";
    public static final String VALID_FISCAL_CODE = "AAAAAA00A00A000A";
    public static final String LIST_TRANSACTION_PATH = "/transactions";
    public static final String FISCAL_CODE_HEADER_KEY = "x-fiscal-code";
    public static final String TRANSACTION_DETAILS_PATH = "/transactions/transaction-id";
    public static final String TRANSACTION_DISABLE_PATH = "/transactions/transaction-id/disable";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ITransactionService transactionService;

    @BeforeEach
    void setUp() throws IOException {
        // precondition
        List<TransactionListItem> transactionListItems = TestUtil.readModelFromFile("biz-events/getTransactionList.json", List.class);
        TransactionDetailResponse transactionDetail = TestUtil.readModelFromFile("biz-events/transactionDetails.json", TransactionDetailResponse.class);
        when(transactionService.getTransactionList(anyString(), anyString(), anyInt())).thenReturn(transactionListItems);
        when(transactionService.getTransactionDetails(anyString(), anyString())).thenReturn(transactionDetail);
    }

/*    @Test
    void getListTransactionShouldReturnData() throws Exception {
        MvcResult result = mvc.perform(get(LIST_TRANSACTION_PATH)
                        .header(FISCAL_CODE_HEADER_KEY, VALID_FISCAL_CODE)
                        .queryParam("start", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        assertNotNull(result.getResponse().getContentAsString());
        assertTrue(result.getResponse().getContentAsString().contains("b77d4987-a3e4-48d4-a2fd-af504f8b79e9"));
        assertTrue(result.getResponse().getContentAsString().contains("100.0"));
    }*/

    @Test
    void getListTransactionWithMissingFiscalCodeShouldReturnError() throws Exception {
        mvc.perform(get(LIST_TRANSACTION_PATH)
                        .queryParam("start", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

//    @Test
//    void getListTransactionWithMissingStartShouldReturnError() throws Exception {
//        mvc.perform(get(LIST_TRANSACTION_PATH)
//                        .header(FISCAL_CODE_HEADER_KEY, VALID_FISCAL_CODE)
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isBadRequest())
//                .andReturn();
//    }

//    @Test
//    void getListTransactionWithInvalidFiscalCodeShouldReturnError() throws Exception {
//        when(transactionService.getTransactionList(anyString(), anyString(), anyInt())).thenAnswer(x -> {
//            throw new AppException(AppError.INVALID_FISCAL_CODE, INVALID_FISCAL_CODE);
//        });
//        mvc.perform(get(LIST_TRANSACTION_PATH)
//                        .header(FISCAL_CODE_HEADER_KEY, INVALID_FISCAL_CODE)
//                        .queryParam("start", "0")
//                        .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isBadRequest())
//                .andReturn();
//    }

    @Test
    void getTransactionDetailsShouldReturnData() throws Exception {
        MvcResult result = mvc.perform(get(TRANSACTION_DETAILS_PATH)
                        .header(FISCAL_CODE_HEADER_KEY, VALID_FISCAL_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        assertNotNull(result.getResponse().getContentAsString());
        assertTrue(result.getResponse().getContentAsString().contains("PAYER NAME"));
    }

    @Test
    void getTransactionDetailsWithMissingFiscalCodeShouldReturnError() throws Exception {
        mvc.perform(get(TRANSACTION_DETAILS_PATH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void getTransactionDetailsWithInvalidFiscalCodeShouldReturnError() throws Exception {
        when(transactionService.getTransactionDetails(anyString(), anyString())).thenAnswer(x -> {
            throw new AppException(AppError.INVALID_FISCAL_CODE, INVALID_FISCAL_CODE);
        });
        mvc.perform(get(TRANSACTION_DETAILS_PATH)
                        .header(FISCAL_CODE_HEADER_KEY, INVALID_FISCAL_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void getTransactionDisableShouldReturnOK() throws Exception {
        mvc.perform(post(TRANSACTION_DISABLE_PATH)
                        .header(FISCAL_CODE_HEADER_KEY, VALID_FISCAL_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        verify(transactionService).disableTransaction(any(), any());
    }

    @Test
    void getTransactionDisableWithMissingFiscalCodeShouldReturnError() throws Exception {
        mvc.perform(post(TRANSACTION_DISABLE_PATH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void getTransactionDisableithInvalidFiscalCodeShouldReturnError() throws Exception {
        doAnswer(x -> {
            throw new AppException(AppError.INVALID_FISCAL_CODE, INVALID_FISCAL_CODE);
        }).when(transactionService).disableTransaction(anyString(), anyString());;
        mvc.perform(post(TRANSACTION_DISABLE_PATH)
                        .header(FISCAL_CODE_HEADER_KEY, INVALID_FISCAL_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }


}
