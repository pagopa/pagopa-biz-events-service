package it.gov.pagopa.bizeventsservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MapEntry implements Serializable {

    /**
     * generated serialVersionUID
     */
    private static final long serialVersionUID = 7768010843596229040L;

    private String key;
    private String value;
}
