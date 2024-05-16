package it.gov.pagopa.bizeventsservice.controller;


import it.gov.pagopa.bizeventsservice.client.IReceiptPDFClient;
import it.gov.pagopa.bizeventsservice.entity.BizEvent;
import it.gov.pagopa.bizeventsservice.exception.AppError;
import it.gov.pagopa.bizeventsservice.exception.AppException;
import it.gov.pagopa.bizeventsservice.model.response.Attachment;
import it.gov.pagopa.bizeventsservice.model.response.AttachmentsDetailsResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionDetailResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListItem;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListResponse;
import it.gov.pagopa.bizeventsservice.service.IBizEventsService;
import it.gov.pagopa.bizeventsservice.service.ITransactionService;
import it.gov.pagopa.bizeventsservice.util.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import feign.FeignException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private static final String CONTINUATION_TOKEN_HEADER_KEY = "x-continuation-token";
    public static final String CONTINUATION_TOKEN = "continuationToken";
    public static final String SIZE_HEADER_KEY = "size";
    public static final String SIZE = "10";
    public static final String TRANSACTION_DISABLE_PATH = "/transactions/transaction-id/disable";
    public static final String TRANSACTION_RECEIPT_PATH = "/transactions/event-id/pdf";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ITransactionService transactionService;
    
    @MockBean
    private IBizEventsService bizEventsService;
    
    @MockBean
    private IReceiptPDFClient receiptClient;
    
    private byte[] receipt = {69, 121, 101, 45, 62, 118, 101, 114, (byte) 196, (byte) 195, 61, 101, 98};

    @BeforeEach
    void setUp() throws IOException {
        // precondition
        List<TransactionListItem> transactionListItems = TestUtil.readModelFromFile("biz-events/getTransactionList.json", List.class);
        TransactionListResponse transactionListResponse = TransactionListResponse.builder().transactionList(transactionListItems).build();
        TransactionDetailResponse transactionDetailResponse = TestUtil.readModelFromFile("biz-events/transactionDetails.json", TransactionDetailResponse.class);
        when(transactionService.getTransactionList(eq(VALID_FISCAL_CODE), anyString(), anyInt())).thenReturn(transactionListResponse);
        when(transactionService.getTransactionDetails(anyString(), anyString())).thenReturn(transactionDetailResponse);
        Attachment attachmentDetail = mock (Attachment.class);
        AttachmentsDetailsResponse attachments = AttachmentsDetailsResponse.builder().attachments(Arrays.asList(attachmentDetail)).build();   
        when(receiptClient.getAttachments(anyString(), anyString())).thenReturn(attachments);
        when(receiptClient.getReceipt(anyString(), anyString(), any())).thenReturn(receipt);
    }

    @Test
    void getListTransactionShouldReturnData() throws Exception {
        MvcResult result = mvc.perform(get(LIST_TRANSACTION_PATH)
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
    void getListTransactionWithMissingFiscalCodeShouldReturnError() throws Exception {
        mvc.perform(get(LIST_TRANSACTION_PATH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    void getListTransactionWithInvalidFiscalCodeShouldReturnError() throws Exception {
        when(transactionService.getTransactionList(eq(INVALID_FISCAL_CODE), any(), anyInt())).thenAnswer(x -> {
            throw new AppException(AppError.INVALID_FISCAL_CODE, INVALID_FISCAL_CODE);
        });
        mvc.perform(get(LIST_TRANSACTION_PATH)
                        .header(FISCAL_CODE_HEADER_KEY, INVALID_FISCAL_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

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
    
    @Test
    void getPDFReceipt_ShouldReturnOK() throws Exception {
    	
    	BizEvent bizEvent = mock (BizEvent.class);
    	when (bizEventsService.getBizEvent(anyString())).thenReturn(bizEvent);
        
    	MvcResult result = mvc.perform(get(TRANSACTION_RECEIPT_PATH)
                .header(FISCAL_CODE_HEADER_KEY, VALID_FISCAL_CODE)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_PDF))
        .andReturn();
    	
    	assertEquals(receipt.length, result.getResponse().getContentAsByteArray().length);	
    }
    
    @Test
    void getPDFReceiptForPMEvent_ShouldReturnNOTFOUND() throws Exception {
    	
    	BizEvent bizEvent = BizEvent.builder().id("mock_id").idPaymentManager("123456789").timestamp(1648771200000L).build();
    	when (bizEventsService.getBizEvent(anyString())).thenReturn(bizEvent);
        
    	mvc.perform(get(TRANSACTION_RECEIPT_PATH)
                .header(FISCAL_CODE_HEADER_KEY, VALID_FISCAL_CODE)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();
    }
    
    @Test // TODO This test will need to be modified when the receipt generation is introduced
    void getPDFReceiptForMissingEventId_ShouldReturnNOTFOUND() throws Exception {
    	
    	BizEvent bizEvent = mock (BizEvent.class);
    	when (bizEventsService.getBizEvent(anyString())).thenReturn(bizEvent);
    	when(receiptClient.getAttachments(anyString(), eq("missing-id"))).thenThrow(FeignException.NotFound.class);
        
    	mvc.perform(get("/transactions/missing-id/pdf")
                .header(FISCAL_CODE_HEADER_KEY, VALID_FISCAL_CODE)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();
    }
    
    @Test
    void getPDFReceiptForUnhandledException_ShouldReturnKO() throws Exception {
    	
    	BizEvent bizEvent = mock (BizEvent.class);
    	when (bizEventsService.getBizEvent(anyString())).thenReturn(bizEvent);
    	// Override @BeforeEach condition
    	when(receiptClient.getAttachments(anyString(), eq("event-id"))).thenThrow(FeignException.BadRequest.class);
        
    	mvc.perform(get(TRANSACTION_RECEIPT_PATH)
                .header(FISCAL_CODE_HEADER_KEY, INVALID_FISCAL_CODE)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();
    }

}
