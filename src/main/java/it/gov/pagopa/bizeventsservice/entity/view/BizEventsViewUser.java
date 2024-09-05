package it.gov.pagopa.bizeventsservice.entity.view;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.GeneratedValue;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    public LocalDateTime getTransactionDateAsLocalDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("[yyyy-MM-dd'T'HH:mm:ss'Z'][yyyy-MM-dd'T'HH:mm:ss.SSSSSS]");
        return LocalDateTime.parse(this.getTransactionDate(), formatter);
    }
}
