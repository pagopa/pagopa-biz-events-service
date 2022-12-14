package it.gov.pagopa.bizeventsservice.model.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import it.gov.pagopa.bizeventsservice.model.MapEntry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CtReceiptModelResponse implements Serializable {
	/**
	 * generated serialVersionUID
	 */
	private static final long serialVersionUID = -242386899661512219L;
	
	@NotBlank(message = "receiptId is required")
    private String receiptId;
	@NotBlank(message = "noticeNumber is required")
    private String noticeNumber;
	@NotBlank(message = "fiscalCode is required")
    private String fiscalCode;
    @Builder.Default
    @NotBlank(message = "outcome is required")
    private String outcome = "OK";
    @NotBlank(message = "creditorReferenceId is required")
    private String creditorReferenceId;
    @NotNull(message = "paymentAmount is required")
    private BigDecimal paymentAmount;
    @NotBlank(message = "description is required")
    private String description;
    
    @NotBlank(message = "companyName is required")
    private String companyName;
    private String officeName;
    
    @NotNull(message = "debtor is required")
    private Debtor debtor;
    @NotNull(message = "transferList is required")
    private List<TransferPA> transferList;
    @NotBlank(message = "idPSP is required")
    private String idPSP;
    private String pspFiscalCode;
    private String pspPartitaIVA;
    @NotBlank(message = "pspCompanyName is required")
    private String pspCompanyName;
    @NotBlank(message = "idChannel is required")
    private String idChannel;  
    @NotBlank(message = "channelDescription is required")
    private String channelDescription;
    
    private Payer payer;
    private String paymentMethod;
    
    private BigDecimal fee;
    private BigDecimal primaryCiIncurredFee;
    private String idBundle;
    private String idCiBundle;
    private LocalDate  paymentDateTime;
    private LocalDate  applicationDate;
    private LocalDate  transferDate;
    private List<MapEntry> metadata;

}
