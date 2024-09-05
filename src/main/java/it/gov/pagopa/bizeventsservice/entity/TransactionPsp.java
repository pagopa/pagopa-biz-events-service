package it.gov.pagopa.bizeventsservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionPsp {
    private String idChannel;
    private String businessName;
    private String serviceName;
}
