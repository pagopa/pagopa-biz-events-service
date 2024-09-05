package it.gov.pagopa.bizeventsservice.model.response;

import it.gov.pagopa.bizeventsservice.model.MapEntry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferPA implements Serializable {
    /**
     * generated serialVersionUID
     */
    private static final long serialVersionUID = 3529035729352848592L;

    @Min(1)
    @Max(5)
    private int idTransfer;
    @NotNull(message = "transferAmount is required")
    private BigDecimal transferAmount;
    @NotBlank(message = "fiscalCodePA is required")
    private String fiscalCodePA;
    @NotBlank(message = "iban is required")
    private String iban;
    @NotBlank(message = "mbdAttachment is required")
    private String mbdAttachment;
    @NotBlank(message = "remittanceInformation is required")
    private String remittanceInformation;
    @NotBlank(message = "transferCategory is required")
    private String transferCategory;
    private List<MapEntry> metadata;
}
