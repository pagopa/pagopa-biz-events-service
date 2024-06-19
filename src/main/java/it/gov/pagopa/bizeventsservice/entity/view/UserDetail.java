package it.gov.pagopa.bizeventsservice.entity.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

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
    private String taxCode;
}
