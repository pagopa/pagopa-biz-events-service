package it.gov.pagopa.bizeventsservice.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import it.gov.pagopa.bizeventsservice.client.IReceiptGeneratePDFClient;
import it.gov.pagopa.bizeventsservice.client.IReceiptGetPDFClient;
import it.gov.pagopa.bizeventsservice.exception.AppError;
import it.gov.pagopa.bizeventsservice.exception.AppException;
import it.gov.pagopa.bizeventsservice.exception.ErrorCode;
import it.gov.pagopa.bizeventsservice.model.response.paidnotice.NoticeDetailResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListItem;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListResponse;
import it.gov.pagopa.bizeventsservice.service.ITransactionService;
import it.gov.pagopa.bizeventsservice.util.Utility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
public class PaidNoticeControllerTest {

    public static final String INVALID_FISCAL_CODE = "INVALID_TX_FISCAL_CODE";
    public static final String VALID_FISCAL_CODE = "AAAAAA00A00A000A";
    public static final String FISCAL_CODE_HEADER_KEY = "x-fiscal-code";
    public static final String PAIDS_EVENT_ID_DISABLE_PATH = "/paids/1234321234/disable";
    public static final String PAIDS_EVENT_DETAILS_PATH = "/paids/event-id";
    public static final String PAIDS_PATH = "/paids";
    public static final String SIZE = "10";
    public static final String SIZE_HEADER_KEY = "size";
    public static final String CONTINUATION_TOKEN = "continuationToken";
    public static final String PAIDS_EVENT_ID_PDF_PATH = "/paids/event-id/pdf";
    private static final String CONTINUATION_TOKEN_HEADER_KEY = "x-continuation-token";
    @Autowired
    private MockMvc mvc;

    @MockBean
    private IReceiptGetPDFClient receiptClient;

    @MockBean
    private IReceiptGeneratePDFClient generateReceiptClient;

    @MockBean
    private ITransactionService transactionService;


    private final byte[] receipt = {69, 121, 101, 45, 62, 118, 101, 114, (byte) 196, (byte) 195, 61, 101, 98};

    @BeforeEach
    void setUp() throws IOException {
        // precondition
        List<TransactionListItem> transactionListItems = Utility.readModelFromFile("biz-events/getTransactionList.json", new TypeReference<List<TransactionListItem>>() {
        });
        TransactionListResponse transactionListResponse = TransactionListResponse.builder().transactionList(transactionListItems).build();
        NoticeDetailResponse noticeDetailResponse = Utility.readModelFromFile("biz-events/paidNoticeDetails.json", NoticeDetailResponse.class);
        when(transactionService.getTransactionList(eq(VALID_FISCAL_CODE), any(), any(), anyString(), anyBoolean(), anyInt(), any(), any())).thenReturn(transactionListResponse);
        when(transactionService.getPaidNoticeDetail(anyString(), anyString())).thenReturn(noticeDetailResponse);
        ResponseEntity<byte[]> receiptPdfResponse = mock(ResponseEntity.class);
        when(receiptPdfResponse.getBody()).thenReturn(receipt);
        HttpHeaders headers = mock(HttpHeaders.class);
        when(headers.getOrDefault(eq("filename"), any())).thenReturn(List.of("filename.pdf"));
        when(receiptPdfResponse.getHeaders()).thenReturn(headers);
        when(receiptClient.getReceiptPdf(anyString(), anyString())).thenReturn(receiptPdfResponse);
        when(generateReceiptClient.generateReceipt(anyString())).thenReturn("OK");
    }

