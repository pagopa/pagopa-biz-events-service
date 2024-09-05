package it.gov.pagopa.bizeventsservice.service;

import static it.gov.pagopa.bizeventsservice.util.ViewGenerator.generateBizEventsViewUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

import com.azure.spring.data.cosmos.core.query.CosmosPageRequest;

import feign.FeignException;
import it.gov.pagopa.bizeventsservice.client.IReceiptGeneratePDFClient;
import it.gov.pagopa.bizeventsservice.client.IReceiptGetPDFClient;
import it.gov.pagopa.bizeventsservice.entity.BizEvent;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewCart;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewGeneral;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewUser;
import it.gov.pagopa.bizeventsservice.exception.AppException;
import it.gov.pagopa.bizeventsservice.model.filterandorder.Order.TransactionListOrder;
import it.gov.pagopa.bizeventsservice.model.response.Attachment;
import it.gov.pagopa.bizeventsservice.model.response.AttachmentsDetailsResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.CartItem;
import it.gov.pagopa.bizeventsservice.model.response.transaction.InfoTransactionView;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionDetailResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListItem;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListResponse;
import it.gov.pagopa.bizeventsservice.repository.BizEventsViewCartRepository;
import it.gov.pagopa.bizeventsservice.repository.BizEventsViewGeneralRepository;
import it.gov.pagopa.bizeventsservice.repository.BizEventsViewUserRepository;
import it.gov.pagopa.bizeventsservice.service.impl.TransactionService;
import it.gov.pagopa.bizeventsservice.util.ViewGenerator;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@ContextConfiguration
public class TransactionServiceTest {
    public static final String INVALID_FISCAL_CODE = "invalidFiscalCode";
    public static final int PAGE_SIZE = 5;
    public static final int PAGE_NUMBER = 0;
    public static final String CONTINUATION_TOKEN = "0";
    public static final String TRANSACTION_ID = "transactionId";
    public static final String TRANSACTION_DISABLE_PATH = "/transactions/transaction-id/disable";
    public static final String TRANSACTION_RECEIPT_PATH = "/transactions/event-id/pdf";
    public static final String VALID_FISCAL_CODE = "AAAAAA00A00A000A";
    public static final String FISCAL_CODE_HEADER_KEY = "x-fiscal-code";

    @MockBean
    private BizEventsViewUserRepository bizEventsViewUserRepository;
    @MockBean
    private BizEventsViewGeneralRepository bizEventsViewGeneralRepository;
    @MockBean
    private BizEventsViewCartRepository bizEventsViewCartRepository;
    @MockBean
    private IReceiptGetPDFClient receiptClient;
    @MockBean
    private IReceiptGeneratePDFClient generateReceiptClient;
    @MockBean
    private IBizEventsService bizEventsService;
    
    private TransactionService transactionService;
    @Value("${transaction.payee.cartName:Pagamento Multiplo}")
    private String payeeCartName;
    
    private byte[] receipt = {69, 121, 101, 45, 62, 118, 101, 114, (byte) 196, (byte) 195, 61, 101, 98};

    @BeforeEach
    void setUp() {
        transactionService = spy(new TransactionService(bizEventsViewGeneralRepository, bizEventsViewCartRepository, bizEventsViewUserRepository, 
        		receiptClient, generateReceiptClient));
        Attachment attachmentDetail = mock (Attachment.class);
        AttachmentsDetailsResponse attachments = AttachmentsDetailsResponse.builder().attachments(Arrays.asList(attachmentDetail)).build();   
        when(receiptClient.getAttachments(anyString(), anyString())).thenReturn(attachments);
        when(receiptClient.getReceipt(anyString(), anyString(), any())).thenReturn(receipt);
        when(generateReceiptClient.generateReceipt(anyString(), anyString(), any())).thenReturn("OK");
    }

