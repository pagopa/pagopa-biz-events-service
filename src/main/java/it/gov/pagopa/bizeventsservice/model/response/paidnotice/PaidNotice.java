package it.gov.pagopa.bizeventsservice.model.response.paidnotice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Response model for transaction list API
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(Include.NON_NULL)
public class PaidNotice implements Serializable {
	private String eventId;
    private String payeeName;
    private String payeeTaxCode;
    private String amount;
    private String noticeDate;
    private Boolean isCart;
    private Boolean isPayer;
    @Builder.Default
    private Boolean isDebtor = false;
}
