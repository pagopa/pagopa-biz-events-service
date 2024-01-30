package it.gov.pagopa.bizeventsservice.service;

import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewCart;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewGeneral;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewUser;
import it.gov.pagopa.bizeventsservice.exception.AppException;
import it.gov.pagopa.bizeventsservice.model.response.transaction.CartItem;
import it.gov.pagopa.bizeventsservice.model.response.transaction.InfoTransaction;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionDetailResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListItem;
import it.gov.pagopa.bizeventsservice.repository.BizEventsRepository;
import it.gov.pagopa.bizeventsservice.repository.BizEventsViewCartRepository;
import it.gov.pagopa.bizeventsservice.repository.BizEventsViewGeneralRepository;
import it.gov.pagopa.bizeventsservice.repository.BizEventsViewUserRepository;
import it.gov.pagopa.bizeventsservice.service.impl.TransactionService;
import it.gov.pagopa.bizeventsservice.util.TestUtil;
import it.gov.pagopa.bizeventsservice.util.ViewGenerator;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.*;

import static it.gov.pagopa.bizeventsservice.util.ViewGenerator.generateBizEventsViewUser;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
public class TransactionServiceTest {
    public static final String INVALID_FISCAL_CODE = "invalidFiscalCode";

    @MockBean
    private BizEventsRepository bizEventsRepository;
    @MockBean
    private BizEventsViewGeneralRepository bizEventsViewGeneralRepository;
    @MockBean
    private BizEventsViewCartRepository bizEventsViewCartRepository;

    @Mock
    private BizEventsViewUserRepository bizEventsViewUserRepository;

