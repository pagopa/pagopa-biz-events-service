package it.gov.pagopa.bizeventsservice.service;

import it.gov.pagopa.bizeventsservice.exception.AppException;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListItem;
import it.gov.pagopa.bizeventsservice.repository.BizEventsRepository;
import it.gov.pagopa.bizeventsservice.service.impl.TransactionService;
import it.gov.pagopa.bizeventsservice.util.TestUtil;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
public class TransactionServiceTest {

    @Mock
    private BizEventsRepository bizEventsRepository;

    private TransactionService transactionService;

    private String USER_TAX_CODE_WITH_TX = "HHSNBG79D07A216W";

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
        transactionService = spy(new TransactionService(bizEventsRepository));
        transactionService.setPayeeCartName("Pagamento Multiplo");
    }

    @Test
    void taxCodeWithEventsShouldReturnTransactionList() {
        when(bizEventsRepository.getTransactionPagedIds(USER_TAX_CODE_WITH_TX, 0, 5))
                .thenReturn(noCartTransactionList);
        List<TransactionListItem> transactionListItems =
                Assertions.assertDoesNotThrow(() ->
                transactionService.getTransactionList(
                        USER_TAX_CODE_WITH_TX, 0, 5));
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
                                USER_TAX_CODE_WITH_TX, 0, 5));
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
                        "INVALID_TAX_CODE", 0, 5));
    }

    @Test
    void transactionListGetException() {
        when(bizEventsRepository.getTransactionPagedIds(USER_TAX_CODE_WITH_TX, 0, 5))
                .thenAnswer(x -> {
                    throw new Exception();
                });
        Assertions.assertThrows(Exception.class, () ->
                        transactionService.getTransactionList(
                                USER_TAX_CODE_WITH_TX, 0, 5));
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
                        USER_TAX_CODE_WITH_TX, 0, 5));
    }

}
