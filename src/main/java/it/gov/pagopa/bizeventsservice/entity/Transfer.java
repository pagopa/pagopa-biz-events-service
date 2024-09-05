package it.gov.pagopa.bizeventsservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.gov.pagopa.bizeventsservice.model.MapEntry;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transfer {
    @Builder.Default
    private String idTransfer = "0";
    private String fiscalCodePA;
    private String companyName;
    private String amount;
    private String transferCategory;
    private String remittanceInformation;
    @JsonProperty(value = "IBAN")
    private String iban;
    @JsonProperty(value = "MBD")
    private MBD mbd;
    private List<MapEntry> metadata;
}