    private TransactionService transactionService;



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
        transactionService = spy(new TransactionService(
                bizEventsRepository, bizEventsViewGeneralRepository,
                bizEventsViewCartRepository, bizEventsViewUserRepository)
        );
        transactionService.setPayeeCartName("Pagamento Multiplo");
    }

    @Test
    void taxCodeWithEventsShouldReturnTransactionList() {
        when(bizEventsRepository.getTransactionPagedIds(ViewGenerator.USER_TAX_CODE_WITH_TX, 0, 5))
                .thenReturn(noCartTransactionList);
        List<TransactionListItem> transactionListItems =
                Assertions.assertDoesNotThrow(() ->
                transactionService.getTransactionList(
                        ViewGenerator.USER_TAX_CODE_WITH_TX, "0", 5));
        Assertions.assertNotNull(transactionListItems);
        Assertions.assertEquals(2, transactionListItems.size());
        Assertions.assertEquals("100.0", transactionListItems.get(0).getAmount());
        Assertions.assertEquals("100.00", transactionListItems.get(1).getAmount());
        Assertions.assertEquals("nodo-doc-dev", transactionListItems.get(0).getPayeeName());
        Assertions.assertEquals("nodo-doc-dev-2", transactionListItems.get(1).getPayeeName());
        Assertions.assertNotNull(transactionListItems);
        verify(bizEventsRepository).getTransactionPagedIds(ViewGenerator.USER_TAX_CODE_WITH_TX, 0, 5);
        verifyNoMoreInteractions(bizEventsRepository);
    }

    @Test
    void taxCodeWithEventsShouldReturnTransactionListWithCartData() {
        when(bizEventsRepository.getTransactionPagedIds(ViewGenerator.USER_TAX_CODE_WITH_TX, 0, 5))
                .thenReturn(cartTransactionList);
        when(bizEventsRepository.getCartData("b77d4987-a3e4-48d4-a2fd-af504f8b79e9"))
                .thenReturn(cartItemTransactionList);
        List<TransactionListItem> transactionListItems =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getTransactionList(
                                ViewGenerator.USER_TAX_CODE_WITH_TX, "0", 5));
        Assertions.assertNotNull(transactionListItems);
        Assertions.assertEquals(1, transactionListItems.size());
        Assertions.assertEquals("200.00", transactionListItems.get(0).getAmount());
        Assertions.assertEquals("Pagamento Multiplo", transactionListItems.get(0).getPayeeName());
        Assertions.assertNotNull(transactionListItems);
        verify(bizEventsRepository).getTransactionPagedIds(ViewGenerator.USER_TAX_CODE_WITH_TX, 0, 5);
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
        when(bizEventsRepository.getTransactionPagedIds(ViewGenerator.USER_TAX_CODE_WITH_TX, 0, 5))
                .thenAnswer(x -> {
                    throw new Exception();
                });
        Assertions.assertThrows(Exception.class, () ->
                        transactionService.getTransactionList(
                                ViewGenerator.USER_TAX_CODE_WITH_TX, "0", 5));
    }

    @Test
    void cartListGetException() {
        when(bizEventsRepository.getTransactionPagedIds(ViewGenerator.USER_TAX_CODE_WITH_TX, 0, 5))
                .thenReturn(cartTransactionList);
        when(bizEventsRepository.getCartData("b77d4987-a3e4-48d4-a2fd-af504f8b79e9"))
                .thenAnswer(x -> {
                    throw new Exception();
                });
        Assertions.assertThrows(Exception.class, () ->
                transactionService.getTransactionList(
                        ViewGenerator.USER_TAX_CODE_WITH_TX, "0", 5));
    }

    @Test
    void idAndTaxCodeWithOneEventShouldReturnTransactionDetails() {
        List<BizEventsViewGeneral> listOfGeneralView = Collections.singletonList(ViewGenerator.generateBizEventsViewGeneral());
        when(bizEventsViewGeneralRepository.getBizEventsViewGeneralByTransactionId(ViewGenerator.TRANSACTION_ID))
                .thenReturn(listOfGeneralView);
        List<BizEventsViewCart> listOfCartView = Collections.singletonList(ViewGenerator.generateBizEventsViewCart());
        when(bizEventsViewCartRepository.getBizEventsViewCartByTransactionIdAndFilteredByFiscalCode(ViewGenerator.TRANSACTION_ID, ViewGenerator.USER_TAX_CODE_WITH_TX))
                .thenReturn(listOfCartView);
        TransactionDetailResponse transactionDetailResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getTransactionDetails(
                                ViewGenerator.USER_TAX_CODE_WITH_TX, ViewGenerator.TRANSACTION_ID));
        Assertions.assertNotNull(transactionDetailResponse);
        verify(bizEventsViewGeneralRepository).getBizEventsViewGeneralByTransactionId(ViewGenerator.TRANSACTION_ID);
        verify(bizEventsViewCartRepository).getBizEventsViewCartByTransactionIdAndFilteredByFiscalCode(ViewGenerator.TRANSACTION_ID, ViewGenerator.USER_TAX_CODE_WITH_TX);
        verifyNoMoreInteractions(bizEventsViewGeneralRepository);
        verifyNoMoreInteractions(bizEventsViewCartRepository);

        BizEventsViewGeneral viewGeneral = listOfGeneralView.get(0);
        Assertions.assertNotNull(transactionDetailResponse);
        InfoTransaction infoTransaction = transactionDetailResponse.getInfoTransaction();
        Assertions.assertEquals(viewGeneral.getTransactionId(), infoTransaction.getTransactionId());
        Assertions.assertEquals(viewGeneral.getAuthCode(), infoTransaction.getAuthCode());
        Assertions.assertEquals(viewGeneral.getRrn(), infoTransaction.getRrn());
        Assertions.assertEquals(viewGeneral.getTransactionDate(), infoTransaction.getTransactionDate());
        Assertions.assertEquals(viewGeneral.getPspName(), infoTransaction.getPspName());
        Assertions.assertEquals(viewGeneral.getWalletInfo().getAccountHolder(), infoTransaction.getWalletInfo().getAccountHolder());
        Assertions.assertEquals(viewGeneral.getWalletInfo().getBrand(), infoTransaction.getWalletInfo().getBrand());
        Assertions.assertEquals(viewGeneral.getWalletInfo().getBlurredNumber(), infoTransaction.getWalletInfo().getBlurredNumber());
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
        List<BizEventsViewGeneral> listOfGeneralView = Collections.singletonList(ViewGenerator.generateBizEventsViewGeneral());
        when(bizEventsViewGeneralRepository.getBizEventsViewGeneralByTransactionId(ViewGenerator.TRANSACTION_ID))
                .thenReturn(listOfGeneralView);
        List<BizEventsViewCart> listOfCartView = ViewGenerator.generateListOfFiveViewCart();
        when(bizEventsViewCartRepository.getBizEventsViewCartByTransactionIdAndFilteredByFiscalCode(ViewGenerator.TRANSACTION_ID, ViewGenerator.USER_TAX_CODE_WITH_TX))
                .thenReturn(listOfCartView);
        TransactionDetailResponse transactionDetailResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getTransactionDetails(
                                ViewGenerator.USER_TAX_CODE_WITH_TX, ViewGenerator.TRANSACTION_ID));
        Assertions.assertNotNull(transactionDetailResponse);
        verify(bizEventsViewGeneralRepository).getBizEventsViewGeneralByTransactionId(ViewGenerator.TRANSACTION_ID);
        verify(bizEventsViewCartRepository).getBizEventsViewCartByTransactionIdAndFilteredByFiscalCode(ViewGenerator.TRANSACTION_ID, ViewGenerator.USER_TAX_CODE_WITH_TX);
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
    void transactionDetailsThrowErrorForInvalidFiscalCode() {
        AppException appException =
                Assertions.assertThrows(AppException.class,() ->
                        transactionService.getTransactionDetails(
                                INVALID_FISCAL_CODE, ViewGenerator.TRANSACTION_ID));
        verifyNoInteractions(bizEventsViewGeneralRepository);
        verifyNoInteractions(bizEventsViewCartRepository);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, appException.getHttpStatus());
    }
    @Test
    void transactionViewGeneralNotFoundThrowError() {
        List<BizEventsViewGeneral> listOfGeneralView = new ArrayList<>();
        when(bizEventsViewGeneralRepository.getBizEventsViewGeneralByTransactionId(ViewGenerator.TRANSACTION_ID))
                .thenReturn(listOfGeneralView);
        AppException appException =
                Assertions.assertThrows(AppException.class, () ->
                        transactionService.getTransactionDetails(
                                ViewGenerator.USER_TAX_CODE_WITH_TX, ViewGenerator.TRANSACTION_ID));
        verify(bizEventsViewGeneralRepository).getBizEventsViewGeneralByTransactionId(ViewGenerator.TRANSACTION_ID);
        verifyNoMoreInteractions(bizEventsViewGeneralRepository);
        verifyNoInteractions(bizEventsViewCartRepository);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, appException.getHttpStatus());
    }
    @Test
    void transactionViewCartNotFoundThrowError() {
        List<BizEventsViewGeneral> listOfGeneralView = Collections.singletonList(ViewGenerator.generateBizEventsViewGeneral());
        when(bizEventsViewGeneralRepository.getBizEventsViewGeneralByTransactionId(ViewGenerator.TRANSACTION_ID))
                .thenReturn(listOfGeneralView);
        List<BizEventsViewCart> listOfCartView = new ArrayList<>();
        when(bizEventsViewCartRepository.getBizEventsViewCartByTransactionIdAndFilteredByFiscalCode(ViewGenerator.TRANSACTION_ID, ViewGenerator.USER_TAX_CODE_WITH_TX))
                .thenReturn(listOfCartView);
        AppException appException =
                Assertions.assertThrows(AppException.class, () ->
                        transactionService.getTransactionDetails(
                                ViewGenerator.USER_TAX_CODE_WITH_TX, ViewGenerator.TRANSACTION_ID));
        verify(bizEventsViewGeneralRepository).getBizEventsViewGeneralByTransactionId(ViewGenerator.TRANSACTION_ID);
        verify(bizEventsViewCartRepository).getBizEventsViewCartByTransactionIdAndFilteredByFiscalCode(ViewGenerator.TRANSACTION_ID, ViewGenerator.USER_TAX_CODE_WITH_TX);
        verifyNoMoreInteractions(bizEventsViewGeneralRepository);
        verifyNoMoreInteractions(bizEventsViewCartRepository);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, appException.getHttpStatus());
    }

    @Test
    public void transactionViewUserDisabled() {
        BizEventsViewUser viewUser = generateBizEventsViewUser();
        when(bizEventsViewUserRepository.getBizEventsViewUserByTaxCodeAndTransactionId(anyString(),anyString()))
                .thenReturn(viewUser);
        Assertions.assertDoesNotThrow(() -> transactionService.disableTransaction(
                ViewGenerator.USER_TAX_CODE_WITH_TX, ViewGenerator.TRANSACTION_ID));
        viewUser.setHidden(true);
        verify(bizEventsViewUserRepository).save(viewUser);
    }

    @Test
    public void transactionViewUserNotFoundThrowError() {
        when(bizEventsViewUserRepository.getBizEventsViewUserByTaxCodeAndTransactionId(anyString(),anyString()))
                .thenReturn(null);
        AppException appException =
                Assertions.assertThrows(AppException.class, () ->
                        transactionService.getTransactionDetails(
                                ViewGenerator.USER_TAX_CODE_WITH_TX, ViewGenerator.TRANSACTION_ID));
        Assertions.assertEquals(HttpStatus.NOT_FOUND, appException.getHttpStatus());
    }

    @Test
    void transactionUserViewThrowErrorForInvalidFiscalCode() {
        AppException appException =
                Assertions.assertThrows(AppException.class,() ->
                        transactionService.disableTransaction(
                                INVALID_FISCAL_CODE, ViewGenerator.TRANSACTION_ID));
        verifyNoInteractions(bizEventsViewUserRepository);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, appException.getHttpStatus());
    }

}
