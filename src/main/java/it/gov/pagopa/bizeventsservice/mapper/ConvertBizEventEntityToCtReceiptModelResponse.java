package it.gov.pagopa.bizeventsservice.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javax.validation.Valid;

import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

import it.gov.pagopa.bizeventsservice.entity.BizEvent;
import it.gov.pagopa.bizeventsservice.model.response.CtReceiptModelResponse;

public class ConvertBizEventEntityToCtReceiptModelResponse implements Converter<BizEvent, CtReceiptModelResponse> {

    @Override
    public CtReceiptModelResponse convert(MappingContext<BizEvent, CtReceiptModelResponse> mappingContext) {
        @Valid BizEvent be = mappingContext.getSource();
        
        DateTimeFormatter  dfDate     = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter  dfDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        
        return CtReceiptModelResponse.builder()
        		.receiptId(be.getId())
        		.noticeNumber(be.getDebtorPosition().getNoticeNumber())
        		// TODO when available replace creditor -> idPa with the creditor -> paFiscalCode field
        		.fiscalCode(be.getCreditor().getIdPA())
        		.creditorReferenceId(be.getDebtorPosition().getIuv())
        		.paymentAmount(BigDecimal.valueOf(Double.valueOf(be.getPaymentInfo().getAmount())))
        		.description(be.getPaymentInfo().getRemittanceInformation())
        		.companyName(be.getCreditor().getCompanyName())
        		// TODO when available replace the mock value with the creditor -> officeName field
        		.officeName("mock officeName")
        		.debtor(null)
        		.transferList(null)
        		.idPSP(be.getPsp().getIdPsp())
        		// TODO when available replace the mock value with the psp -> pspFiscalCode field
        		.pspFiscalCode("mock pspFiscalCode")
        		// TODO when available replace the mock value with the psp -> pspPartitaIVA field
                .pspPartitaIVA("mock pspPartitaIVA")
                .pspCompanyName(be.getPsp().getPsp())
                .idChannel(be.getPsp().getIdChannel())
                // TODO when available replace the mock value with the psp -> channelDescription (?) field
                .channelDescription("mock channelDescription")
                .payer(null)
                .paymentMethod(be.getPaymentInfo().getPaymentMethod())
                .fee(BigDecimal.valueOf(Double.valueOf(be.getPaymentInfo().getFee())))
                .paymentDateTime(LocalDate.parse(be.getPaymentInfo().getPaymentDateTime(), dfDateTime))	
                .applicationDate(LocalDate.parse(be.getPaymentInfo().getApplicationDate(), dfDate))
                .transferDate(LocalDate.parse(be.getPaymentInfo().getTransferDate(), dfDate))
                // TODO when available replace the empty list with the valued list
                .metadata(new ArrayList<>())
                .build();
    }
}
