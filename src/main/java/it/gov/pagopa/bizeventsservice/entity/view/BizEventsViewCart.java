package it.gov.pagopa.bizeventsservice.entity.view;

import com.azure.spring.data.cosmos.core.mapping.Container;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

/**
 * Entity model for biz-events-view-cart
 */
@Container(containerName = "${azure.cosmos.biz-events-view-cart-container-name}", autoCreateContainer = false, ru="1000")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class BizEventsViewCart {
    @Id
    private String transactionId;
    private String eventId;
    private String subject;
    private long amount;
    private UserDetail payee;
    private UserDetail debtor;
    private String refNumberValue;
    private String refNumberType;
}
