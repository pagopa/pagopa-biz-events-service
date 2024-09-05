package it.gov.pagopa.bizeventsservice.entity.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;

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
	
	private String name;
	@Schema(required = true)
    @NotNull
    private String taxCode;
}
