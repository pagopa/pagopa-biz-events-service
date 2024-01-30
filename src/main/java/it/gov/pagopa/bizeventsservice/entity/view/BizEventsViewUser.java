package it.gov.pagopa.bizeventsservice.entity.view;

import lombok.*;

/**
 * Entity model for biz-events-view-user
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class BizEventsViewUser {
    private String fiscalCode;
    private String transactionId;
    private String transactionDate;
    private boolean hidden;
}
