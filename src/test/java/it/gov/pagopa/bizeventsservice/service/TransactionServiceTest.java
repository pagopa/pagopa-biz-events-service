package it.gov.pagopa.bizeventsservice.service;

import com.azure.spring.data.cosmos.core.query.CosmosPageRequest;
import feign.FeignException;
import it.gov.pagopa.bizeventsservice.client.IReceiptGeneratePDFClient;
import it.gov.pagopa.bizeventsservice.client.IReceiptGetPDFClient;
import it.gov.pagopa.bizeventsservice.entity.BizEvent;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewCart;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewGeneral;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewUser;
import it.gov.pagopa.bizeventsservice.entity.view.enumeration.OriginType;
import it.gov.pagopa.bizeventsservice.exception.AppException;
import it.gov.pagopa.bizeventsservice.model.filterandorder.Order.TransactionListOrder;
import it.gov.pagopa.bizeventsservice.model.response.Attachment;
import it.gov.pagopa.bizeventsservice.model.response.AttachmentsDetailsResponse;
import it.gov.pagopa.bizeventsservice.model.response.paidnotice.InfoNotice;
import it.gov.pagopa.bizeventsservice.model.response.paidnotice.NoticeDetailResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.*;
import it.gov.pagopa.bizeventsservice.repository.primary.BizEventsViewCartRepository;
import it.gov.pagopa.bizeventsservice.repository.primary.BizEventsViewGeneralRepository;
import it.gov.pagopa.bizeventsservice.repository.primary.BizEventsViewUserRepository;
import it.gov.pagopa.bizeventsservice.service.impl.BizEventsService;
import it.gov.pagopa.bizeventsservice.service.impl.TransactionService;
import it.gov.pagopa.bizeventsservice.util.ViewGenerator;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static it.gov.pagopa.bizeventsservice.util.ViewGenerator.generateBizEventsViewUser;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
    @MockBean
    private CacheManager cacheManager;

    private TransactionService transactionService;

    private byte[] receipt = {69, 121, 101, 45, 62, 118, 101, 114, (byte) 196, (byte) 195, 61, 101, 98};

    @BeforeEach
    void setUp() {
        transactionService = spy(new TransactionService(bizEventsViewGeneralRepository, bizEventsViewCartRepository, bizEventsViewUserRepository,
                receiptClient, generateReceiptClient, bizEventsService, cacheManager));
        Attachment attachmentDetail = mock(Attachment.class);
        when(attachmentDetail.getName()).thenReturn("name.pdf");
        AttachmentsDetailsResponse attachments = AttachmentsDetailsResponse.builder().attachments(Arrays.asList(attachmentDetail)).build();
        when(receiptClient.getAttachments(anyString(), anyString())).thenReturn(attachments);
        when(receiptClient.getReceipt(anyString(), anyString(), any())).thenReturn(receipt);
        when(generateReceiptClient.generateReceipt(anyString())).thenReturn("OK");
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

        List<BizEventsViewCart> listOfCartView = ViewGenerator.generateListOfFiveViewCart();
        when(bizEventsViewCartRepository.findByTransactionIdIn(anySet())).thenReturn(listOfCartView);

        TransactionListResponse transactionListResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getTransactionList(
                                ViewGenerator.USER_TAX_CODE_WITH_TX, null, null, CONTINUATION_TOKEN, PAGE_SIZE, TransactionListOrder.TRANSACTION_DATE, Direction.DESC));
        assertEquals(CONTINUATION_TOKEN, transactionListResponse.getContinuationToken());
        List<TransactionListItem> transactionListItems = transactionListResponse.getTransactionList();
        assertNotNull(transactionListItems);
        assertEquals(listOfViewUser.size(), transactionListItems.size());

        for (TransactionListItem listItem : transactionListItems) {
            assertEquals(ViewGenerator.FORMATTED_AMOUNT, listItem.getAmount());
            assertEquals(ViewGenerator.PAYEE_NAME, listItem.getPayeeName());
            assertEquals(ViewGenerator.PAYEE_TAX_CODE, listItem.getPayeeTaxCode());
        }

        verify(bizEventsViewUserRepository).getBizEventsViewUserByTaxCode(eq(ViewGenerator.USER_TAX_CODE_WITH_TX), any(), any(), any());
        verifyNoMoreInteractions(bizEventsViewUserRepository);
    }

    @Test
    void taxCodeWithCartEventsShouldReturnTransactionList() {
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

        List<BizEventsViewCart> listOfCartView = ViewGenerator.generateListOfFiveViewCart();
        String cartTransactionId = listOfCartView.get(0).getTransactionId();
        listOfCartView.forEach(cartView -> cartView.setTransactionId(cartTransactionId));
        when(bizEventsViewCartRepository.findByTransactionIdIn(anySet())).thenReturn(listOfCartView);

        TransactionListResponse transactionListResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getTransactionList(
                                ViewGenerator.USER_TAX_CODE_WITH_TX, null, null, CONTINUATION_TOKEN, PAGE_SIZE, TransactionListOrder.TRANSACTION_DATE, Direction.DESC));
        assertEquals(CONTINUATION_TOKEN, transactionListResponse.getContinuationToken());
        List<TransactionListItem> transactionListItems = transactionListResponse.getTransactionList();
        assertNotNull(transactionListItems);
        assertEquals(listOfViewUser.size(), transactionListItems.size());

        for (TransactionListItem listItem : transactionListItems) {
            assertEquals(ViewGenerator.FORMATTED_AMOUNT, listItem.getAmount());
            assertEquals(ViewGenerator.PAYEE_NAME, listItem.getPayeeName());
            assertEquals(ViewGenerator.PAYEE_TAX_CODE, listItem.getPayeeTaxCode());
        }

        verify(bizEventsViewUserRepository).getBizEventsViewUserByTaxCode(eq(ViewGenerator.USER_TAX_CODE_WITH_TX), any(), any(), any());
        verifyNoMoreInteractions(bizEventsViewUserRepository);
    }

    @Test
    void taxCodeWithoutEventsShouldReturnEmptyTransactionList() {
        Page<BizEventsViewUser> pageOfViewUser = mock(Page.class);
        when(pageOfViewUser.getContent()).thenReturn(Collections.emptyList());
        Pageable pageable = mock(Pageable.class);
        when(pageOfViewUser.getPageable()).thenReturn(pageable);
        when(bizEventsViewUserRepository.getBizEventsViewUserByTaxCode(eq(ViewGenerator.USER_TAX_CODE_WITH_TX), any(), any(), any()))
                .thenReturn(pageOfViewUser);

        TransactionListResponse transactionListResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getTransactionList(
                                ViewGenerator.USER_TAX_CODE_WITH_TX, null, null, CONTINUATION_TOKEN, PAGE_SIZE, TransactionListOrder.TRANSACTION_DATE, Direction.DESC));
        assertNull(transactionListResponse.getContinuationToken());
        List<TransactionListItem> transactionListItems = transactionListResponse.getTransactionList();
        assertNotNull(transactionListItems);
        assertTrue(transactionListItems.isEmpty());

        verify(bizEventsViewUserRepository).getBizEventsViewUserByTaxCode(eq(ViewGenerator.USER_TAX_CODE_WITH_TX), any(), any(), any());
        verifyNoMoreInteractions(bizEventsViewUserRepository);
        verify(bizEventsViewCartRepository, never()).findByTransactionIdIn(anySet());
    }

    @Test
    void taxCodeWithEventsButWithoutCartShouldReturnEmptyTransactionList() {
        List<BizEventsViewUser> listOfViewUser = ViewGenerator.generateListOfFiveBizEventsViewUser();
        Page<BizEventsViewUser> pageOfViewUser = mock(Page.class);
        when(pageOfViewUser.getContent()).thenReturn(listOfViewUser);
        Pageable pageable = mock(Pageable.class);
        when(pageOfViewUser.getPageable()).thenReturn(pageable);
        when(bizEventsViewUserRepository.getBizEventsViewUserByTaxCode(eq(ViewGenerator.USER_TAX_CODE_WITH_TX), any(), any(), any()))
                .thenReturn(pageOfViewUser);

        when(bizEventsViewCartRepository.findByTransactionIdIn(anySet())).thenReturn(Collections.emptyList());

        TransactionListResponse transactionListResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getTransactionList(
                                ViewGenerator.USER_TAX_CODE_WITH_TX, null, null, CONTINUATION_TOKEN, PAGE_SIZE, TransactionListOrder.TRANSACTION_DATE, Direction.DESC));
        assertNull(transactionListResponse.getContinuationToken());
        List<TransactionListItem> transactionListItems = transactionListResponse.getTransactionList();
        assertNotNull(transactionListItems);
        assertTrue(transactionListItems.isEmpty());

        verify(bizEventsViewUserRepository).getBizEventsViewUserByTaxCode(eq(ViewGenerator.USER_TAX_CODE_WITH_TX), any(), any(), any());
        verifyNoMoreInteractions(bizEventsViewUserRepository);
    }

    @Test
    void idAndTaxCodeWithOneEventShouldReturnNoticeDetails() {
    	BizEventsViewGeneral viewGeneral = ViewGenerator.generateBizEventsViewGeneral(false);
        when(bizEventsViewGeneralRepository.findByTransactionId(ViewGenerator.TRANSACTION_ID))
        .thenReturn(List.of(viewGeneral));
        List<BizEventsViewCart> listOfCartView = Collections.singletonList(ViewGenerator.generateBizEventsViewCart());
        when(bizEventsViewCartRepository.getBizEventsViewCartByTransactionId(ViewGenerator.TRANSACTION_ID))
                .thenReturn(listOfCartView);
        NoticeDetailResponse noticeDetailResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getPaidNoticeDetail(
                                ViewGenerator.USER_TAX_CODE_WITH_TX, ViewGenerator.TRANSACTION_ID));
        assertNotNull(noticeDetailResponse);
        verify(bizEventsViewGeneralRepository).findByTransactionId(ViewGenerator.TRANSACTION_ID);
        verify(bizEventsViewCartRepository).getBizEventsViewCartByTransactionId(ViewGenerator.TRANSACTION_ID);
        verifyNoMoreInteractions(bizEventsViewGeneralRepository);
        verifyNoMoreInteractions(bizEventsViewCartRepository);

        assertNotNull(noticeDetailResponse);
        InfoNotice infoNotice = noticeDetailResponse.getInfoNotice();
        assertEquals(viewGeneral.getTransactionId(), infoNotice.getEventId());
        assertEquals(viewGeneral.getAuthCode(), infoNotice.getAuthCode());
        assertEquals(viewGeneral.getRrn(), infoNotice.getRrn());
        assertEquals(viewGeneral.getTransactionDate(), infoNotice.getNoticeDate());
        assertEquals(viewGeneral.getPspName(), infoNotice.getPspName());
        assertEquals(viewGeneral.getWalletInfo().getAccountHolder(), infoNotice.getWalletInfo().getAccountHolder());
        assertEquals(viewGeneral.getWalletInfo().getBrand(), infoNotice.getWalletInfo().getBrand());
        assertEquals(viewGeneral.getWalletInfo().getBlurredNumber(), infoNotice.getWalletInfo().getBlurredNumber());
        assertEquals(viewGeneral.getWalletInfo().getMaskedEmail(), infoNotice.getWalletInfo().getMaskedEmail());
        assertEquals(viewGeneral.getPayer().getName(), infoNotice.getPayer().getName());
        assertEquals(viewGeneral.getPayer().getTaxCode(), infoNotice.getPayer().getTaxCode());
        assertEquals(ViewGenerator.FORMATTED_AMOUNT, infoNotice.getAmount());
        assertEquals(viewGeneral.getFee(), infoNotice.getFee());
        assertEquals(viewGeneral.getPaymentMethod(), infoNotice.getPaymentMethod());
        assertEquals(viewGeneral.getOrigin(), infoNotice.getOrigin());

        BizEventsViewCart viewCart = listOfCartView.get(0);
        it.gov.pagopa.bizeventsservice.model.response.paidnotice.CartItem cartItem = noticeDetailResponse.getCarts().get(0);
        assertCartItems(viewCart, cartItem);
    }

    @Test
    void idAndTaxCodeWithOneEventFromCartAsDebtorShouldReturnNoticeDetails() {
        BizEventsViewGeneral viewGeneral = ViewGenerator.generateBizEventsViewGeneralForDebtor(true);
        when(bizEventsViewGeneralRepository.findByTransactionId(ViewGenerator.TRANSACTION_ID))
                .thenReturn(List.of(viewGeneral));
        List<BizEventsViewCart> listOfCartView = Collections.singletonList(ViewGenerator.generateBizEventsViewCart());
        when(bizEventsViewCartRepository.getBizEventsViewCartByTransactionIdAndIdAndFilteredByTaxCode(ViewGenerator.TRANSACTION_ID, ViewGenerator.EVENT_ID, ViewGenerator.USER_TAX_CODE_WITH_TX))
                .thenReturn(listOfCartView);
        NoticeDetailResponse noticeDetailResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getPaidNoticeDetail(
                                ViewGenerator.USER_TAX_CODE_WITH_TX, ViewGenerator.TRANSACTION_ID_ON_CART_FOR_DEBTOR));
        assertNotNull(noticeDetailResponse);
        verify(bizEventsViewGeneralRepository).findByTransactionId(ViewGenerator.TRANSACTION_ID);
        verify(bizEventsViewCartRepository).getBizEventsViewCartByTransactionIdAndIdAndFilteredByTaxCode(ViewGenerator.TRANSACTION_ID, ViewGenerator.EVENT_ID, ViewGenerator.USER_TAX_CODE_WITH_TX);
        verifyNoMoreInteractions(bizEventsViewGeneralRepository);
        verifyNoMoreInteractions(bizEventsViewCartRepository);

        assertNotNull(noticeDetailResponse);
        InfoNotice infoNotice = noticeDetailResponse.getInfoNotice();
        assertEquals(ViewGenerator.TRANSACTION_ID_ON_CART_FOR_DEBTOR, infoNotice.getEventId());
        assertEquals(viewGeneral.getAuthCode(), infoNotice.getAuthCode());
        assertEquals(viewGeneral.getRrn(), infoNotice.getRrn());
        assertEquals(viewGeneral.getTransactionDate(), infoNotice.getNoticeDate());
        assertEquals(viewGeneral.getPspName(), infoNotice.getPspName());
        assertNull(infoNotice.getWalletInfo());
        assertNull(infoNotice.getPayer());
        assertEquals(ViewGenerator.FORMATTED_AMOUNT, infoNotice.getAmount());
        assertNull(infoNotice.getFee());
        assertNull(infoNotice.getPaymentMethod());
        assertEquals(viewGeneral.getOrigin(), infoNotice.getOrigin());

        BizEventsViewCart viewCart = listOfCartView.get(0);
        it.gov.pagopa.bizeventsservice.model.response.paidnotice.CartItem cartItem = noticeDetailResponse.getCarts().get(0);
        assertCartItems(viewCart, cartItem);
    }

    @Test
    void idAndTaxCodeWithTwoEventsFromCartAsDebtorShouldReturnNoticeDetails() {
        List<BizEventsViewGeneral> viewGenerals = ViewGenerator.generateNBizEventsViewGeneralForDebtor(2, true);
        BizEventsViewGeneral viewGeneral = viewGenerals.get(0);
        when(bizEventsViewGeneralRepository.findByTransactionId(ViewGenerator.TRANSACTION_ID))
                .thenReturn(viewGenerals);
        String eventId = ViewGenerator.EVENT_ID + "-0";
        BizEventsViewCart mockedViewCart = ViewGenerator.generateBizEventsViewCart();
        mockedViewCart.setId(eventId);
        List<BizEventsViewCart> listOfCartView = Collections.singletonList(mockedViewCart);
        when(bizEventsViewCartRepository.getBizEventsViewCartByTransactionIdAndIdAndFilteredByTaxCode(ViewGenerator.TRANSACTION_ID, eventId, ViewGenerator.USER_TAX_CODE_WITH_TX))
                .thenReturn(listOfCartView);
        NoticeDetailResponse noticeDetailResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getPaidNoticeDetail(
                                ViewGenerator.USER_TAX_CODE_WITH_TX, ViewGenerator.TRANSACTION_ID_ON_CART_FOR_DEBTOR + "-0"));
        assertNotNull(noticeDetailResponse);
        verify(bizEventsViewGeneralRepository).findByTransactionId(ViewGenerator.TRANSACTION_ID);
        verify(bizEventsViewCartRepository).getBizEventsViewCartByTransactionIdAndIdAndFilteredByTaxCode(ViewGenerator.TRANSACTION_ID, eventId, ViewGenerator.USER_TAX_CODE_WITH_TX);
        verifyNoMoreInteractions(bizEventsViewGeneralRepository);
        verifyNoMoreInteractions(bizEventsViewCartRepository);

        assertNotNull(noticeDetailResponse);
        InfoNotice infoNotice = noticeDetailResponse.getInfoNotice();
        assertEquals(ViewGenerator.TRANSACTION_ID_ON_CART_FOR_DEBTOR + "-0", infoNotice.getEventId());
        assertEquals(viewGeneral.getAuthCode(), infoNotice.getAuthCode());
        assertEquals(viewGeneral.getRrn(), infoNotice.getRrn());
        assertEquals(viewGeneral.getTransactionDate(), infoNotice.getNoticeDate());
        assertEquals(viewGeneral.getPspName(), infoNotice.getPspName());
        assertNull(infoNotice.getWalletInfo());
        assertNull(infoNotice.getPayer());
        assertEquals(ViewGenerator.FORMATTED_AMOUNT, infoNotice.getAmount());
        assertNull(infoNotice.getFee());
        assertNull(infoNotice.getPaymentMethod());
        assertEquals(viewGeneral.getOrigin(), infoNotice.getOrigin());

        BizEventsViewCart viewCart = listOfCartView.get(0);
        it.gov.pagopa.bizeventsservice.model.response.paidnotice.CartItem cartItem = noticeDetailResponse.getCarts().get(0);
        assertCartItems(viewCart, cartItem);
    }

    private static void assertCartItems(BizEventsViewCart viewCart, it.gov.pagopa.bizeventsservice.model.response.paidnotice.CartItem cartItem) {
        assertEquals(viewCart.getSubject(), cartItem.getSubject());
        assertEquals(ViewGenerator.FORMATTED_AMOUNT, cartItem.getAmount());
        assertEquals(viewCart.getDebtor().getName(), cartItem.getDebtor().getName());
        assertEquals(viewCart.getDebtor().getTaxCode(), cartItem.getDebtor().getTaxCode());
        assertEquals(viewCart.getPayee().getName(), cartItem.getPayee().getName());
        assertEquals(viewCart.getPayee().getTaxCode(), cartItem.getPayee().getTaxCode());
        assertEquals(viewCart.getRefNumberType(), cartItem.getRefNumberType());
        assertEquals(viewCart.getRefNumberValue(), cartItem.getRefNumberValue());
    }

    @Test
    void idAndTaxCodeWithOneEventAsDebtorShouldReturnNoticeDetails() {
        BizEventsViewGeneral viewGeneral = ViewGenerator.generateBizEventsViewGeneralForDebtor(false);
        when(bizEventsViewGeneralRepository.findByTransactionId(ViewGenerator.TRANSACTION_ID))
                .thenReturn(List.of(viewGeneral));
        List<BizEventsViewCart> listOfCartView = Collections.singletonList(ViewGenerator.generateBizEventsViewCart());
        when(bizEventsViewCartRepository.getBizEventsViewCartByTransactionId(ViewGenerator.TRANSACTION_ID))
                .thenReturn(listOfCartView);
        NoticeDetailResponse noticeDetailResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getPaidNoticeDetail(
                                ViewGenerator.USER_TAX_CODE_WITH_TX, ViewGenerator.TRANSACTION_ID));
        assertNotNull(noticeDetailResponse);
        verify(bizEventsViewGeneralRepository).findByTransactionId(ViewGenerator.TRANSACTION_ID);
        verify(bizEventsViewCartRepository).getBizEventsViewCartByTransactionId(ViewGenerator.TRANSACTION_ID);
        verifyNoMoreInteractions(bizEventsViewGeneralRepository);
        verifyNoMoreInteractions(bizEventsViewCartRepository);

        assertNotNull(noticeDetailResponse);
        InfoNotice infoNotice = noticeDetailResponse.getInfoNotice();
        assertEquals(viewGeneral.getTransactionId(), infoNotice.getEventId());
        assertEquals(viewGeneral.getAuthCode(), infoNotice.getAuthCode());
        assertEquals(viewGeneral.getRrn(), infoNotice.getRrn());
        assertEquals(viewGeneral.getTransactionDate(), infoNotice.getNoticeDate());
        assertEquals(viewGeneral.getPspName(), infoNotice.getPspName());
        assertNull(infoNotice.getWalletInfo());
        assertNull(infoNotice.getPayer());
        assertEquals(ViewGenerator.FORMATTED_AMOUNT, infoNotice.getAmount());
        assertNull(infoNotice.getFee());
        assertNull(infoNotice.getPaymentMethod());
        assertEquals(viewGeneral.getOrigin(), infoNotice.getOrigin());

        BizEventsViewCart viewCart = listOfCartView.get(0);
        it.gov.pagopa.bizeventsservice.model.response.paidnotice.CartItem cartItem = noticeDetailResponse.getCarts().get(0);
        assertCartItems(viewCart, cartItem);
    }

    @Test
    void idAndTaxCodeWithNoEventShouldReturnException() {
    	when(bizEventsViewGeneralRepository.findByTransactionId(ViewGenerator.TRANSACTION_ID))
    	.thenReturn(Collections.emptyList());
    	
    	Assertions.assertThrows(AppException.class, () ->
    	transactionService.getPaidNoticeDetail(
    			ViewGenerator.USER_TAX_CODE_WITH_TX, ViewGenerator.TRANSACTION_ID));

    	verify(bizEventsViewGeneralRepository).findByTransactionId(ViewGenerator.TRANSACTION_ID);

    	BizEventsViewGeneral viewGeneral = ViewGenerator.generateBizEventsViewGeneral(false);
    	when(bizEventsViewGeneralRepository.findByTransactionId(ViewGenerator.TRANSACTION_ID))
    	.thenReturn(List.of(viewGeneral));
    	List<BizEventsViewCart> listOfCartView = new ArrayList<>();
    	when(bizEventsViewCartRepository.getBizEventsViewCartByTransactionId(ViewGenerator.TRANSACTION_ID))
    	.thenReturn(listOfCartView);

    	Assertions.assertThrows(AppException.class, () ->
    	transactionService.getPaidNoticeDetail(
    			ViewGenerator.USER_TAX_CODE_WITH_TX, ViewGenerator.TRANSACTION_ID));
    	
    	verify(bizEventsViewGeneralRepository, times(2)).findByTransactionId(ViewGenerator.TRANSACTION_ID);
        verify(bizEventsViewCartRepository).getBizEventsViewCartByTransactionId(ViewGenerator.TRANSACTION_ID);
    }

    @Test
    void idAndTaxCodeWithOneEventShouldReturnTransactionDetails() {
        List<BizEventsViewGeneral> generalViewList = Collections.singletonList(ViewGenerator.generateBizEventsViewGeneral(false));
        when(bizEventsViewGeneralRepository.findByTransactionId(ViewGenerator.TRANSACTION_ID))
                .thenReturn(generalViewList);
        List<BizEventsViewCart> listOfCartView = Collections.singletonList(ViewGenerator.generateBizEventsViewCart());
        when(bizEventsViewCartRepository.getBizEventsViewCartByTransactionId(ViewGenerator.TRANSACTION_ID))
                .thenReturn(listOfCartView);
        TransactionDetailResponse transactionDetailResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getTransactionDetails(
                                ViewGenerator.USER_TAX_CODE_WITH_TX, ViewGenerator.TRANSACTION_ID));
        assertNotNull(transactionDetailResponse);
        verify(bizEventsViewGeneralRepository).findByTransactionId(ViewGenerator.TRANSACTION_ID);
        verify(bizEventsViewCartRepository).getBizEventsViewCartByTransactionId(ViewGenerator.TRANSACTION_ID);
        verifyNoMoreInteractions(bizEventsViewGeneralRepository);
        verifyNoMoreInteractions(bizEventsViewCartRepository);

        BizEventsViewGeneral viewGeneral = generalViewList.get(0);
        assertNotNull(transactionDetailResponse);
        InfoTransactionView infoTransaction = transactionDetailResponse.getInfoTransaction();
        assertEquals(viewGeneral.getTransactionId(), infoTransaction.getTransactionId());
        assertEquals(viewGeneral.getAuthCode(), infoTransaction.getAuthCode());
        assertEquals(viewGeneral.getRrn(), infoTransaction.getRrn());
        assertEquals(viewGeneral.getTransactionDate(), infoTransaction.getTransactionDate());
        assertEquals(viewGeneral.getPspName(), infoTransaction.getPspName());
        assertEquals(viewGeneral.getWalletInfo().getAccountHolder(), infoTransaction.getWalletInfo().getAccountHolder());
        assertEquals(viewGeneral.getWalletInfo().getBrand(), infoTransaction.getWalletInfo().getBrand());
        assertEquals(viewGeneral.getWalletInfo().getBlurredNumber(), infoTransaction.getWalletInfo().getBlurredNumber());
        assertEquals(viewGeneral.getWalletInfo().getMaskedEmail(), infoTransaction.getWalletInfo().getMaskedEmail());
        assertEquals(viewGeneral.getPayer().getName(), infoTransaction.getPayer().getName());
        assertEquals(viewGeneral.getPayer().getTaxCode(), infoTransaction.getPayer().getTaxCode());
        assertEquals(ViewGenerator.FORMATTED_AMOUNT, infoTransaction.getAmount());
        assertEquals(viewGeneral.getFee(), infoTransaction.getFee());
        assertEquals(viewGeneral.getPaymentMethod(), infoTransaction.getPaymentMethod());
        assertEquals(viewGeneral.getOrigin(), infoTransaction.getOrigin());

        BizEventsViewCart viewCart = listOfCartView.get(0);
        CartItem cartItem = transactionDetailResponse.getCarts().get(0);
        assertEquals(viewCart.getSubject(), cartItem.getSubject());
        assertEquals(ViewGenerator.FORMATTED_AMOUNT, cartItem.getAmount());
        assertEquals(viewCart.getDebtor().getName(), cartItem.getDebtor().getName());
        assertEquals(viewCart.getDebtor().getTaxCode(), cartItem.getDebtor().getTaxCode());
        assertEquals(viewCart.getPayee().getName(), cartItem.getPayee().getName());
        assertEquals(viewCart.getPayee().getTaxCode(), cartItem.getPayee().getTaxCode());
        assertEquals(viewCart.getRefNumberType(), cartItem.getRefNumberType());
        assertEquals(viewCart.getRefNumberValue(), cartItem.getRefNumberValue());
    }
    
    @Test
    void idAndTaxCodeWithOneEventShouldReturnNDP004OriginType() {
        List<BizEventsViewGeneral> generalViewList = Collections.singletonList(ViewGenerator.generateBizEventsViewGeneral(false, OriginType.NDP004PROD));
        when(bizEventsViewGeneralRepository.findByTransactionId(ViewGenerator.TRANSACTION_ID))
                .thenReturn(generalViewList);
        List<BizEventsViewCart> listOfCartView = Collections.singletonList(ViewGenerator.generateBizEventsViewCart());
        when(bizEventsViewCartRepository.getBizEventsViewCartByTransactionId(ViewGenerator.TRANSACTION_ID))
                .thenReturn(listOfCartView);
        TransactionDetailResponse transactionDetailResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getTransactionDetails(
                                ViewGenerator.USER_TAX_CODE_WITH_TX, ViewGenerator.TRANSACTION_ID));
        assertNotNull(transactionDetailResponse);
        verify(bizEventsViewGeneralRepository).findByTransactionId(ViewGenerator.TRANSACTION_ID);
        verify(bizEventsViewCartRepository).getBizEventsViewCartByTransactionId(ViewGenerator.TRANSACTION_ID);
        verifyNoMoreInteractions(bizEventsViewGeneralRepository);
        verifyNoMoreInteractions(bizEventsViewCartRepository);

        BizEventsViewGeneral viewGeneral = generalViewList.get(0);
        assertNotNull(transactionDetailResponse);
        InfoTransactionView infoTransaction = transactionDetailResponse.getInfoTransaction();
        assertEquals(viewGeneral.getOrigin(), infoTransaction.getOrigin());
    }


    @Test
    void idAndTaxCodeWithMultipleCartEventsShouldReturnTransactionDetails() {
        List<BizEventsViewGeneral> generalViewList = Collections.singletonList(ViewGenerator.generateBizEventsViewGeneral(true));
        when(bizEventsViewGeneralRepository.findByTransactionId(ViewGenerator.TRANSACTION_ID))
                .thenReturn(generalViewList);
        List<BizEventsViewCart> listOfCartView = ViewGenerator.generateListOfFiveViewCart();
        when(bizEventsViewCartRepository.getBizEventsViewCartByTransactionId(ViewGenerator.TRANSACTION_ID))
                .thenReturn(listOfCartView);
        TransactionDetailResponse transactionDetailResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getTransactionDetails(
                                ViewGenerator.USER_TAX_CODE_WITH_TX, ViewGenerator.TRANSACTION_ID));
        assertNotNull(transactionDetailResponse);
        verify(bizEventsViewGeneralRepository).findByTransactionId(ViewGenerator.TRANSACTION_ID);
        verify(bizEventsViewCartRepository).getBizEventsViewCartByTransactionId(ViewGenerator.TRANSACTION_ID);
        verifyNoMoreInteractions(bizEventsViewGeneralRepository);
        verifyNoMoreInteractions(bizEventsViewCartRepository);

        assertEquals(ViewGenerator.FORMATTED_GRAND_TOTAL, transactionDetailResponse.getInfoTransaction().getAmount());
        for (int i = 0; i < transactionDetailResponse.getCarts().size(); i++) {
            BizEventsViewCart viewCart = listOfCartView.get(i);
            CartItem cartItem = transactionDetailResponse.getCarts().get(i);
            assertEquals(viewCart.getSubject(), cartItem.getSubject());
            assertEquals(ViewGenerator.FORMATTED_AMOUNT, cartItem.getAmount());
            assertEquals(viewCart.getDebtor().getName(), cartItem.getDebtor().getName());
            assertEquals(viewCart.getDebtor().getTaxCode(), cartItem.getDebtor().getTaxCode());
            assertEquals(viewCart.getPayee().getName(), cartItem.getPayee().getName());
            assertEquals(viewCart.getPayee().getTaxCode(), cartItem.getPayee().getTaxCode());
            assertEquals(viewCart.getRefNumberType(), cartItem.getRefNumberType());
            assertEquals(viewCart.getRefNumberValue(), cartItem.getRefNumberValue());
        }
    }

    @Test
    void idAndTaxCodeWithMultipleCartEventsShouldReturnNoticeDetails() {
    	BizEventsViewGeneral viewGeneral = ViewGenerator.generateBizEventsViewGeneral(true);
        when(bizEventsViewGeneralRepository.findByTransactionId(ViewGenerator.TRANSACTION_ID))
        .thenReturn(List.of(viewGeneral));
        List<BizEventsViewCart> listOfCartView = ViewGenerator.generateListOfFiveViewCart();
        when(bizEventsViewCartRepository.getBizEventsViewCartByTransactionId(ViewGenerator.TRANSACTION_ID))
                .thenReturn(listOfCartView);
        NoticeDetailResponse noticeDetailResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getPaidNoticeDetail(
                                ViewGenerator.USER_TAX_CODE_WITH_TX, ViewGenerator.TRANSACTION_ID));
        assertNotNull(noticeDetailResponse);
        verify(bizEventsViewGeneralRepository).findByTransactionId(ViewGenerator.TRANSACTION_ID);
        verify(bizEventsViewCartRepository).getBizEventsViewCartByTransactionId(ViewGenerator.TRANSACTION_ID);
        verifyNoMoreInteractions(bizEventsViewGeneralRepository);
        verifyNoMoreInteractions(bizEventsViewCartRepository);

        assertEquals(ViewGenerator.FORMATTED_GRAND_TOTAL, noticeDetailResponse.getInfoNotice().getAmount());
        for (int i = 0; i < noticeDetailResponse.getCarts().size(); i++) {
            BizEventsViewCart viewCart = listOfCartView.get(i);
            it.gov.pagopa.bizeventsservice.model.response.paidnotice.CartItem cartItem = noticeDetailResponse.getCarts().get(i);
            assertCartItems(viewCart, cartItem);
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

        assertEquals(HttpStatus.NOT_FOUND, appException.getHttpStatus());
    }

    @Test
    void transactionViewCartNotFoundThrowError() {
        List<BizEventsViewGeneral> generalViewList = Collections.singletonList(ViewGenerator.generateBizEventsViewGeneral(true));
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

        assertEquals(HttpStatus.NOT_FOUND, appException.getHttpStatus());
    }

    @Test
    void transactionViewUserDisabled() {
        List<BizEventsViewUser> viewUserList = Collections.singletonList(generateBizEventsViewUser());
        when(bizEventsViewUserRepository.getBizEventsViewUserByTaxCodeAndTransactionId(anyString(), anyString()))
                .thenReturn(viewUserList);
        Assertions.assertDoesNotThrow(() -> transactionService.disablePaidNotice(
                ViewGenerator.USER_TAX_CODE_WITH_TX, ViewGenerator.TRANSACTION_ID));

        ArgumentCaptor<List<BizEventsViewUser>> argument = ArgumentCaptor.forClass(List.class);
        verify(bizEventsViewUserRepository).saveAll(argument.capture());
        assertEquals(Boolean.TRUE, argument.getValue().get(0).getHidden());
    }
    
    @Test
    void transactionViewUserDisabledEmptyList() {
        List<BizEventsViewUser> viewUserList = new ArrayList<>();
        when(bizEventsViewUserRepository.getBizEventsViewUserByTaxCodeAndTransactionId(anyString(), anyString()))
                .thenReturn(viewUserList);
        Assertions.assertThrows(AppException.class, () -> transactionService.disablePaidNotice(
                ViewGenerator.USER_TAX_CODE_WITH_TX, ViewGenerator.TRANSACTION_ID));

        verify(bizEventsViewUserRepository, times(0)).saveAll(viewUserList);
    }
    
    @Test
    void transactionViewUserCartDisabledPayer() {

        List<BizEventsViewUser> viewUserList = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            BizEventsViewUser u = generateBizEventsViewUser();
            u.setId(u.getId() + "_" + i);
            viewUserList.add(u);
        }

        when(bizEventsViewUserRepository.getBizEventsViewUserByTaxCodeAndTransactionId(anyString(), anyString()))
                .thenReturn(viewUserList);

        Assertions.assertDoesNotThrow(() -> transactionService.disablePaidNotice(
                ViewGenerator.USER_TAX_CODE_WITH_TX, ViewGenerator.TRANSACTION_ID+"_CART_"));

        ArgumentCaptor<List<BizEventsViewUser>> argument = ArgumentCaptor.forClass(List.class);
        verify(bizEventsViewUserRepository).saveAll(argument.capture());
        argument.getValue().forEach(u -> assertEquals(Boolean.TRUE, u.getHidden()));
    }


    @Test
    void transactionViewUserCartDisabledDebtor() {

        List<BizEventsViewUser> viewUserList = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            BizEventsViewUser u = generateBizEventsViewUser();
            u.setId(u.getId() + "_" + i);
            viewUserList.add(u);
        }

        when(bizEventsViewUserRepository.findByFiscalCodeAndTransactionIdAndEventId(anyString(), anyString(), anyString()))
                .thenReturn(viewUserList);

        Assertions.assertDoesNotThrow(() -> transactionService.disablePaidNotice(
                ViewGenerator.USER_TAX_CODE_WITH_TX, ViewGenerator.TRANSACTION_ID+"_CART_"+ViewGenerator.EVENT_ID));

        ArgumentCaptor<List<BizEventsViewUser>> argument = ArgumentCaptor.forClass(List.class);
        verify(bizEventsViewUserRepository).saveAll(argument.capture());
        argument.getValue().forEach(u -> assertEquals(Boolean.TRUE, u.getHidden()));
    }

    @Test
    void transactionViewUserNotFoundThrowError() {
        when(bizEventsViewUserRepository.getBizEventsViewUserByTaxCodeAndTransactionId(anyString(), anyString()))
                .thenReturn(null);
        AppException appException =
                Assertions.assertThrows(AppException.class, () ->
                        transactionService.getTransactionDetails(
                                ViewGenerator.USER_TAX_CODE_WITH_TX, ViewGenerator.TRANSACTION_ID));
        assertEquals(HttpStatus.NOT_FOUND, appException.getHttpStatus());
    }

    @Test
    void getPDFReceiptOK() {

        BizEvent bizEvent = BizEvent.builder().id("event-id").build();
        when(bizEventsService.getBizEvent(anyString())).thenReturn(bizEvent);

        byte[] res =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getPDFReceipt(
                                VALID_FISCAL_CODE, "event-id"));

        assertEquals(receipt.length, res.length);
    }

    @Test
    void getPDFReceiptResponseOK() {

        BizEvent bizEvent = BizEvent.builder().id("event-id").build();
        when(bizEventsService.getBizEvent(anyString())).thenReturn(bizEvent);

        var res = Assertions.assertDoesNotThrow(() ->
                transactionService.getPDFReceiptResponse(VALID_FISCAL_CODE, "event-id"));

        assertEquals(200, res.getStatusCode().value());
    }

    @Test
    void getPDFReceiptForMissingEventIdOK() {

        BizEvent bizEvent = BizEvent.builder().id("missing-id").ts(OffsetDateTime.MIN).build();
        when(bizEventsService.getBizEvent(anyString())).thenReturn(bizEvent);
        Attachment attachmentDetail = mock(Attachment.class);
        AttachmentsDetailsResponse attachments = AttachmentsDetailsResponse.builder().attachments(Arrays.asList(attachmentDetail)).build();
        when(receiptClient.getAttachments(anyString(), eq("missing-id"))).thenThrow(FeignException.NotFound.class).thenReturn(attachments);

        Assertions.assertThrows(AppException.class, () ->
                transactionService.getPDFReceipt(
                        VALID_FISCAL_CODE, "missing-id"));

        verify(transactionService).getPDFReceipt(VALID_FISCAL_CODE, "missing-id");
        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> verify(generateReceiptClient).generateReceipt("missing-id")); // generateReceiptClient.generateReceipt is launched as async invocation: so, wait 2s
    }

    @Test
    void getPDFReceiptForMissingPDFFileOK() {

        BizEvent bizEvent = BizEvent.builder().id("missing-pdf-file").build();
        when(bizEventsService.getBizEvent(anyString())).thenReturn(bizEvent);
        when(receiptClient.getReceipt(anyString(), eq("missing-pdf-file"), any())).thenThrow(FeignException.NotFound.class).thenReturn(receipt);

        byte[] res =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getPDFReceipt(
                                VALID_FISCAL_CODE, "missing-pdf-file"));

        verify(transactionService).getPDFReceipt(VALID_FISCAL_CODE, "missing-pdf-file");
        verify(receiptClient).getAttachments(VALID_FISCAL_CODE, "missing-pdf-file");
        verify(generateReceiptClient).generateReceipt("missing-pdf-file");
        verify(receiptClient, times(2)).getReceipt(eq(VALID_FISCAL_CODE), eq("missing-pdf-file"), any());
        assertEquals(receipt.length, res.length);
    }

    @Test
    void getPDFReceiptForUnhandledExceptionKO() {

        BizEvent bizEvent = BizEvent.builder().id("event-id").build();
        when(bizEventsService.getBizEvent(anyString())).thenReturn(bizEvent);
        // Override @BeforeEach condition
        when(receiptClient.getAttachments(anyString(), eq("event-id"))).thenThrow(FeignException.BadRequest.class);


        Assertions.assertThrows(FeignException.BadRequest.class, () ->
                transactionService.getPDFReceipt(
                        INVALID_FISCAL_CODE, "event-id"));

        verify(receiptClient).getAttachments(INVALID_FISCAL_CODE, "event-id");
    }
}
