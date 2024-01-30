package it.gov.pagopa.bizeventsservice.entity.view;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.GeneratedValue;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import lombok.*;

/**
 * Entity model for biz-events-view-user
 */
@Container(containerName = "${azure.cosmos.biz-events-view-user-container-name}", autoCreateContainer = false, ru="1000")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class BizEventsViewUser {
    @GeneratedValue
    private String id;
    @PartitionKey
    private String taxCode;
    private String transactionId;
    private String transactionDate;
    private boolean hidden;
}
