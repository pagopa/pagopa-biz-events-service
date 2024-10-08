package it.gov.pagopa.bizeventsservice.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DebtorPosition {
    private String modelType;
    private String noticeNumber;
    private String iuv;
    private String iur;
}