    @Test
    void taxCodeWithEventsShouldReturnTransactionList() {
        List<BizEventsViewUser> listOfViewUser = ViewGenerator.generateListOfFiveBizEventsViewUser();
        Page<BizEventsViewUser> pageOfViewUser = mock(Page.class);
        when(pageOfViewUser.getContent()).thenReturn(listOfViewUser);
        CosmosPageRequest pageRequest = mock(CosmosPageRequest.class);
        when(pageRequest.getRequestContinuation()).thenReturn(CONTINUATION_TOKEN);
        Pageable pageable = mock(Pageable.class);
        when(pageOfViewUser.getPageable()).thenReturn(pageable);
        when(pageable.next()).thenReturn(pageRequest);
        when(bizEventsViewUserRepository.getBizEventsViewUserByTaxCode(eq(ViewGenerator.USER_TAX_CODE_WITH_TX), any(), any(), any()))
                .thenReturn(pageOfViewUser);
        List<BizEventsViewCart> listOfViewCart = Collections.singletonList(ViewGenerator.generateBizEventsViewCart());
        when(bizEventsViewCartRepository.getBizEventsViewCartByTransactionIdAndFilteredByTaxCode(
                anyString(), eq(ViewGenerator.USER_TAX_CODE_WITH_TX)))
                .thenReturn(listOfViewCart);
        TransactionListResponse transactionListResponse =
                Assertions.assertDoesNotThrow(() ->
                transactionService.getTransactionList(
                        ViewGenerator.USER_TAX_CODE_WITH_TX, null, null, CONTINUATION_TOKEN, PAGE_SIZE, TransactionListOrder.TRANSACTION_DATE, Direction.DESC));
        Assertions.assertEquals(CONTINUATION_TOKEN, transactionListResponse.getContinuationToken());
        List<TransactionListItem> transactionListItems = transactionListResponse.getTransactionList();
        Assertions.assertNotNull(transactionListItems);
        Assertions.assertEquals(listOfViewUser.size(), transactionListItems.size());

        for(TransactionListItem listItem : transactionListItems){
            Assertions.assertEquals(ViewGenerator.FORMATTED_AMOUNT, listItem.getAmount());
            Assertions.assertEquals(listOfViewCart.get(0).getPayee().getName(), listItem.getPayeeName());
            Assertions.assertEquals(listOfViewCart.get(0).getPayee().getTaxCode(), listItem.getPayeeTaxCode());
        }

        verify(bizEventsViewUserRepository).getBizEventsViewUserByTaxCode(eq(ViewGenerator.USER_TAX_CODE_WITH_TX), any(), any(), any());
        verifyNoMoreInteractions(bizEventsViewUserRepository);
    }

    @Test
    void taxCodeWithEventsAndMultipleCartItemsShouldReturnTransactionList() {
        List<BizEventsViewUser> listOfViewUser = ViewGenerator.generateListOfFiveBizEventsViewUser();
        Page<BizEventsViewUser> pageOfViewUser = mock(Page.class);
        when(pageOfViewUser.getContent()).thenReturn(listOfViewUser);
        CosmosPageRequest pageRequest = mock(CosmosPageRequest.class);
        when(pageRequest.getRequestContinuation()).thenReturn(CONTINUATION_TOKEN);
        Pageable pageable = mock(Pageable.class);
        when(pageOfViewUser.getPageable()).thenReturn(pageable);
        when(pageable.next()).thenReturn(pageRequest);
        when(bizEventsViewUserRepository.getBizEventsViewUserByTaxCode(eq(ViewGenerator.USER_TAX_CODE_WITH_TX), any(), any(), any()))
                .thenReturn(pageOfViewUser);
        List<BizEventsViewCart> listOfViewCart = ViewGenerator.generateListOfFiveViewCart();
        when(bizEventsViewCartRepository.getBizEventsViewCartByTransactionIdAndFilteredByTaxCode(
                anyString(), eq(ViewGenerator.USER_TAX_CODE_WITH_TX)))
                .thenReturn(listOfViewCart);
        TransactionListResponse transactionListResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getTransactionList(
                                ViewGenerator.USER_TAX_CODE_WITH_TX, null, null, CONTINUATION_TOKEN, PAGE_SIZE, TransactionListOrder.TRANSACTION_DATE, Direction.DESC));
        Assertions.assertEquals(CONTINUATION_TOKEN, transactionListResponse.getContinuationToken());
        List<TransactionListItem> transactionListItems = transactionListResponse.getTransactionList();
        Assertions.assertNotNull(transactionListItems);
        Assertions.assertEquals(listOfViewUser.size(), transactionListItems.size());

