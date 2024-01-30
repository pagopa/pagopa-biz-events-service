package it.gov.pagopa.bizeventsservice.entity.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entity model for biz-events-view-cart
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class BizEventsViewCart {
    private String transactionId;
    private String eventId;
    private long amount;
    private UserDetail payee;
    private UserDetail debtor;
    private String refNumberValue;
    private String refNumberType;
}
