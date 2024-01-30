package it.gov.pagopa.bizeventsservice.entity.view;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import lombok.*;

/**
 * Entity model for biz-events-view-cart
 */
@Container(containerName = "${azure.cosmos.biz-events-view-cart-container-name}", autoCreateContainer = false, ru="1000")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class BizEventsViewCart {
    private String id;
    @PartitionKey
    private String transactionId;
    private String eventId;
    private String subject;
    private long amount;
    private UserDetail payee;
    private UserDetail debtor;
    private String refNumberValue;
    private String refNumberType;
}
