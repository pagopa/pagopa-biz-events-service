package it.gov.pagopa.bizeventsservice.service;

import it.gov.pagopa.bizeventsservice.entity.BizEvent;
import it.gov.pagopa.bizeventsservice.exception.AppException;
import it.gov.pagopa.bizeventsservice.model.response.transaction.CartItem;
import it.gov.pagopa.bizeventsservice.model.response.transaction.InfoTransaction;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionDetailResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListItem;
import it.gov.pagopa.bizeventsservice.entity.view.enumeration.OriginType;
import it.gov.pagopa.bizeventsservice.entity.view.enumeration.PaymentMethodType;
import it.gov.pagopa.bizeventsservice.repository.BizEventsRepository;
import it.gov.pagopa.bizeventsservice.repository.BizEventsViewCartRepository;
import it.gov.pagopa.bizeventsservice.repository.BizEventsViewGeneralRepository;
import it.gov.pagopa.bizeventsservice.service.impl.TransactionService;
import it.gov.pagopa.bizeventsservice.util.BizEventGenerator;
import it.gov.pagopa.bizeventsservice.util.TestUtil;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.util.*;

import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
public class TransactionServiceTest {

    public static final String INVALID_REMITTANCE_INFORMATION = "pagamento multibeneficiario";


    @MockBean
    private BizEventsRepository bizEventsRepository;
    @MockBean
    private BizEventsViewGeneralRepository bizEventsViewGeneralRepository;
    @MockBean
    private BizEventsViewCartRepository bizEventsViewCartRepository;

    private TransactionService transactionService;

    private String USER_TAX_CODE_WITH_TX = "AAAAAA00A00A000A";

    private List<Map<String, Object>> noCartTransactionList;

    private List<Map<String, Object>> cartTransactionList;

    private List<Map<String, Object>> cartItemTransactionList;

    @BeforeAll
    public void beforeAll() throws IOException {
        noCartTransactionList = TestUtil.readModelFromFile("biz-events/transactionListNoCart.json", List.class);
        cartTransactionList = TestUtil.readModelFromFile("biz-events/transactionListCart.json", List.class);
        cartItemTransactionList = TestUtil.readModelFromFile("biz-events/transactionCartItems.json", List.class);
    }

    @BeforeEach
    void setUp() {
        transactionService = spy(new TransactionService(bizEventsRepository, bizEventsViewGeneralRepository, bizEventsViewCartRepository));
        transactionService.setPayeeCartName("Pagamento Multiplo");
    }

    @Test
    void taxCodeWithEventsShouldReturnTransactionList() {
        when(bizEventsRepository.getTransactionPagedIds(USER_TAX_CODE_WITH_TX, 0, 5))
                .thenReturn(noCartTransactionList);
        List<TransactionListItem> transactionListItems =
                Assertions.assertDoesNotThrow(() ->
                transactionService.getTransactionList(
                        USER_TAX_CODE_WITH_TX, "0", 5));
        Assertions.assertNotNull(transactionListItems);
        Assertions.assertEquals(2, transactionListItems.size());
        Assertions.assertEquals("100.0", transactionListItems.get(0).getAmount());
        Assertions.assertEquals("100.00", transactionListItems.get(1).getAmount());
        Assertions.assertEquals("nodo-doc-dev", transactionListItems.get(0).getPayeeName());
        Assertions.assertEquals("nodo-doc-dev-2", transactionListItems.get(1).getPayeeName());
        Assertions.assertNotNull(transactionListItems);
        verify(bizEventsRepository).getTransactionPagedIds(USER_TAX_CODE_WITH_TX, 0, 5);
        verifyNoMoreInteractions(bizEventsRepository);
    }

