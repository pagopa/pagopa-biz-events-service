package it.gov.pagopa.bizeventsservice.model.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetail {

    private String name;
    private String taxCode;
}
