package it.gov.pagopa.bizeventsservice.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Info {
    private String type;
    private String blurredNumber;
    @ToString.Exclude
    private String holder;
    private String expireMonth;
    private String expireYear;
    private String brand;
    private String issuerAbi;
    private String issuerName;
    private String label;
}
