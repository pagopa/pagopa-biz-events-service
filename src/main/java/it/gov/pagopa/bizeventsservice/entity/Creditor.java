package it.gov.pagopa.bizeventsservice.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Creditor {
    private String idPA;
    private String idBrokerPA;
    private String idStation;
    private String companyName;
    private String officeName;
}
