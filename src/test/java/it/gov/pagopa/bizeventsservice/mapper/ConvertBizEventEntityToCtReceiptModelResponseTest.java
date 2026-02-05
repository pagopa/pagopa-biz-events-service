package it.gov.pagopa.bizeventsservice.mapper;

import it.gov.pagopa.bizeventsservice.entity.*;
import it.gov.pagopa.bizeventsservice.model.response.CtReceiptModelResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ConvertBizEventEntityToCtReceiptModelResponseTest {

    @Autowired
    private ModelMapper modelMapper;

    private BizEvent baseBizEventWithTransfers(List<Transfer> transfers) {
        return BizEvent.builder()
                .receiptId("RID")
                .debtorPosition(DebtorPosition.builder()
                        .noticeNumber("NOTICE")
                        .iuv("IUV")
                        .build())
                .creditor(Creditor.builder()
                        .idPA("IDPA")
                        .companyName("COMPANY")
                        .build())
                .psp(Psp.builder()
                        .idPsp("PSP")
                        .pspFiscalCode("PSP_FISCAL")
                        .idChannel("CHANNEL")
                        .build())
                .paymentInfo(PaymentInfo.builder()
                        .amount("16.00")
                        .remittanceInformation("desc")
                        .paymentMethod("creditCard")
                        .paymentDateTime("2026-01-01T17:20:30.952429")
                        .build())
                .transferList(transfers)
                .build();
    }

    @Test
    void shouldUseStringMBDAttachmentWhenPresent_evenIfMbdObjectPresent() {
        MBD mbd = Mockito.mock(MBD.class);
        Mockito.when(mbd.getMbdAttachment()).thenReturn("FROM_MBD");

        Transfer t = Transfer.builder()
                .idTransfer("1")
                .amount("16.10")
                .mbd(mbd)
                .mbdAttachment("FROM_STRING")
                .build();

        BizEvent be = baseBizEventWithTransfers(List.of(t));

        // when
        CtReceiptModelResponse resp = modelMapper.map(be, CtReceiptModelResponse.class);

        // then
        assertNotNull(resp.getTransferList());
        assertEquals(1, resp.getTransferList().size());
        assertEquals("FROM_STRING", resp.getTransferList().get(0).getMbdAttachment());

        assertEquals("16.10", resp.getTransferList().get(0).getTransferAmount().toPlainString());
    }

    @Test
    void shouldFallbackToMbdObjectWhenStringMBDAttachmentIsBlank() {
        MBD mbd = Mockito.mock(MBD.class);
        Mockito.when(mbd.getMbdAttachment()).thenReturn("FROM_MBD");

        Transfer t = Transfer.builder()
                .idTransfer("1")
                .amount("10.00")
                .mbd(mbd)
                .mbdAttachment("   ") // blank
                .build();

        BizEvent be = baseBizEventWithTransfers(List.of(t));

        // when
        CtReceiptModelResponse resp = modelMapper.map(be, CtReceiptModelResponse.class);

        // then
        assertEquals("FROM_MBD", resp.getTransferList().get(0).getMbdAttachment());
    }

    @Test
    void shouldReturnNullWhenNeitherStringNorMbdObjectIsPresent() {
        Transfer t = Transfer.builder()
                .idTransfer("1")
                .amount("10.00")
                .mbd(null)
                .mbdAttachment(null)
                .build();

        BizEvent be = baseBizEventWithTransfers(List.of(t));

        // when
        CtReceiptModelResponse resp = modelMapper.map(be, CtReceiptModelResponse.class);

        // then
        assertNull(resp.getTransferList().get(0).getMbdAttachment());
    }
}
