package it.gov.pagopa.bizeventsservice.model.response.paidnotice;

import it.gov.pagopa.bizeventsservice.entity.view.UserDetail;
import it.gov.pagopa.bizeventsservice.entity.view.WalletInfo;
import it.gov.pagopa.bizeventsservice.entity.view.enumeration.OriginType;
import it.gov.pagopa.bizeventsservice.entity.view.enumeration.PaymentMethodType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Response model for transaction detail API
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class InfoNotice implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5306955320137743890L;
	private String eventId;
    private String authCode;
    private String rrn;
    private String noticeDate;
    private String pspName;
    private WalletInfo walletInfo;
    private PaymentMethodType paymentMethod;
    private UserDetail payer;
    private String amount;
    private String fee;
    private OriginType origin;
}
