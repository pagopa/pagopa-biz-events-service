package it.gov.pagopa.bizeventsservice.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.gov.pagopa.bizeventsservice.model.response.enumeration.EntityUniqueIdentifierType;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payer implements Serializable {
    /**
     * generated serialVersionUID
     */
    private static final long serialVersionUID = -1069568268303542892L;

    @NotNull(message = "entityUniqueIdentifierType is required")
    private EntityUniqueIdentifierType entityUniqueIdentifierType;
    @NotBlank(message = "entityUniqueIdentifierValue is required")
    @ToString.Exclude
    private String entityUniqueIdentifierValue;
    @NotBlank(message = "fullName is required")
    @ToString.Exclude
    private String fullName;
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