    @Test
    void taxCodeWithEventsShouldReturnTransactionListWithCartData() {
        when(bizEventsRepository.getTransactionPagedIds(USER_TAX_CODE_WITH_TX, 0, 5))
                .thenReturn(cartTransactionList);
        when(bizEventsRepository.getCartData("b77d4987-a3e4-48d4-a2fd-af504f8b79e9"))
                .thenReturn(cartItemTransactionList);
        List<TransactionListItem> transactionListItems =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getTransactionList(
                                USER_TAX_CODE_WITH_TX, "0", 5));
        Assertions.assertNotNull(transactionListItems);
        Assertions.assertEquals(1, transactionListItems.size());
        Assertions.assertEquals("200.00", transactionListItems.get(0).getAmount());
        Assertions.assertEquals("Pagamento Multiplo", transactionListItems.get(0).getPayeeName());
        Assertions.assertNotNull(transactionListItems);
        verify(bizEventsRepository).getTransactionPagedIds(USER_TAX_CODE_WITH_TX, 0, 5);
        verify(bizEventsRepository).getCartData("b77d4987-a3e4-48d4-a2fd-af504f8b79e9");
    }

    @Test
    void transactionListGetExceptionForInvalidTaxCode() {
        Assertions.assertThrows(AppException.class, () ->
                transactionService.getTransactionList(
                        "INVALID_TAX_CODE", "0", 5));
    }

    @Test
    void transactionListGetException() {
        when(bizEventsRepository.getTransactionPagedIds(USER_TAX_CODE_WITH_TX, 0, 5))
                .thenAnswer(x -> {
                    throw new Exception();
                });
        Assertions.assertThrows(Exception.class, () ->
                        transactionService.getTransactionList(
                                USER_TAX_CODE_WITH_TX, "0", 5));
    }

    @Test
    void cartListGetException() {
        when(bizEventsRepository.getTransactionPagedIds(USER_TAX_CODE_WITH_TX, 0, 5))
                .thenReturn(cartTransactionList);
        when(bizEventsRepository.getCartData("b77d4987-a3e4-48d4-a2fd-af504f8b79e9"))
                .thenAnswer(x -> {
                    throw new Exception();
                });
        Assertions.assertThrows(Exception.class, () ->
                transactionService.getTransactionList(
                        USER_TAX_CODE_WITH_TX, "0", 5));
    }

    @Test
    void idAndTaxCodeWithOneEventShouldReturnTransactionDetails() {
        List<BizEvent> listOfBizEvents = BizEventGenerator.generateListOfValidBizEvents(1);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        TransactionDetailResponse transactionDetailResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getTransactionDetails(
                                USER_TAX_CODE_WITH_TX,  "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);

        BizEvent bizEvent = listOfBizEvents.get(0);

        Assertions.assertNotNull(transactionDetailResponse);
        InfoTransaction infoTransaction = transactionDetailResponse.getInfoTransaction();
        Assertions.assertEquals(bizEvent.getTransactionDetails().getTransaction().getTransactionId(), infoTransaction.getTransactionId());
        Assertions.assertEquals(bizEvent.getTransactionDetails().getTransaction().getNumAut(), infoTransaction.getAuthCode());
        Assertions.assertEquals(bizEvent.getTransactionDetails().getTransaction().getRrn(), infoTransaction.getRrn());
        Assertions.assertEquals(BizEventGenerator.DATE_TIME_TIMESTAMP_FORMATTED_DST_WINTER, infoTransaction.getTransactionDate());
        Assertions.assertEquals(bizEvent.getTransactionDetails().getTransaction().getPsp().getBusinessName(), infoTransaction.getPspName());
        Assertions.assertEquals(bizEvent.getTransactionDetails().getWallet().getInfo().getHolder(), infoTransaction.getWalletInfo().getAccountHolder());
        Assertions.assertEquals(bizEvent.getTransactionDetails().getWallet().getInfo().getBrand(), infoTransaction.getWalletInfo().getBrand());
        Assertions.assertEquals(bizEvent.getTransactionDetails().getWallet().getInfo().getBlurredNumber(), infoTransaction.getWalletInfo().getBlurredNumber());
        Assertions.assertEquals(bizEvent.getPayer().getFullName(), infoTransaction.getPayer().getName());
        Assertions.assertEquals(bizEvent.getPayer().getEntityUniqueIdentifierValue(), infoTransaction.getPayer().getTaxCode());
        Assertions.assertEquals(BizEventGenerator.FORMATTED_GRAND_TOTAL, infoTransaction.getAmount());
        Assertions.assertEquals(BizEventGenerator.FORMATTED_FEE, infoTransaction.getFee());
        Assertions.assertEquals(PaymentMethodType.AD, infoTransaction.getPaymentMethod());
        Assertions.assertEquals(OriginType.valueOf(BizEventGenerator.TRANSACTION_ORIGIN), infoTransaction.getOrigin());
        CartItem cartItem = transactionDetailResponse.getCarts().get(0);
        Assertions.assertEquals(bizEvent.getPaymentInfo().getRemittanceInformation(), cartItem.getSubject());
        Assertions.assertEquals(bizEvent.getPaymentInfo().getAmount(), cartItem.getAmount());
        Assertions.assertEquals(bizEvent.getDebtor().getFullName(), cartItem.getDebtor().getName());
        Assertions.assertEquals(bizEvent.getDebtor().getEntityUniqueIdentifierValue(), cartItem.getDebtor().getTaxCode());
        Assertions.assertEquals(bizEvent.getCreditor().getCompanyName(), cartItem.getPayee().getName());
        Assertions.assertEquals(bizEvent.getCreditor().getIdPA(), cartItem.getPayee().getTaxCode());
        Assertions.assertEquals(BizEventGenerator.MODEL_TYPE_IUV_TEXT, cartItem.getRefNumberType());
        Assertions.assertEquals(bizEvent.getDebtorPosition().getIuv(), cartItem.getRefNumberValue());
    }
    @Test
    void idAndTaxCodeWithOneEventShouldReturnTransactionDetailsWithDateSummer() {
        BizEvent bizEvent = BizEventGenerator.generateValidBizEventTimestampDSTSummer();
        List<BizEvent> listOfBizEvents = Collections.singletonList(bizEvent);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        TransactionDetailResponse transactionDetailResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getTransactionDetails(
                                USER_TAX_CODE_WITH_TX, "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);

        Assertions.assertNotNull(transactionDetailResponse);
        InfoTransaction infoTransaction = transactionDetailResponse.getInfoTransaction();
        Assertions.assertEquals(BizEventGenerator.DATE_TIME_TIMESTAMP_FORMATTED_DST_SUMMER, infoTransaction.getTransactionDate());
    }
    @Test
    void idAndTaxCodeWithOneEventShouldReturnTransactionDetailsWithDateMillisecondSummer() {
        BizEvent bizEvent = BizEventGenerator.generateValidBizEventTimestampMillisecondsDSTSummer();
        List<BizEvent> listOfBizEvents = Collections.singletonList(bizEvent);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        TransactionDetailResponse transactionDetailResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getTransactionDetails(
                                USER_TAX_CODE_WITH_TX, "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);

        Assertions.assertNotNull(transactionDetailResponse);
        InfoTransaction infoTransaction = transactionDetailResponse.getInfoTransaction();
        Assertions.assertEquals(BizEventGenerator.DATE_TIME_TIMESTAMP_FORMATTED_DST_SUMMER, infoTransaction.getTransactionDate());
    }
    @Test
    void idAndTaxCodeWithOneEventShouldReturnTransactionDetailsWithDateMillisecondWinter() {
        BizEvent bizEvent = BizEventGenerator.generateValidBizEventTimestampMillisecondsDSTWinter();
        List<BizEvent> listOfBizEvents = Collections.singletonList(bizEvent);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        TransactionDetailResponse transactionDetailResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getTransactionDetails(
                                USER_TAX_CODE_WITH_TX,  "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);

        Assertions.assertNotNull(transactionDetailResponse);
        InfoTransaction infoTransaction = transactionDetailResponse.getInfoTransaction();
        Assertions.assertEquals(BizEventGenerator.DATE_TIME_TIMESTAMP_FORMATTED_DST_WINTER, infoTransaction.getTransactionDate());
    }
    @Test
    void idAndTaxCodeWithOneEventShouldReturnTransactionDetailsWithPaymentMethodUnknown() {
        BizEvent bizEvent = BizEventGenerator.generateValidBizEvent(0);
        bizEvent.getPaymentInfo().setPaymentMethod("Unchecked payment method");
        List<BizEvent> listOfBizEvents = Collections.singletonList(bizEvent);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        TransactionDetailResponse transactionDetailResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getTransactionDetails(
                                USER_TAX_CODE_WITH_TX,  "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);

        Assertions.assertNotNull(transactionDetailResponse);
        Assertions.assertEquals(PaymentMethodType.UNKNOWN, transactionDetailResponse.getInfoTransaction().getPaymentMethod());
    }
    @Test
    void idAndTaxCodeWithOneEventShouldReturnTransactionDetailsWithOriginFromClientId() {
        BizEvent bizEvent = BizEventGenerator.generateValidBizEvent(0);
        bizEvent.getTransactionDetails().getTransaction().setOrigin(null);
        List<BizEvent> listOfBizEvents = Collections.singletonList(bizEvent);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        TransactionDetailResponse transactionDetailResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getTransactionDetails(
                                USER_TAX_CODE_WITH_TX,  "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);

        Assertions.assertNotNull(transactionDetailResponse);
        Assertions.assertEquals(OriginType.valueOf(BizEventGenerator.TRANSACTION_ORIGIN), transactionDetailResponse.getInfoTransaction().getOrigin());
    }
    @Test
    void idAndTaxCodeWithOneEventShouldReturnTransactionDetailsWithOriginUnknown() {
        BizEvent bizEvent = BizEventGenerator.generateValidBizEvent(0);
        bizEvent.getTransactionDetails().getTransaction().setOrigin("Unchecked transaction origin");
        bizEvent.getTransactionDetails().getInfo().setClientId("Unchecked transaction origin");
        List<BizEvent> listOfBizEvents = Collections.singletonList(bizEvent);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        TransactionDetailResponse transactionDetailResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getTransactionDetails(
                                USER_TAX_CODE_WITH_TX,  "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);

        Assertions.assertNotNull(transactionDetailResponse);
        Assertions.assertEquals(OriginType.UNKNOWN, transactionDetailResponse.getInfoTransaction().getOrigin());
    }
    @Test
    void idAndTaxCodeWithOneEventShouldReturnTransactionDetailsRrnAsPaymentToken() {
        BizEvent bizEvent = BizEventGenerator.generateValidBizEvent(0);
        bizEvent.getTransactionDetails().getTransaction().setRrn(null);
        List<BizEvent> listOfBizEvents = Collections.singletonList(bizEvent);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        TransactionDetailResponse transactionDetailResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getTransactionDetails(
                                USER_TAX_CODE_WITH_TX,  "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);

        Assertions.assertNotNull(transactionDetailResponse);
        InfoTransaction infoTransaction = transactionDetailResponse.getInfoTransaction();
        Assertions.assertEquals(bizEvent.getPaymentInfo().getPaymentToken(), infoTransaction.getRrn());
    }
    @Test
    void idAndTaxCodeWithOneEventShouldReturnTransactionDetailsRrnAsIUR() {
        BizEvent bizEvent = BizEventGenerator.generateValidBizEvent(0);
        bizEvent.getTransactionDetails().getTransaction().setRrn(null);
        bizEvent.getPaymentInfo().setPaymentToken(null);
        List<BizEvent> listOfBizEvents = Collections.singletonList(bizEvent);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        TransactionDetailResponse transactionDetailResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getTransactionDetails(
                                USER_TAX_CODE_WITH_TX,  "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);

        Assertions.assertNotNull(transactionDetailResponse);
        InfoTransaction infoTransaction = transactionDetailResponse.getInfoTransaction();
        Assertions.assertEquals(bizEvent.getPaymentInfo().getIUR(), infoTransaction.getRrn());
    }
    @Test
    void idAndTaxCodeWithOneEventShouldReturnTransactionDetailsModelTypeNotice() {
        BizEvent bizEvent = BizEventGenerator.generateValidBizEvent(0);
        bizEvent.getDebtorPosition().setModelType(BizEventGenerator.MODEL_TYPE_NOTICE_CODE);
        bizEvent.getDebtorPosition().setNoticeNumber(BizEventGenerator.NOTICE_NUMBER);
        bizEvent.getPaymentInfo().setPaymentToken(null);
        List<BizEvent> listOfBizEvents = Collections.singletonList(bizEvent);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        TransactionDetailResponse transactionDetailResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getTransactionDetails(
                                USER_TAX_CODE_WITH_TX,  "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);

        Assertions.assertNotNull(transactionDetailResponse);
        CartItem cartItem = transactionDetailResponse.getCarts().get(0);
        Assertions.assertEquals(BizEventGenerator.MODEL_TYPE_NOTICE_TEXT, cartItem.getRefNumberType());
        Assertions.assertEquals(bizEvent.getDebtorPosition().getNoticeNumber(), cartItem.getRefNumberValue());
    }
    @Test
    void idAndTaxCodeWithOneEventShouldReturnTransactionDetailsPspNameFromPsp() {
        BizEvent bizEvent = BizEventGenerator.generateValidBizEvent(0);
        bizEvent.getTransactionDetails().getTransaction().getPsp().setBusinessName(null);
        List<BizEvent> listOfBizEvents = Collections.singletonList(bizEvent);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        TransactionDetailResponse transactionDetailResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getTransactionDetails(
                                USER_TAX_CODE_WITH_TX,  "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);

        Assertions.assertNotNull(transactionDetailResponse);
        Assertions.assertEquals(bizEvent.getPsp().getPsp(), transactionDetailResponse.getInfoTransaction().getPspName());
    }
    @Test
    void idAndTaxCodeWithOneEventShouldReturnTransactionDetailsTransactionDateFromPaymentDateTime() {
        BizEvent bizEvent = BizEventGenerator.generateValidBizEvent(0);
        bizEvent.getTransactionDetails().getTransaction().setCreationDate(null);
        List<BizEvent> listOfBizEvents = Collections.singletonList(bizEvent);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        TransactionDetailResponse transactionDetailResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getTransactionDetails(
                                USER_TAX_CODE_WITH_TX,  "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);

        Assertions.assertNotNull(transactionDetailResponse);
        Assertions.assertEquals(BizEventGenerator.DATE_TIME_TIMESTAMP_FORMATTED_DST_WINTER, transactionDetailResponse.getInfoTransaction().getTransactionDate());
    }
    @Test
    void idAndTaxCodeWithOneEventShouldReturnTransactionDetailsWithRemmittanceInformationInTransferList() {
        BizEvent bizEvent = BizEventGenerator.generateValidBizEvent(0);
        bizEvent.getPaymentInfo().setRemittanceInformation(INVALID_REMITTANCE_INFORMATION);
        List<BizEvent> listOfBizEvents = Collections.singletonList(bizEvent);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        TransactionDetailResponse transactionDetailResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getTransactionDetails(
                                USER_TAX_CODE_WITH_TX,  "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);

        Assertions.assertNotNull(transactionDetailResponse);
        CartItem cartItem = transactionDetailResponse.getCarts().get(0);
        Assertions.assertEquals(BizEventGenerator.REMITTANCE_INFORMATION_TRANSFER_LIST_FORMATTED, cartItem.getSubject());
    }
    @Test
    void idAndTaxCodeWithCartEventShouldReturnTransactionDetails() {
        List<BizEvent> listOfBizEvents = BizEventGenerator.generateListOfValidBizEvents(5);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        TransactionDetailResponse transactionDetailResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getTransactionDetails(
                                USER_TAX_CODE_WITH_TX,  "eventId"));
        Assertions.assertNotNull(transactionDetailResponse);
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);

        for(int i = 0; i < transactionDetailResponse.getCarts().size(); i++){
            BizEvent bizEvent = listOfBizEvents.get(i);
            CartItem cartItem = transactionDetailResponse.getCarts().get(i);
            Assertions.assertEquals(bizEvent.getPaymentInfo().getRemittanceInformation(), cartItem.getSubject());
            Assertions.assertEquals(bizEvent.getPaymentInfo().getAmount(), cartItem.getAmount());
            Assertions.assertEquals(bizEvent.getDebtor().getFullName(), cartItem.getDebtor().getName());
            Assertions.assertEquals(bizEvent.getDebtor().getEntityUniqueIdentifierValue(), cartItem.getDebtor().getTaxCode());
            Assertions.assertEquals(bizEvent.getCreditor().getCompanyName(), cartItem.getPayee().getName());
            Assertions.assertEquals(bizEvent.getCreditor().getIdPA(), cartItem.getPayee().getTaxCode());
            Assertions.assertEquals(BizEventGenerator.MODEL_TYPE_IUV_TEXT, cartItem.getRefNumberType());
            Assertions.assertEquals(bizEvent.getDebtorPosition().getIuv(), cartItem.getRefNumberValue());
        }
    }
    @Test
    void transactionDetailsErrorMappingDebtorName() {
        BizEvent bizEvent = BizEventGenerator.generateValidBizEvent(0);
        bizEvent.getDebtor().setFullName(null);
        List<BizEvent> listOfBizEvents = Collections.singletonList(bizEvent);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        Assertions.assertThrows(AppException.class ,() ->
                        transactionService.getTransactionDetails(
                                USER_TAX_CODE_WITH_TX,  "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);
    }
    @Test
    void transactionDetailsErrorMappingDebtorTaxCode() {
        BizEvent bizEvent = BizEventGenerator.generateValidBizEvent(0);
        bizEvent.getDebtor().setEntityUniqueIdentifierValue(null);
        List<BizEvent> listOfBizEvents = Collections.singletonList(bizEvent);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        Assertions.assertThrows(AppException.class ,() ->
                transactionService.getTransactionDetails(
                        USER_TAX_CODE_WITH_TX,  "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);
    }
    @Test
    void transactionDetailsErrorMappingMissingDebtor() {
        BizEvent bizEvent = BizEventGenerator.generateValidBizEvent(0);
        bizEvent.setDebtor(null);
        List<BizEvent> listOfBizEvents = Collections.singletonList(bizEvent);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        Assertions.assertThrows(AppException.class ,() ->
                transactionService.getTransactionDetails(
                        USER_TAX_CODE_WITH_TX,  "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);
    }
    @Test
    void transactionDetailsErrorMappingPayerName() {
        BizEvent bizEvent = BizEventGenerator.generateValidBizEvent(0);
        bizEvent.getPayer().setFullName(null);
        List<BizEvent> listOfBizEvents = Collections.singletonList(bizEvent);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        Assertions.assertThrows(AppException.class ,() ->
                transactionService.getTransactionDetails(
                        USER_TAX_CODE_WITH_TX,  "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);
    }
    @Test
    void transactionDetailsErrorMappingPayerTaxCode() {
        BizEvent bizEvent = BizEventGenerator.generateValidBizEvent(0);
        bizEvent.getPayer().setEntityUniqueIdentifierValue(null);
        List<BizEvent> listOfBizEvents = Collections.singletonList(bizEvent);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        Assertions.assertThrows(AppException.class ,() ->
                transactionService.getTransactionDetails(
                        USER_TAX_CODE_WITH_TX,  "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);
    }
    @Test
    void transactionDetailsErrorMappingMissingPayer() {
        BizEvent bizEvent = BizEventGenerator.generateValidBizEvent(0);
        bizEvent.setPayer(null);
        List<BizEvent> listOfBizEvents = Collections.singletonList(bizEvent);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        Assertions.assertThrows(AppException.class ,() ->
                transactionService.getTransactionDetails(
                        USER_TAX_CODE_WITH_TX,  "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);
    }
    @Test
    void transactionDetailsErrorMappingMissingTransactionId() {
        BizEvent bizEvent = BizEventGenerator.generateValidBizEvent(0);
        bizEvent.getTransactionDetails().getTransaction().setTransactionId(null);
        List<BizEvent> listOfBizEvents = Collections.singletonList(bizEvent);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        Assertions.assertThrows(AppException.class ,() ->
                transactionService.getTransactionDetails(
                        USER_TAX_CODE_WITH_TX,  "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);
    }
    @Test
    void transactionDetailsErrorMappingMissingAuthCode() {
        BizEvent bizEvent = BizEventGenerator.generateValidBizEvent(0);
        bizEvent.getTransactionDetails().getTransaction().setNumAut(null);
        List<BizEvent> listOfBizEvents = Collections.singletonList(bizEvent);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        Assertions.assertThrows(AppException.class ,() ->
                transactionService.getTransactionDetails(
                        USER_TAX_CODE_WITH_TX,  "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);
    }
    @Test
    void transactionDetailsErrorMappingMissingRrn() {
        BizEvent bizEvent = BizEventGenerator.generateValidBizEvent(0);
        bizEvent.getTransactionDetails().getTransaction().setRrn(null);
        bizEvent.getPaymentInfo().setPaymentToken(null);
        bizEvent.getPaymentInfo().setIUR(null);
        List<BizEvent> listOfBizEvents = Collections.singletonList(bizEvent);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        Assertions.assertThrows(AppException.class ,() ->
                transactionService.getTransactionDetails(
                        USER_TAX_CODE_WITH_TX,  "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);
    }
    @Test
    void transactionDetailsErrorMappingMissingPspName() {
        BizEvent bizEvent = BizEventGenerator.generateValidBizEvent(0);
        bizEvent.getTransactionDetails().getTransaction().getPsp().setBusinessName(null);
        bizEvent.getPsp().setPsp(null);
        List<BizEvent> listOfBizEvents = Collections.singletonList(bizEvent);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        Assertions.assertThrows(AppException.class ,() ->
                transactionService.getTransactionDetails(
                        USER_TAX_CODE_WITH_TX,  "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);
    }
    @Test
    void transactionDetailsErrorMappingMissingTransactionDate() {
        BizEvent bizEvent = BizEventGenerator.generateValidBizEvent(0);
        bizEvent.getTransactionDetails().getTransaction().setCreationDate(null);
        bizEvent.getPaymentInfo().setPaymentDateTime(null);
        List<BizEvent> listOfBizEvents = Collections.singletonList(bizEvent);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        Assertions.assertThrows(AppException.class ,() ->
                transactionService.getTransactionDetails(
                        USER_TAX_CODE_WITH_TX,  "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);
    }
    @Test
    void transactionDetailsErrorMappingMissingTotalAmount() {
        BizEvent bizEvent = BizEventGenerator.generateValidBizEvent(0);
        bizEvent.getTransactionDetails().getTransaction().setGrandTotal(0L);
        bizEvent.getPaymentInfo().setAmount(null);
        List<BizEvent> listOfBizEvents = Collections.singletonList(bizEvent);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        Assertions.assertThrows(AppException.class ,() ->
                transactionService.getTransactionDetails(
                        USER_TAX_CODE_WITH_TX,  "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);
    }
    @Test
    void transactionDetailsErrorMappingMissingRemittanceInformation() {
        BizEvent bizEvent = BizEventGenerator.generateValidBizEvent(0);
        bizEvent.getPaymentInfo().setRemittanceInformation(null);
        bizEvent.setTransferList(null);
        List<BizEvent> listOfBizEvents = Collections.singletonList(bizEvent);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        Assertions.assertThrows(AppException.class ,() ->
                transactionService.getTransactionDetails(
                        USER_TAX_CODE_WITH_TX,  "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);
    }
    @Test
    void transactionDetailsErrorMappingMissingItemAmount() {
        BizEvent bizEvent = BizEventGenerator.generateValidBizEvent(0);
        bizEvent.getPaymentInfo().setAmount(null);
        List<BizEvent> listOfBizEvents = Collections.singletonList(bizEvent);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        Assertions.assertThrows(AppException.class ,() ->
                transactionService.getTransactionDetails(
                        USER_TAX_CODE_WITH_TX,  "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);
    }
    @Test
    void transactionDetailsErrorMappingPayeeName() {
        BizEvent bizEvent = BizEventGenerator.generateValidBizEvent(0);
        bizEvent.getCreditor().setCompanyName(null);
        List<BizEvent> listOfBizEvents = Collections.singletonList(bizEvent);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        Assertions.assertThrows(AppException.class ,() ->
                transactionService.getTransactionDetails(
                        USER_TAX_CODE_WITH_TX,  "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);
    }
    @Test
    void transactionDetailsErrorMappingPayeeTaxCode() {
        BizEvent bizEvent = BizEventGenerator.generateValidBizEvent(0);
        bizEvent.getCreditor().setIdPA(null);
        List<BizEvent> listOfBizEvents = Collections.singletonList(bizEvent);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        Assertions.assertThrows(AppException.class ,() ->
                transactionService.getTransactionDetails(
                        USER_TAX_CODE_WITH_TX,  "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);
    }
    @Test
    void transactionDetailsErrorMappingMissingPayee() {
        BizEvent bizEvent = BizEventGenerator.generateValidBizEvent(0);
        bizEvent.setCreditor(null);
        List<BizEvent> listOfBizEvents = Collections.singletonList(bizEvent);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        Assertions.assertThrows(AppException.class ,() ->
                transactionService.getTransactionDetails(
                        USER_TAX_CODE_WITH_TX,  "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);
    }
    @Test
    void transactionDetailsErrorMappingMissingRefNumberType() {
        BizEvent bizEvent = BizEventGenerator.generateValidBizEvent(0);
        bizEvent.getDebtorPosition().setModelType(null);
        List<BizEvent> listOfBizEvents = Collections.singletonList(bizEvent);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        Assertions.assertThrows(AppException.class ,() ->
                transactionService.getTransactionDetails(
                        USER_TAX_CODE_WITH_TX,  "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);
    }
    @Test
    void transactionDetailsErrorMappingMissingRefNumberValue() {
        BizEvent bizEvent = BizEventGenerator.generateValidBizEvent(0);
        bizEvent.getDebtorPosition().setIuv(null);
        bizEvent.getDebtorPosition().setNoticeNumber(null);
        List<BizEvent> listOfBizEvents = Collections.singletonList(bizEvent);
        when(bizEventsRepository.getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString()))
                .thenReturn(listOfBizEvents);
        Assertions.assertThrows(AppException.class ,() ->
                transactionService.getTransactionDetails(
                        USER_TAX_CODE_WITH_TX,  "eventId"));
        verify(bizEventsRepository).getBizEventByFiscalCodeAndId(eq(USER_TAX_CODE_WITH_TX), anyString());
        verifyNoMoreInteractions(bizEventsRepository);
    }
}
