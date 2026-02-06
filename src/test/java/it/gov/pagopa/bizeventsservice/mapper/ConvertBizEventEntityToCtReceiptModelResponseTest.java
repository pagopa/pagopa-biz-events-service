package it.gov.pagopa.bizeventsservice.mapper;

import it.gov.pagopa.bizeventsservice.entity.*;
import it.gov.pagopa.bizeventsservice.model.response.CtReceiptModelResponse;
import org.junit.jupiter.api.Test;
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
    void shouldMapMbdAttachmentStringWhenPresent() {
        Transfer t = Transfer.builder()
                .idTransfer("1")
                .amount("16.10")
                .mbdAttachment("FROM_STRING")
                .build();

        BizEvent be = baseBizEventWithTransfers(List.of(t));

        CtReceiptModelResponse resp = modelMapper.map(be, CtReceiptModelResponse.class);

        assertNotNull(resp.getTransferList());
        assertEquals(1, resp.getTransferList().size());
        assertEquals("FROM_STRING", resp.getTransferList().get(0).getMbdAttachment());
        assertEquals("16.10", resp.getTransferList().get(0).getTransferAmount().toPlainString());
    }

    @Test
    void shouldMapMbdAttachmentAsBlankIfBlankInEntity() {
        Transfer t = Transfer.builder()
                .idTransfer("1")
                .amount("10.00")
                .mbdAttachment("   ")
                .build();

        BizEvent be = baseBizEventWithTransfers(List.of(t));

        CtReceiptModelResponse resp = modelMapper.map(be, CtReceiptModelResponse.class);

        assertEquals("   ", resp.getTransferList().get(0).getMbdAttachment());
    }

    @Test
    void shouldReturnNullWhenMbdAttachmentIsNull() {
        Transfer t = Transfer.builder()
                .idTransfer("1")
                .amount("10.00")
                .mbdAttachment(null)
                .build();

        BizEvent be = baseBizEventWithTransfers(List.of(t));

        CtReceiptModelResponse resp = modelMapper.map(be, CtReceiptModelResponse.class);

        assertNull(resp.getTransferList().get(0).getMbdAttachment());
    }
    
    @Test
    void shouldSetTransferListNullWhenSourceTransferListIsNull() {
        BizEvent be = baseBizEventWithTransfers(null);

        CtReceiptModelResponse resp = modelMapper.map(be, CtReceiptModelResponse.class);

        assertNull(resp.getTransferList());
    }

    @Test
    void shouldSetTransferListNullWhenSourceTransferListIsEmpty() {
        BizEvent be = baseBizEventWithTransfers(List.of());

        CtReceiptModelResponse resp = modelMapper.map(be, CtReceiptModelResponse.class);

        assertNull(resp.getTransferList());
    }
    
    @Test
    void shouldSetTransferAmountNullWhenAmountIsNull() {
        Transfer t = Transfer.builder()
                .idTransfer("1")
                .amount(null)
                .mbdAttachment("X")
                .build();

        BizEvent be = baseBizEventWithTransfers(List.of(t));

        CtReceiptModelResponse resp = modelMapper.map(be, CtReceiptModelResponse.class);

        assertNull(resp.getTransferList().get(0).getTransferAmount());
    }

    @Test
    void shouldSetTransferAmountNullWhenAmountIsBlank() {
        Transfer t = Transfer.builder()
                .idTransfer("1")
                .amount("   ")
                .mbdAttachment("X")
                .build();

        BizEvent be = baseBizEventWithTransfers(List.of(t));

        CtReceiptModelResponse resp = modelMapper.map(be, CtReceiptModelResponse.class);

        assertNull(resp.getTransferList().get(0).getTransferAmount());
    }
    
    @Test
    void shouldMapPrimaryCiIncurredFeeWhenPresent() {
        BizEvent be = baseBizEventWithTransfers(List.of(
                Transfer.builder().idTransfer("1").amount("1.00").build()
        ));
        be.getPaymentInfo().setPrimaryCiIncurredFee("0.12");

        CtReceiptModelResponse resp = modelMapper.map(be, CtReceiptModelResponse.class);

        assertNotNull(resp.getPrimaryCiIncurredFee());
        assertEquals("0.12", resp.getPrimaryCiIncurredFee().toPlainString());
    }

    @Test
    void shouldSetPrimaryCiIncurredFeeNullWhenMissing() {
        BizEvent be = baseBizEventWithTransfers(List.of(
                Transfer.builder().idTransfer("1").amount("1.00").build()
        ));
        be.getPaymentInfo().setPrimaryCiIncurredFee(null);

        CtReceiptModelResponse resp = modelMapper.map(be, CtReceiptModelResponse.class);

        assertNull(resp.getPrimaryCiIncurredFee());
    }

}