package it.gov.pagopa.bizeventsservice.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Debtor {
    @ToString.Exclude
    private String fullName;
    private String entityUniqueIdentifierType;
    @ToString.Exclude
    private String entityUniqueIdentifierValue;
    private String streetName;
    private String civicNumber;
    private String postalCode;
    private String city;
    private String stateProvinceRegion;
    private String country;
    @JsonProperty(value = "email")
    @ToString.Exclude
    private String eMail;
}
