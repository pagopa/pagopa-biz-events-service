package it.gov.pagopa.bizeventsservice.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Details {
    private String blurredNumber;
    private String holder;
    private String circuit;
}
