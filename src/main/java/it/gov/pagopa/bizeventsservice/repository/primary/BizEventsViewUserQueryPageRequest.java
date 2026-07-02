package it.gov.pagopa.bizeventsservice.repository.primary;

import it.gov.pagopa.bizeventsservice.model.filterandorder.Order.TransactionListOrder;
import org.springframework.data.domain.Sort.Direction;

public record BizEventsViewUserQueryPageRequest(
        String continuationToken,
        Integer size,
        TransactionListOrder orderBy,
        Direction ordering
) {
}