    @Test
    void getPaidNoticesListShouldReturnData() throws Exception {
        MvcResult result = mvc.perform(get(PAIDS_PATH)
                        .header(FISCAL_CODE_HEADER_KEY, VALID_FISCAL_CODE)
                        .header(CONTINUATION_TOKEN_HEADER_KEY, CONTINUATION_TOKEN)
                        .queryParam(SIZE_HEADER_KEY, SIZE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        assertNotNull(result.getResponse().getContentAsString());
        assertTrue(result.getResponse().getContentAsString().contains("b77d4987-a3e4-48d4-a2fd-af504f8b79e9"));
        assertTrue(result.getResponse().getContentAsString().contains("100.0"));
    }

    @Test
    void getPaidNoticesListWithMissingFiscalCodeShouldReturnError() throws Exception {
        mvc.perform(get(PAIDS_PATH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void getPaidNoticesListWithInvalidFiscalCodeShouldReturnError() throws Exception {
        when(transactionService.getTransactionList(eq(INVALID_FISCAL_CODE), any(), any(), any(), anyBoolean(), anyInt(), any(), any())).thenAnswer(x -> {
            throw new AppException(AppError.INVALID_FISCAL_CODE, INVALID_FISCAL_CODE);
        });
        mvc.perform(get(PAIDS_PATH)
                        .header(FISCAL_CODE_HEADER_KEY, INVALID_FISCAL_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void getPaidNoticeDisableShouldReturnOK() throws Exception {
        mvc.perform(post(PAIDS_EVENT_ID_DISABLE_PATH)
                        .header(FISCAL_CODE_HEADER_KEY, VALID_FISCAL_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        verify(transactionService).disablePaidNotice(any(), any());
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
        }).when(transactionService).disablePaidNotice(anyString(), anyString());
        mvc.perform(post(PAIDS_EVENT_ID_DISABLE_PATH)
                        .header(FISCAL_CODE_HEADER_KEY, INVALID_FISCAL_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void getPaidNoticeDetailShouldReturnData() throws Exception {
        MvcResult result = mvc.perform(get(PAIDS_EVENT_DETAILS_PATH)
                        .header(FISCAL_CODE_HEADER_KEY, VALID_FISCAL_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        assertNotNull(result.getResponse().getContentAsString());
        assertTrue(result.getResponse().getContentAsString().contains("PAYER NAME"));
    }

    @Test
    void getPaidNoticeDetailWithMissingFiscalCodeShouldReturnError() throws Exception {
        mvc.perform(get(PAIDS_EVENT_DETAILS_PATH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void getPaidNoticeDetailWithInvalidFiscalCodeShouldReturnError() throws Exception {
        when(transactionService.getPaidNoticeDetail(anyString(), anyString())).thenAnswer(x -> {
            throw new AppException(AppError.INVALID_FISCAL_CODE, INVALID_FISCAL_CODE);
        });
        mvc.perform(get(PAIDS_EVENT_DETAILS_PATH)
                        .header(FISCAL_CODE_HEADER_KEY, INVALID_FISCAL_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void getPDFReceipt_ShouldReturnOK() throws Exception {
        ResponseEntity<Resource> response = mock(ResponseEntity.class);
        when(response.getStatusCode()).thenReturn(HttpStatus.OK);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/pdf");
        when(response.getHeaders()).thenReturn(headers);
        when(response.getStatusCodeValue()).thenReturn(200);
        when(transactionService.getPDFReceiptResponse(anyString(), any())).thenReturn(response);

        mvc.perform(get(PAIDS_EVENT_ID_PDF_PATH)
                        .header(FISCAL_CODE_HEADER_KEY, VALID_FISCAL_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andReturn();

        verify(transactionService).getPDFReceiptResponse(VALID_FISCAL_CODE, "event-id");
    }

    @Test
    void getPDFReceiptForOldPMEvent_ShouldReturnNOTFOUND() throws Exception {
        AppException ex = new AppException(HttpStatus.NOT_FOUND, ErrorCode.TS_000_000, "mock", "mock");
        when(transactionService.getPDFReceiptResponse(anyString(), anyString())).thenThrow(ex);

        mvc.perform(get(PAIDS_EVENT_ID_PDF_PATH)
                        .header(FISCAL_CODE_HEADER_KEY, VALID_FISCAL_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        verify(transactionService).getPDFReceiptResponse(VALID_FISCAL_CODE, "event-id");
    }

}
