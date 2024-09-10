package it.gov.pagopa.bizeventsservice.entity.view;

import java.io.Serializable;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.GeneratedValue;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity model for biz-events-view-user
 */
@Container(containerName = "${azure.cosmos.biz-events-view-user-container-name}", autoCreateContainer = false, ru = "1000")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BizEventsViewUser implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -4997399615775767480L;

    @GeneratedValue
    private String id;
    @PartitionKey
    private String taxCode;
    private String transactionId;
    private String transactionDate;
    private Boolean hidden;
    private Boolean isPayer;
    private Boolean isDebtor;
}
