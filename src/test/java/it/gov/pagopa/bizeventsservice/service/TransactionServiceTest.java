package it.gov.pagopa.bizeventsservice.service;

import com.azure.spring.data.cosmos.core.query.CosmosPageRequest;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import it.gov.pagopa.bizeventsservice.client.IReceiptGeneratePDFClient;
import it.gov.pagopa.bizeventsservice.client.IReceiptGetPDFClient;
import it.gov.pagopa.bizeventsservice.entity.BizEvent;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewCart;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewGeneral;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewUser;
import it.gov.pagopa.bizeventsservice.exception.AppError;
import it.gov.pagopa.bizeventsservice.exception.AppException;
import it.gov.pagopa.bizeventsservice.model.filterandorder.Order.TransactionListOrder;
import it.gov.pagopa.bizeventsservice.model.response.paidnotice.InfoNotice;
import it.gov.pagopa.bizeventsservice.model.response.paidnotice.NoticeDetailResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListItem;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListResponse;
import it.gov.pagopa.bizeventsservice.repository.primary.BizEventsViewCartRepository;
import it.gov.pagopa.bizeventsservice.repository.primary.BizEventsViewGeneralRepository;
import it.gov.pagopa.bizeventsservice.repository.primary.BizEventsViewUserRepository;
import it.gov.pagopa.bizeventsservice.service.impl.TransactionService;
import it.gov.pagopa.bizeventsservice.util.CacheService;
import it.gov.pagopa.bizeventsservice.util.ViewGenerator;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static it.gov.pagopa.bizeventsservice.exception.enumeration.ReceiptServiceStatusCode.*;
import static it.gov.pagopa.bizeventsservice.util.ViewGenerator.EVENT_ID;
import static it.gov.pagopa.bizeventsservice.util.ViewGenerator.generateBizEventsViewUser;
import static org.junit.jupiter.api.Assertions.*;
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
    public static final String PDF_FILE_NAME = "pdfFileName";
    public static final String HEADER_FILENAME = "filename";
    public static final String EVENT_ID_CART = "eventId_CART_";

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
    private CacheService cacheService;

    private TransactionService transactionService;

    private final byte[] receipt = {69, 121, 101, 45, 62, 118, 101, 114, (byte) 196, (byte) 195, 61, 101, 98};

    @BeforeEach
    void setUp() {
        transactionService = spy(new TransactionService(bizEventsViewGeneralRepository, bizEventsViewCartRepository, bizEventsViewUserRepository,
                receiptClient, generateReceiptClient, bizEventsService, cacheService));
        ResponseEntity<byte[]> receiptPdfResponse = mock(ResponseEntity.class);
        when(receiptPdfResponse.getBody()).thenReturn(receipt);
        HttpHeaders headers = mock(HttpHeaders.class);
        when(headers.getOrDefault(eq(HEADER_FILENAME), any())).thenReturn(List.of("filename.pdf"));
        when(receiptPdfResponse.getHeaders()).thenReturn(headers);
        when(receiptClient.getReceiptPdf(anyString(), anyString())).thenReturn(receiptPdfResponse);
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
                ViewGenerator.USER_TAX_CODE_WITH_TX, ViewGenerator.TRANSACTION_ID + "_CART_"));

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
                ViewGenerator.USER_TAX_CODE_WITH_TX, ViewGenerator.TRANSACTION_ID + "_CART_" + ViewGenerator.EVENT_ID));

        ArgumentCaptor<List<BizEventsViewUser>> argument = ArgumentCaptor.forClass(List.class);
        verify(bizEventsViewUserRepository).saveAll(argument.capture());
        argument.getValue().forEach(u -> assertEquals(Boolean.TRUE, u.getHidden()));
    }

    @Test
    void getPDFReceiptResponse_OK() throws IOException {
        MultiValueMap headers = new LinkedMultiValueMap();
        headers.put(HEADER_FILENAME, List.of(PDF_FILE_NAME));
        byte[] body = "body".getBytes();
        ResponseEntity<byte[]> clientResponse = new ResponseEntity<byte[]>(
                body,
                headers,
                HttpStatus.valueOf(200));
        when(receiptClient.getReceiptPdf(VALID_FISCAL_CODE, EVENT_ID)).thenReturn(clientResponse);

        ResponseEntity<Resource> res = Assertions.assertDoesNotThrow(() ->
                transactionService.getPDFReceiptResponse(VALID_FISCAL_CODE, EVENT_ID));

        assertEquals(200, res.getStatusCode().value());
        assertTrue(res.getHeaders().getOrDefault(HttpHeaders.CONTENT_DISPOSITION, List.of("")).get(0).contains(PDF_FILE_NAME));
        assertArrayEquals(body, res.getBody().getInputStream().readAllBytes());
    }

    @Test
    void getPDFReceiptResponse_KO_EmptyBodyFromClient() {
        ResponseEntity<byte[]> clientResponse = new ResponseEntity<>(
                (byte[]) null,
                HttpStatus.valueOf(200));
        when(receiptClient.getReceiptPdf(VALID_FISCAL_CODE, EVENT_ID)).thenReturn(clientResponse);

        AppException e = Assertions.assertThrows(AppException.class, () ->
                transactionService.getPDFReceiptResponse(VALID_FISCAL_CODE, EVENT_ID));

        assertEquals(AppError.ATTACHMENT_NOT_FOUND.getCode(), e.getCode());
    }

    @Test
    void getPDFReceiptResponse_KO_ExceptionEmptyBody() {
        FeignException feignException =
                FeignException.errorStatus(
                        "getReceiptPdf",
                        Response.builder()
                                .request(Request.create(
                                        Request.HttpMethod.GET,
                                        "/receipt",
                                        Collections.emptyMap(),
                                        null,
                                        StandardCharsets.UTF_8,
                                        new RequestTemplate()
                                ))
                                .build()
                );
        when(receiptClient.getReceiptPdf(VALID_FISCAL_CODE, EVENT_ID)).thenThrow(feignException);

        Assertions.assertThrows(FeignException.class, () ->
                transactionService.getPDFReceiptResponse(VALID_FISCAL_CODE, EVENT_ID));
        verify(bizEventsService, never()).getBizEventFromLAPId(anyString());
        verify(generateReceiptClient, never()).generateReceipt(anyString());
        verify(generateReceiptClient, never()).generateReceiptCart(anyString());
    }

    @Test
    void getPDFReceiptResponse_KO_ErrorPDFS714() {
        FeignException feignException = mock(FeignException.class);
        when(feignException.contentUTF8()).thenReturn(PDFS_714.getErrorCode());
        when(receiptClient.getReceiptPdf(VALID_FISCAL_CODE, EVENT_ID)).thenThrow(feignException);
        BizEvent biz = BizEvent.builder().ts(OffsetDateTime.now().minusMinutes(10)).build();
        when(bizEventsService.getBizEventFromLAPId(EVENT_ID)).thenReturn(biz);

        AppException e = Assertions.assertThrows(AppException.class, () ->
                transactionService.getPDFReceiptResponse(VALID_FISCAL_CODE, EVENT_ID));

        assertEquals(AppError.ATTACHMENT_GENERATING.getCode(), e.getCode());
        verify(generateReceiptClient, never()).generateReceipt(anyString());
        verify(generateReceiptClient, never()).generateReceiptCart(anyString());
    }

    @Test
    void getPDFReceiptResponse_KO_Receipt_ErrorPDFS715() {
        FeignException feignException = mock(FeignException.class);
        when(feignException.contentUTF8()).thenReturn(PDFS_715.getErrorCode());
        when(receiptClient.getReceiptPdf(VALID_FISCAL_CODE, EVENT_ID)).thenThrow(feignException);
        when(generateReceiptClient.generateReceipt(EVENT_ID)).thenReturn("ok");

        AppException e = Assertions.assertThrows(AppException.class, () ->
                transactionService.getPDFReceiptResponse(VALID_FISCAL_CODE, EVENT_ID));

        assertEquals(AppError.ATTACHMENT_GENERATING.getCode(), e.getCode());
        verify(bizEventsService, never()).getBizEventFromLAPId(anyString());
        verify(generateReceiptClient, never()).generateReceiptCart(anyString());
    }

    @Test
    void getPDFReceiptResponse_KO_Cart_ErrorPDFS715() {
        FeignException feignException = mock(FeignException.class);
        when(feignException.contentUTF8()).thenReturn(PDFS_715.getErrorCode());
        when(receiptClient.getReceiptPdf(VALID_FISCAL_CODE, EVENT_ID_CART)).thenThrow(feignException);
        when(generateReceiptClient.generateReceiptCart(EVENT_ID_CART)).thenReturn("ok");

        AppException e = Assertions.assertThrows(AppException.class, () ->
                transactionService.getPDFReceiptResponse(VALID_FISCAL_CODE, EVENT_ID_CART));

        assertEquals(AppError.ATTACHMENT_GENERATING.getCode(), e.getCode());
        verify(bizEventsService, never()).getBizEventFromLAPId(anyString());
        verify(generateReceiptClient, never()).generateReceipt(anyString());
    }

    @Test
    void getPDFReceiptResponse_KO_ErrorPDFS716() {
        FeignException feignException = mock(FeignException.class);
        when(feignException.contentUTF8()).thenReturn(PDFS_716.getErrorCode());
        when(receiptClient.getReceiptPdf(VALID_FISCAL_CODE, EVENT_ID)).thenThrow(feignException);

        AppException e = Assertions.assertThrows(AppException.class, () ->
                transactionService.getPDFReceiptResponse(VALID_FISCAL_CODE, EVENT_ID));

        assertEquals(AppError.ATTACHMENT_NOT_FOUND.getCode(), e.getCode());
        verify(bizEventsService, never()).getBizEventFromLAPId(anyString());
        verify(generateReceiptClient, never()).generateReceipt(anyString());
        verify(generateReceiptClient, never()).generateReceiptCart(anyString());
    }

    @Test
    void getPDFReceiptResponse_KO_Receipt_ErrorPDFS800_OldBizEvent() {
        FeignException feignException = mock(FeignException.class);
        when(feignException.contentUTF8()).thenReturn(PDFS_800.getErrorCode());
        when(receiptClient.getReceiptPdf(VALID_FISCAL_CODE, EVENT_ID)).thenThrow(feignException);
        when(generateReceiptClient.generateReceipt(EVENT_ID)).thenReturn("ok");
        BizEvent biz = BizEvent.builder().ts(OffsetDateTime.now().minusMinutes(50)).build();
        when(bizEventsService.getBizEventFromLAPId(EVENT_ID)).thenReturn(biz);

        AppException e = Assertions.assertThrows(AppException.class, () ->
                transactionService.getPDFReceiptResponse(VALID_FISCAL_CODE, EVENT_ID));

        assertEquals(AppError.ATTACHMENT_GENERATING.getCode(), e.getCode());
        verify(bizEventsService, atMostOnce()).getBizEventFromLAPId(EVENT_ID);
        verify(generateReceiptClient, atMostOnce()).generateReceipt(EVENT_ID);
        verify(generateReceiptClient, never()).generateReceiptCart(anyString());
    }

    @Test
    void getPDFReceiptResponse_KO_Cart_ErrorPDFS801_OldBizEvent() {
        FeignException feignException = mock(FeignException.class);
        when(feignException.contentUTF8()).thenReturn(PDFS_801.getErrorCode());
        when(receiptClient.getReceiptPdf(VALID_FISCAL_CODE, EVENT_ID_CART)).thenThrow(feignException);
        when(generateReceiptClient.generateReceiptCart(EVENT_ID_CART)).thenReturn("ok");
        BizEvent biz = BizEvent.builder().ts(OffsetDateTime.now().minusMinutes(50)).build();
        when(bizEventsService.getBizEventFromLAPId(EVENT_ID_CART)).thenReturn(biz);

        AppException e = Assertions.assertThrows(AppException.class, () ->
                transactionService.getPDFReceiptResponse(VALID_FISCAL_CODE, EVENT_ID_CART));

        assertEquals(AppError.ATTACHMENT_GENERATING.getCode(), e.getCode());
        verify(bizEventsService, atMostOnce()).getBizEventFromLAPId(EVENT_ID_CART);
        verify(generateReceiptClient, atMostOnce()).generateReceiptCart(EVENT_ID_CART);
        verify(generateReceiptClient, never()).generateReceipt(anyString());
    }

    @Test
    void getPDFReceiptResponse_KO_Receipt_ErrorPDFS800_NewBizEvent() {
        FeignException feignException = mock(FeignException.class);
        when(feignException.contentUTF8()).thenReturn(PDFS_800.getErrorCode());
        when(receiptClient.getReceiptPdf(VALID_FISCAL_CODE, EVENT_ID)).thenThrow(feignException);
        BizEvent biz = BizEvent.builder().ts(OffsetDateTime.now().minusMinutes(10)).build();
        when(bizEventsService.getBizEventFromLAPId(EVENT_ID)).thenReturn(biz);

        AppException e = Assertions.assertThrows(AppException.class, () ->
                transactionService.getPDFReceiptResponse(VALID_FISCAL_CODE, EVENT_ID));

        assertEquals(AppError.ATTACHMENT_GENERATING.getCode(), e.getCode());
        verify(bizEventsService, atMostOnce()).getBizEventFromLAPId(EVENT_ID);
        verify(generateReceiptClient, never()).generateReceipt(anyString());
        verify(generateReceiptClient, never()).generateReceiptCart(anyString());
    }

    @Test
    void getPDFReceiptResponse_KO_Cart_ErrorPDFS801_NewBizEvent() {
        FeignException feignException = mock(FeignException.class);
        when(feignException.contentUTF8()).thenReturn(PDFS_801.getErrorCode());
        when(receiptClient.getReceiptPdf(VALID_FISCAL_CODE, EVENT_ID_CART)).thenThrow(feignException);
        BizEvent biz = BizEvent.builder().ts(OffsetDateTime.now().minusMinutes(10)).build();
        when(bizEventsService.getBizEventFromLAPId(EVENT_ID_CART)).thenReturn(biz);

        AppException e = Assertions.assertThrows(AppException.class, () ->
                transactionService.getPDFReceiptResponse(VALID_FISCAL_CODE, EVENT_ID_CART));

        assertEquals(AppError.ATTACHMENT_GENERATING.getCode(), e.getCode());
        verify(bizEventsService, atMostOnce()).getBizEventFromLAPId(EVENT_ID_CART);
        verify(generateReceiptClient, never()).generateReceiptCart(anyString());
        verify(generateReceiptClient, never()).generateReceipt(anyString());
    }

    @Test
    void getPDFReceiptResponse_KO_ErrorPDFS706() {
        FeignException feignException = mock(FeignException.class);
        when(feignException.contentUTF8()).thenReturn(PDFS_706.getErrorCode());
        when(receiptClient.getReceiptPdf(VALID_FISCAL_CODE, EVENT_ID)).thenThrow(feignException);

        AppException e = Assertions.assertThrows(AppException.class, () ->
                transactionService.getPDFReceiptResponse(VALID_FISCAL_CODE, EVENT_ID));

        assertEquals(AppError.INVALID_FISCAL_CODE.getCode(), e.getCode());
        verify(bizEventsService, never()).getBizEventFromLAPId(anyString());
        verify(generateReceiptClient, never()).generateReceipt(anyString());
        verify(generateReceiptClient, never()).generateReceiptCart(anyString());
    }

    @Test
    void getPDFReceiptResponse_KO_GenericError() {
        FeignException feignException =
                FeignException.errorStatus(
                        "getReceiptPdf",
                        Response.builder()
                                .request(Request.create(
                                        Request.HttpMethod.GET,
                                        "/receipt",
                                        Collections.emptyMap(),
                                        "genericError".getBytes(),
                                        StandardCharsets.UTF_8,
                                        new RequestTemplate()
                                ))
                                .build()
                );
        when(receiptClient.getReceiptPdf(VALID_FISCAL_CODE, EVENT_ID)).thenThrow(feignException);

        Assertions.assertThrows(FeignException.class, () ->
                transactionService.getPDFReceiptResponse(VALID_FISCAL_CODE, EVENT_ID));

        verify(bizEventsService, never()).getBizEventFromLAPId(anyString());
        verify(generateReceiptClient, never()).generateReceipt(anyString());
        verify(generateReceiptClient, never()).generateReceiptCart(anyString());
    }
}
