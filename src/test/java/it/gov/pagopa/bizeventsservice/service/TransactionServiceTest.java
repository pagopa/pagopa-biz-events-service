package it.gov.pagopa.bizeventsservice.service;

import com.azure.cosmos.models.PartitionKey;
import com.azure.spring.data.cosmos.core.query.CosmosPageRequest;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewCart;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewGeneral;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewUser;
import it.gov.pagopa.bizeventsservice.exception.AppException;
import it.gov.pagopa.bizeventsservice.model.response.transaction.*;
import it.gov.pagopa.bizeventsservice.repository.BizEventsViewCartRepository;
import it.gov.pagopa.bizeventsservice.repository.BizEventsViewGeneralRepository;
import it.gov.pagopa.bizeventsservice.repository.BizEventsViewUserRepository;
import it.gov.pagopa.bizeventsservice.service.impl.TransactionService;
import it.gov.pagopa.bizeventsservice.util.ViewGenerator;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.*;

import static it.gov.pagopa.bizeventsservice.util.ViewGenerator.generateBizEventsViewUser;
import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
public class TransactionServiceTest {
    public static final String INVALID_FISCAL_CODE = "invalidFiscalCode";
    public static final int PAGE_SIZE = 5;
    public static final String CONTINUATION_TOKEN = "0";

    @MockBean
    private BizEventsViewUserRepository bizEventsViewUserRepository;
    @MockBean
    private BizEventsViewGeneralRepository bizEventsViewGeneralRepository;
    @MockBean
    private BizEventsViewCartRepository bizEventsViewCartRepository;

    private TransactionService transactionService;
    @Value("transaction.payee.cartName")
    private static String payeeCartName;

    @BeforeEach
    void setUp() {
        transactionService = spy(new TransactionService(bizEventsViewGeneralRepository, bizEventsViewCartRepository, bizEventsViewUserRepository));
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
        when(bizEventsViewUserRepository.getBizEventsViewUserByTaxCode(eq(ViewGenerator.USER_TAX_CODE_WITH_TX), any()))
                .thenReturn(pageOfViewUser);
        List<BizEventsViewCart> listOfViewCart = Collections.singletonList(ViewGenerator.generateBizEventsViewCart());
        when(bizEventsViewCartRepository.getBizEventsViewCartByTransactionIdAndFilteredByTaxCode(
                anyString(), eq(ViewGenerator.USER_TAX_CODE_WITH_TX)))
                .thenReturn(listOfViewCart);
        TransactionListResponse transactionListResponse =
                Assertions.assertDoesNotThrow(() ->
                transactionService.getTransactionList(
                        ViewGenerator.USER_TAX_CODE_WITH_TX, CONTINUATION_TOKEN, PAGE_SIZE));
        Assertions.assertEquals(CONTINUATION_TOKEN, transactionListResponse.getContinuationToken());
        List<TransactionListItem> transactionListItems = transactionListResponse.getTransactionList();
        Assertions.assertNotNull(transactionListItems);
        Assertions.assertEquals(listOfViewUser.size(), transactionListItems.size());

        for(TransactionListItem listItem : transactionListItems){
            Assertions.assertEquals(ViewGenerator.FORMATTED_AMOUNT, listItem.getAmount());
            Assertions.assertEquals(listOfViewCart.get(0).getPayee().getName(), listItem.getPayeeName());
            Assertions.assertEquals(listOfViewCart.get(0).getPayee().getTaxCode(), listItem.getPayeeTaxCode());
        }

        verify(bizEventsViewUserRepository).getBizEventsViewUserByTaxCode(eq(ViewGenerator.USER_TAX_CODE_WITH_TX), any());
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
        when(bizEventsViewUserRepository.getBizEventsViewUserByTaxCode(eq(ViewGenerator.USER_TAX_CODE_WITH_TX), any()))
                .thenReturn(pageOfViewUser);
        List<BizEventsViewCart> listOfViewCart = ViewGenerator.generateListOfFiveViewCart();
        when(bizEventsViewCartRepository.getBizEventsViewCartByTransactionIdAndFilteredByTaxCode(
                anyString(), eq(ViewGenerator.USER_TAX_CODE_WITH_TX)))
                .thenReturn(listOfViewCart);
        TransactionListResponse transactionListResponse =
                Assertions.assertDoesNotThrow(() ->
                        transactionService.getTransactionList(
                                ViewGenerator.USER_TAX_CODE_WITH_TX, CONTINUATION_TOKEN, PAGE_SIZE));
        Assertions.assertEquals(CONTINUATION_TOKEN, transactionListResponse.getContinuationToken());
        List<TransactionListItem> transactionListItems = transactionListResponse.getTransactionList();
        Assertions.assertNotNull(transactionListItems);
        Assertions.assertEquals(listOfViewUser.size(), transactionListItems.size());

        for(TransactionListItem listItem : transactionListItems){
            Assertions.assertEquals(ViewGenerator.FORMATTED_GRAND_TOTAL, listItem.getAmount());
            Assertions.assertEquals(payeeCartName, listItem.getPayeeName());
            Assertions.assertEquals("", listItem.getPayeeTaxCode());
        }

        verify(bizEventsViewUserRepository).getBizEventsViewUserByTaxCode(eq(ViewGenerator.USER_TAX_CODE_WITH_TX), any());
        verifyNoMoreInteractions(bizEventsViewUserRepository);
    }

    @Test
    void transactionListGetExceptionForInvalidTaxCode() {
        Assertions.assertThrows(AppException.class, () ->
                transactionService.getTransactionList(
                        INVALID_FISCAL_CODE, CONTINUATION_TOKEN, PAGE_SIZE));
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
    public void transactionViewUserDisabled() {
        List<BizEventsViewUser> viewUserList = Collections.singletonList(generateBizEventsViewUser());
        when(bizEventsViewUserRepository.getBizEventsViewUserByTaxCodeAndTransactionId(anyString(),anyString()))
                .thenReturn(viewUserList);
        Assertions.assertDoesNotThrow(() -> transactionService.disableTransaction(
                ViewGenerator.USER_TAX_CODE_WITH_TX, ViewGenerator.TRANSACTION_ID));
        viewUserList.get(0).setHidden(true);
        verify(bizEventsViewUserRepository).save(viewUserList.get(0));
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
