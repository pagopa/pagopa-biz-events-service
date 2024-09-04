package it.gov.pagopa.bizeventsservice.controller;


import it.gov.pagopa.bizeventsservice.entity.BizEvent;
import it.gov.pagopa.bizeventsservice.exception.AppError;
import it.gov.pagopa.bizeventsservice.exception.AppException;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionDetailResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListItem;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListResponse;
import it.gov.pagopa.bizeventsservice.service.IBizEventsService;
import it.gov.pagopa.bizeventsservice.service.ITransactionService;
import it.gov.pagopa.bizeventsservice.util.Utility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PaidNoticeControllerTest {

    public static final String INVALID_FISCAL_CODE = "INVALID_TX_FISCAL_CODE";
    public static final String VALID_FISCAL_CODE = "AAAAAA00A00A000A";
    public static final String FISCAL_CODE_HEADER_KEY = "x-fiscal-code";
    public static final String PAIDS_EVENT_ID_DISABLE_PATH = "/paids/1234321234/disable";
    public static final String PAIDS_EVENT_ID_PDF_PATH = "/paids/event-id/pdf";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private IBizEventsService bizEventsService;

    @MockBean
    private ITransactionService transactionService;

    
    private byte[] receipt = {69, 121, 101, 45, 62, 118, 101, 114, (byte) 196, (byte) 195, 61, 101, 98};

    @BeforeEach
    void setUp() throws IOException {
        // precondition
        List<TransactionListItem> transactionListItems = Utility.readModelFromFile("biz-events/getTransactionList.json", List.class);
        TransactionListResponse transactionListResponse = TransactionListResponse.builder().transactionList(transactionListItems).build();
        TransactionDetailResponse transactionDetailResponse = Utility.readModelFromFile("biz-events/transactionDetails.json", TransactionDetailResponse.class);
        when(transactionService.getTransactionList(eq(VALID_FISCAL_CODE), any(), any(), anyString(), anyInt(), any(), any())).thenReturn(transactionListResponse);
        when(transactionService.getCachedTransactionList(eq(VALID_FISCAL_CODE), any(), any(), anyInt(), anyInt(), any(), any())).thenReturn(transactionListResponse);
        when(transactionService.getTransactionDetails(anyString(), anyString())).thenReturn(transactionDetailResponse);
        when(transactionService.getPDFReceipt(anyString(), anyString())).thenReturn(receipt);
    }

    @Test
    void getPaidNoticeDisableShouldReturnOK() throws Exception {
        mvc.perform(post(PAIDS_EVENT_ID_DISABLE_PATH)
                        .header(FISCAL_CODE_HEADER_KEY, VALID_FISCAL_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        verify(transactionService).disableTransaction(any(), any());
    }

    @Test
    void getPaidNoticeDisableWithMissingFiscalCodeShouldReturnError() throws Exception {
        mvc.perform(post(PAIDS_EVENT_ID_DISABLE_PATH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void getPaidNoticeDisableWithInvalidFiscalCodeShouldReturnError() throws Exception {
        doAnswer(x -> {
            throw new AppException(AppError.INVALID_FISCAL_CODE, INVALID_FISCAL_CODE);
        }).when(transactionService).disableTransaction(anyString(), anyString());
        mvc.perform(post(PAIDS_EVENT_ID_DISABLE_PATH)
                        .header(FISCAL_CODE_HEADER_KEY, INVALID_FISCAL_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void getPDFReceipt_ShouldReturnOK() throws Exception {

        BizEvent bizEvent = mock (BizEvent.class);
        when (bizEventsService.getBizEvent(anyString())).thenReturn(bizEvent);

        MvcResult result = mvc.perform(get(PAIDS_EVENT_ID_PDF_PATH)
                        .header(FISCAL_CODE_HEADER_KEY, VALID_FISCAL_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andReturn();

        verify(bizEventsService).getBizEvent("event-id");
        verify(transactionService).getPDFReceipt(VALID_FISCAL_CODE,"event-id");
        assertEquals(receipt.length, result.getResponse().getContentAsByteArray().length);
    }

    @Test
    void getPDFReceiptForOldPMEvent_ShouldReturnNOTFOUND() throws Exception {
        AppException ex = new AppException(HttpStatus.NOT_FOUND, "mock", "mock");
        when (bizEventsService.getBizEvent(anyString())).thenThrow(ex);

        mvc.perform(get(PAIDS_EVENT_ID_PDF_PATH)
                        .header(FISCAL_CODE_HEADER_KEY, VALID_FISCAL_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        verify(bizEventsService).getBizEvent("event-id");
    }

}
