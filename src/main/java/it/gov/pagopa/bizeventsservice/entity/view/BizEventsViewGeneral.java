package it.gov.pagopa.bizeventsservice.entity.view;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import it.gov.pagopa.bizeventsservice.entity.view.enumeration.OriginType;
import it.gov.pagopa.bizeventsservice.entity.view.enumeration.PaymentMethodType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

/**
 * Entity model for biz-events-view-general
 */
@Container(containerName = "${azure.cosmos.biz-events-view-general-container-name}", autoCreateContainer = false, ru="1000")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class BizEventsViewGeneral {
    @Id
    private String id;
    @PartitionKey
    private String transactionId;
    private String authCode;
    private PaymentMethodType paymentMethod;
    private String rrn;
    private String pspName;
    private String transactionDate;
    private WalletInfo walletInfo;
    private UserDetail payer;
    private boolean isCart;
    private String fee;
    private OriginType origin;
    private int totalNotice;

}
