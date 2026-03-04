package it.gov.pagopa.bizeventsservice.controller;

import it.gov.pagopa.bizeventsservice.exception.AppError;
import it.gov.pagopa.bizeventsservice.exception.AppException;
import it.gov.pagopa.bizeventsservice.service.ITransactionService;
import it.gov.pagopa.bizeventsservice.util.ViewGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TransactionsControllerTest {

    public static final String INVALID_FISCAL_CODE = "INVALID_TX_FISCAL_CODE";
    public static final String VALID_FISCAL_CODE = "AAAAAA00A00A000A";
    public static final String FISCAL_CODE_HEADER_KEY = "x-fiscal-code";
    private static final String ORGANIZATION_FISCAL_CODE = "12345678901";
    private static final String NAV = "NAV131546213164";
    private static final String TRANSACTIONS_ORGANIZATION_NAV_CF_PATH = String.format("/transactions/organizations/%s/notices/%s", ORGANIZATION_FISCAL_CODE, NAV);

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ITransactionService transactionService;


    @Test
    void getPaidNoticeDetailByCfOrgAndNavAndDebtorFiscalCode_ShouldReturnOK() throws Exception {
        when(transactionService.getCartItemByCfOrgAndNavAndDebtorFiscalCode(
                ORGANIZATION_FISCAL_CODE, NAV, VALID_FISCAL_CODE))
                .thenReturn(ViewGenerator.generateCartItemView());

        mvc.perform(get(TRANSACTIONS_ORGANIZATION_NAV_CF_PATH, ORGANIZATION_FISCAL_CODE, NAV)
                        .header(FISCAL_CODE_HEADER_KEY, VALID_FISCAL_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(transactionService).getCartItemByCfOrgAndNavAndDebtorFiscalCode(
                ORGANIZATION_FISCAL_CODE, NAV, VALID_FISCAL_CODE);
    }

    @Test
    void getPaidNoticeDetailByCfOrgAndNavAndDebtorFiscalCode_ShouldReturnBadRequestIfHeaderMissing() throws Exception {
        mvc.perform(get(TRANSACTIONS_ORGANIZATION_NAV_CF_PATH, ORGANIZATION_FISCAL_CODE, NAV)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPaidNoticeDetailByCfOrgAndNavAndDebtorFiscalCode_ShouldReturnErrorForInvalidFiscalCode() throws Exception {
        when(transactionService.getCartItemByCfOrgAndNavAndDebtorFiscalCode(
                ORGANIZATION_FISCAL_CODE, NAV, INVALID_FISCAL_CODE))
                .thenAnswer(invocation -> {
                    throw new AppException(AppError.INVALID_FISCAL_CODE, INVALID_FISCAL_CODE);
                });

        mvc.perform(get(TRANSACTIONS_ORGANIZATION_NAV_CF_PATH, ORGANIZATION_FISCAL_CODE, NAV)
                        .header(FISCAL_CODE_HEADER_KEY, INVALID_FISCAL_CODE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

}
