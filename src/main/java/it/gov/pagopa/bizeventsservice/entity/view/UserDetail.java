package it.gov.pagopa.bizeventsservice.entity.view;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class UserDetail implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -5682201542579999764L;

    @ToString.Exclude
    private String name;
    @Schema(required = true)
    @NotNull
    @ToString.Exclude
    private String taxCode;
}