        for(TransactionListItem listItem : transactionListItems){
        	// PAGOPA-1763: the amount value must be returned only if it is not a cart type transaction
        	Assertions.assertTrue(listItem.getIsCart() && Boolean.TRUE.equals(listItem.getIsDebtor()) ? 
        			listItem.getAmount()==null:listItem.getAmount().equals(ViewGenerator.FORMATTED_GRAND_TOTAL));
            Assertions.assertEquals(payeeCartName, listItem.getPayeeName());
            Assertions.assertEquals("", listItem.getPayeeTaxCode());
        }

        verify(bizEventsViewUserRepository).getBizEventsViewUserByTaxCode(eq(ViewGenerator.USER_TAX_CODE_WITH_TX), any(), any(), any());
        verifyNoMoreInteractions(bizEventsViewUserRepository);
    }

    @Test
    void idAndTaxCodeWithOneEventShouldReturnTransactionDetails() {
        List<BizEventsViewGeneral> generalViewList = Collections.singletonList(ViewGenerator.generateBizEventsViewGeneral());
        when(bizEventsViewGeneralRepository.findByTransactionId(ViewGenerator.TRANSACTION_ID))
                .thenReturn(generalViewList);
        List<BizEventsViewCart> listOfCartView = Collections.singletonList(ViewGenerator.generateBizEventsViewCart());
        when(bizEventsViewCartRepository.getBizEventsViewCartByTransactionId(ViewGenerator.TRANSACTION_ID))
                .thenReturn(listOfCartView);
        TransactionDetailResponse transactionDetailResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getTransactionDetails(
                                ViewGenerator.USER_TAX_CODE_WITH_TX, ViewGenerator.TRANSACTION_ID));
        Assertions.assertNotNull(transactionDetailResponse);
        verify(bizEventsViewGeneralRepository).findByTransactionId(ViewGenerator.TRANSACTION_ID);
        verify(bizEventsViewCartRepository).getBizEventsViewCartByTransactionId(ViewGenerator.TRANSACTION_ID);
        verifyNoMoreInteractions(bizEventsViewGeneralRepository);
        verifyNoMoreInteractions(bizEventsViewCartRepository);

        BizEventsViewGeneral viewGeneral = generalViewList.get(0);
        Assertions.assertNotNull(transactionDetailResponse);
        InfoTransactionView infoTransaction = transactionDetailResponse.getInfoTransaction();
        Assertions.assertEquals(viewGeneral.getTransactionId(), infoTransaction.getTransactionId());
        Assertions.assertEquals(viewGeneral.getAuthCode(), infoTransaction.getAuthCode());
        Assertions.assertEquals(viewGeneral.getRrn(), infoTransaction.getRrn());
        Assertions.assertEquals(viewGeneral.getTransactionDate(), infoTransaction.getTransactionDate());
        Assertions.assertEquals(viewGeneral.getPspName(), infoTransaction.getPspName());
        Assertions.assertEquals(viewGeneral.getWalletInfo().getAccountHolder(), infoTransaction.getWalletInfo().getAccountHolder());
        Assertions.assertEquals(viewGeneral.getWalletInfo().getBrand(), infoTransaction.getWalletInfo().getBrand());
        Assertions.assertEquals(viewGeneral.getWalletInfo().getBlurredNumber(), infoTransaction.getWalletInfo().getBlurredNumber());
        Assertions.assertEquals(viewGeneral.getWalletInfo().getMaskedEmail(), infoTransaction.getWalletInfo().getMaskedEmail());
        Assertions.assertEquals(viewGeneral.getPayer().getName(), infoTransaction.getPayer().getName());
        Assertions.assertEquals(viewGeneral.getPayer().getTaxCode(), infoTransaction.getPayer().getTaxCode());
        Assertions.assertEquals(ViewGenerator.FORMATTED_AMOUNT, infoTransaction.getAmount());
        Assertions.assertEquals(viewGeneral.getFee(), infoTransaction.getFee());
        Assertions.assertEquals(viewGeneral.getPaymentMethod(), infoTransaction.getPaymentMethod());
        Assertions.assertEquals(viewGeneral.getOrigin(), infoTransaction.getOrigin());

        BizEventsViewCart viewCart = listOfCartView.get(0);
        CartItem cartItem = transactionDetailResponse.getCarts().get(0);
        Assertions.assertEquals(viewCart.getSubject(), cartItem.getSubject());
        Assertions.assertEquals(ViewGenerator.FORMATTED_AMOUNT, cartItem.getAmount());
        Assertions.assertEquals(viewCart.getDebtor().getName(), cartItem.getDebtor().getName());
        Assertions.assertEquals(viewCart.getDebtor().getTaxCode(), cartItem.getDebtor().getTaxCode());
        Assertions.assertEquals(viewCart.getPayee().getName(), cartItem.getPayee().getName());
        Assertions.assertEquals(viewCart.getPayee().getTaxCode(), cartItem.getPayee().getTaxCode());
        Assertions.assertEquals(viewCart.getRefNumberType(), cartItem.getRefNumberType());
        Assertions.assertEquals(viewCart.getRefNumberValue(), cartItem.getRefNumberValue());
    }
    @Test
    void idAndTaxCodeWithCartEventShouldReturnTransactionDetails() {
        List<BizEventsViewGeneral> generalViewList = Collections.singletonList(ViewGenerator.generateBizEventsViewGeneral());
        when(bizEventsViewGeneralRepository.findByTransactionId(ViewGenerator.TRANSACTION_ID))
                .thenReturn(generalViewList);
        List<BizEventsViewCart> listOfCartView = ViewGenerator.generateListOfFiveViewCart();
        when(bizEventsViewCartRepository.getBizEventsViewCartByTransactionId(ViewGenerator.TRANSACTION_ID))
                .thenReturn(listOfCartView);
        TransactionDetailResponse transactionDetailResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getTransactionDetails(
                                ViewGenerator.USER_TAX_CODE_WITH_TX, ViewGenerator.TRANSACTION_ID));
        Assertions.assertNotNull(transactionDetailResponse);
        verify(bizEventsViewGeneralRepository).findByTransactionId(ViewGenerator.TRANSACTION_ID);
        verify(bizEventsViewCartRepository).getBizEventsViewCartByTransactionId(ViewGenerator.TRANSACTION_ID);
        verifyNoMoreInteractions(bizEventsViewGeneralRepository);
        verifyNoMoreInteractions(bizEventsViewCartRepository);

        Assertions.assertEquals(ViewGenerator.FORMATTED_GRAND_TOTAL, transactionDetailResponse.getInfoTransaction().getAmount());
        for(int i = 0; i < transactionDetailResponse.getCarts().size(); i++){
            BizEventsViewCart viewCart = listOfCartView.get(i);
            CartItem cartItem = transactionDetailResponse.getCarts().get(i);
            Assertions.assertEquals(viewCart.getSubject(), cartItem.getSubject());
            Assertions.assertEquals(ViewGenerator.FORMATTED_AMOUNT, cartItem.getAmount());
            Assertions.assertEquals(viewCart.getDebtor().getName(), cartItem.getDebtor().getName());
            Assertions.assertEquals(viewCart.getDebtor().getTaxCode(), cartItem.getDebtor().getTaxCode());
            Assertions.assertEquals(viewCart.getPayee().getName(), cartItem.getPayee().getName());
            Assertions.assertEquals(viewCart.getPayee().getTaxCode(), cartItem.getPayee().getTaxCode());
            Assertions.assertEquals(viewCart.getRefNumberType(), cartItem.getRefNumberType());
            Assertions.assertEquals(viewCart.getRefNumberValue(), cartItem.getRefNumberValue());
        }
    }
    @Test
    void transactionViewGeneralNotFoundThrowError() {
        List<BizEventsViewGeneral> generalViewList = new ArrayList<>();
        when(bizEventsViewGeneralRepository.findByTransactionId(ViewGenerator.TRANSACTION_ID))
                .thenReturn(generalViewList);
        AppException appException =
                Assertions.assertThrows(AppException.class, () ->
                        transactionService.getTransactionDetails(
                                ViewGenerator.USER_TAX_CODE_WITH_TX, ViewGenerator.TRANSACTION_ID));
        verify(bizEventsViewGeneralRepository).findByTransactionId(ViewGenerator.TRANSACTION_ID);
        verifyNoMoreInteractions(bizEventsViewGeneralRepository);
        verifyNoInteractions(bizEventsViewCartRepository);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, appException.getHttpStatus());
    }
    @Test
    void transactionViewCartNotFoundThrowError() {
        List<BizEventsViewGeneral> generalViewList = Collections.singletonList(ViewGenerator.generateBizEventsViewGeneral());
        when(bizEventsViewGeneralRepository.findByTransactionId(ViewGenerator.TRANSACTION_ID))
                .thenReturn(generalViewList);
        List<BizEventsViewCart> listOfCartView = new ArrayList<>();
        when(bizEventsViewCartRepository.getBizEventsViewCartByTransactionId(ViewGenerator.TRANSACTION_ID))
                .thenReturn(listOfCartView);
        AppException appException =
                Assertions.assertThrows(AppException.class, () ->
                        transactionService.getTransactionDetails(
                                ViewGenerator.USER_TAX_CODE_WITH_TX, ViewGenerator.TRANSACTION_ID));
        verify(bizEventsViewGeneralRepository).findByTransactionId(ViewGenerator.TRANSACTION_ID);
        verify(bizEventsViewCartRepository).getBizEventsViewCartByTransactionId(ViewGenerator.TRANSACTION_ID);
        verifyNoMoreInteractions(bizEventsViewGeneralRepository);
        verifyNoMoreInteractions(bizEventsViewCartRepository);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, appException.getHttpStatus());
    }
    @Test
    void transactionViewUserDisabled() {
        List<BizEventsViewUser> viewUserList = Collections.singletonList(generateBizEventsViewUser());
        when(bizEventsViewUserRepository.getBizEventsViewUserByTaxCodeAndTransactionId(anyString(),anyString()))
                .thenReturn(viewUserList);
        Assertions.assertDoesNotThrow(() -> transactionService.disableTransaction(
                ViewGenerator.USER_TAX_CODE_WITH_TX, ViewGenerator.TRANSACTION_ID));
        
        ArgumentCaptor<List<BizEventsViewUser>> argument = ArgumentCaptor.forClass(List.class);
        verify(bizEventsViewUserRepository).saveAll(argument.capture());
        assertEquals(Boolean.TRUE, argument.getValue().get(0).getHidden());
    }
    
    @Test
    void transactionViewUserCartDisabled() {
    	
    	List<BizEventsViewUser> viewUserList = new ArrayList<>();
    	
    	for (int i=0; i<10; i++) {
    		BizEventsViewUser u = generateBizEventsViewUser();
    		u.setId(u.getId()+"_"+i);
    		viewUserList.add(u);
    	}
    	
        when(bizEventsViewUserRepository.getBizEventsViewUserByTaxCodeAndTransactionId(anyString(),anyString()))
                .thenReturn(viewUserList);
        
        Assertions.assertDoesNotThrow(() -> transactionService.disableTransaction(
                ViewGenerator.USER_TAX_CODE_WITH_TX, ViewGenerator.TRANSACTION_ID));
        
        ArgumentCaptor<List<BizEventsViewUser>> argument = ArgumentCaptor.forClass(List.class);
        verify(bizEventsViewUserRepository).saveAll(argument.capture());
        argument.getValue().forEach(u -> assertEquals(Boolean.TRUE, u.getHidden()));
    }

    @Test
    void transactionViewUserNotFoundThrowError() {
        when(bizEventsViewUserRepository.getBizEventsViewUserByTaxCodeAndTransactionId(anyString(),anyString()))
                .thenReturn(null);
        AppException appException =
                Assertions.assertThrows(AppException.class, () ->
                        transactionService.getTransactionDetails(
                                ViewGenerator.USER_TAX_CODE_WITH_TX, ViewGenerator.TRANSACTION_ID));
        Assertions.assertEquals(HttpStatus.NOT_FOUND, appException.getHttpStatus());
    }
    
    @Test
    void getPDFReceiptOK() {
    	
    	BizEvent bizEvent = mock (BizEvent.class);
    	when (bizEventsService.getBizEvent(anyString())).thenReturn(bizEvent);
    	
    	byte[] res =
                Assertions.assertDoesNotThrow(() ->
                transactionService.getPDFReceipt(
                		VALID_FISCAL_CODE, "event-id"));
  
    	assertEquals(receipt.length, res.length);	
    }
    
    @Test
    void getPDFReceiptForMissingEventIdOK() {
    	
    	BizEvent bizEvent = mock (BizEvent.class);
    	when (bizEventsService.getBizEvent(anyString())).thenReturn(bizEvent);
    	Attachment attachmentDetail = mock (Attachment.class);
        AttachmentsDetailsResponse attachments = AttachmentsDetailsResponse.builder().attachments(Arrays.asList(attachmentDetail)).build();  
    	when(receiptClient.getAttachments(anyString(), eq("missing-id"))).thenThrow(FeignException.NotFound.class).thenReturn(attachments);
        
    	byte[] res =
                Assertions.assertDoesNotThrow(() ->
                transactionService.getPDFReceipt(
                		VALID_FISCAL_CODE, "missing-id"));
  
    	verify(transactionService).getPDFReceipt(VALID_FISCAL_CODE,"missing-id");
    	verify(receiptClient, times(2)).getAttachments(VALID_FISCAL_CODE,"missing-id");
    	verify(generateReceiptClient).generateReceipt("missing-id", "false", "{}");
    	verify(receiptClient).getReceipt(eq(VALID_FISCAL_CODE), eq("missing-id"), any());
    	assertEquals(receipt.length, res.length);
    }
    
    @Test
    void getPDFReceiptForMissingPDFFileOK() {
    	
    	BizEvent bizEvent = mock (BizEvent.class);
    	when (bizEventsService.getBizEvent(anyString())).thenReturn(bizEvent);
    	when(receiptClient.getReceipt(anyString(), eq("missing-pdf-file"), any())).thenThrow(FeignException.NotFound.class).thenReturn(receipt);
        
    	byte[] res =
                Assertions.assertDoesNotThrow(() ->
                transactionService.getPDFReceipt(
                		VALID_FISCAL_CODE, "missing-pdf-file"));
    	
    	verify(transactionService).getPDFReceipt(VALID_FISCAL_CODE,"missing-pdf-file");
    	verify(receiptClient).getAttachments(VALID_FISCAL_CODE,"missing-pdf-file");
    	verify(generateReceiptClient).generateReceipt("missing-pdf-file", "false", "{}");
    	verify(receiptClient, times(2)).getReceipt(eq(VALID_FISCAL_CODE), eq("missing-pdf-file"), any());
    	assertEquals(receipt.length, res.length);	
    }
    
    @Test
    void getPDFReceiptForUnhandledExceptionKO() {

    	BizEvent bizEvent = mock (BizEvent.class);
    	when (bizEventsService.getBizEvent(anyString())).thenReturn(bizEvent);
    	// Override @BeforeEach condition
    	when(receiptClient.getAttachments(anyString(), eq("event-id"))).thenThrow(FeignException.BadRequest.class);


    	Assertions.assertThrows(FeignException.BadRequest.class, () ->
    	transactionService.getPDFReceipt(
    			INVALID_FISCAL_CODE, "event-id"));

    	verify(receiptClient).getAttachments(INVALID_FISCAL_CODE,"event-id");
    }
}
