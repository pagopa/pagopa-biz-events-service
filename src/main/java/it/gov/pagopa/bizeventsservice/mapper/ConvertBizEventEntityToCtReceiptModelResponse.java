package it.gov.pagopa.bizeventsservice.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

import it.gov.pagopa.bizeventsservice.entity.BizEvent;
import it.gov.pagopa.bizeventsservice.entity.Transfer;
import it.gov.pagopa.bizeventsservice.model.response.CtReceiptModelResponse;
import it.gov.pagopa.bizeventsservice.model.response.Debtor;
import it.gov.pagopa.bizeventsservice.model.response.Payer;
import it.gov.pagopa.bizeventsservice.model.response.TransferPA;
import it.gov.pagopa.bizeventsservice.model.response.enumeration.EntityUniqueIdentifierType;

public class ConvertBizEventEntityToCtReceiptModelResponse implements Converter<BizEvent, CtReceiptModelResponse> {

    @Override
    public CtReceiptModelResponse convert(MappingContext<BizEvent, CtReceiptModelResponse> mappingContext) {
        @Valid BizEvent be = mappingContext.getSource();
        
        DateTimeFormatter  dfDate     = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter  dfDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        Debtor ctReceiptDebtor = null;
        Payer ctReceiptPayer   = null;
        List<TransferPA> ctTransferListPA = null;
        
        if (null != be.getDebtor()) {
        	ctReceiptDebtor = Debtor.builder()
        			.entityUniqueIdentifierType(EntityUniqueIdentifierType.valueOf(be.getDebtor().getEntityUniqueIdentifierType()))
        			.entityUniqueIdentifierValue(be.getDebtor().getEntityUniqueIdentifierValue())
        			.fullName(be.getDebtor().getFullName())
        			// TODO when available replace the mock value with the debtor  -> streetName field
        			.streetName(null)
        			// TODO when available replace the mock value with the debtor -> civicNumber field
        			.civicNumber(null)
        			// TODO when available replace the mock value with the debtor -> postalCode field
        			.postalCode(null)
        			// TODO when available replace the mock value with the debtor -> city field
        			.city(null)
        			// TODO when available replace the mock value with the debtor -> stateProvinceRegion field
        			.stateProvinceRegion(null)
        			// TODO when available replace the mock value with the debtor -> country field
        			.country(null)
        			// TODO when available replace the mock value with the debtor -> eMail field
        			.eMail(null)
        			.build();
        }

        if (null != be.getPayer()) {
        	ctReceiptPayer = Payer.builder()
        			.entityUniqueIdentifierType(EntityUniqueIdentifierType.valueOf(be.getPayer().getEntityUniqueIdentifierType()))
        			.entityUniqueIdentifierValue(be.getPayer().getEntityUniqueIdentifierValue())
        			.fullName(be.getPayer().getFullName())
        			// TODO when available replace the mock value with the payer  -> streetName field
        			.streetName(null)
        			// TODO when available replace the mock value with the payer -> civicNumber field
        			.civicNumber(null)
        			// TODO when available replace the mock value with the payer -> postalCode field
        			.postalCode(null)
        			// TODO when available replace the mock value with the payer -> city field
        			.city(null)
        			// TODO when available replace the mock value with the payer -> stateProvinceRegion field
        			.stateProvinceRegion(null)
        			// TODO when available replace the mock value with the payer -> country field
        			.country(null)
        			// TODO when available replace the mock value with the payer -> eMail field
        			.eMail(null)
        			.build();
        }

        if (null != be.getTransferList() && !be.getTransferList().isEmpty()) {
        	ctTransferListPA = new ArrayList<>();
        	for (Transfer t : be.getTransferList()) {
        		/**
        		 * TODO *** confirm that TransferPA must contain also MBDAttachment and metadata fields ***
        		 */
        		ctTransferListPA.add(TransferPA.builder()
        				// TODO when available replace the 0 value with the transfer  -> idTransfer field
        				.idTransfer(0)
        				.transferAmount(BigDecimal.valueOf(Double.valueOf(t.getAmount())))
        				.fiscalCodePA(t.getFiscalCodePA())
        				// TODO when available replace the mock value with the transfer -> iban field
        				.iban("mock iban")
        				.remittanceInformation(t.getRemittanceInformation())
        				.transferCategory(t.getTransferCategory())
        				.build());
        	}
        }
        
        
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
        		.debtor(ctReceiptDebtor)
        		.transferList(ctTransferListPA)
        		.idPSP(be.getPsp().getIdPsp())
        		// TODO when available replace the mock value with the psp -> pspFiscalCode field
        		.pspFiscalCode("mock pspFiscalCode")
        		// TODO when available replace the mock value with the psp -> pspPartitaIVA field
                .pspPartitaIVA("mock pspPartitaIVA")
                .pspCompanyName(be.getPsp().getPsp())
                .idChannel(be.getPsp().getIdChannel())
                // TODO when available replace the mock value with the psp -> channelDescription (?) field
                .channelDescription("mock channelDescription")
                .payer(ctReceiptPayer)
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
