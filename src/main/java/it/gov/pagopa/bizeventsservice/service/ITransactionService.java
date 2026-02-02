package it.gov.pagopa.bizeventsservice.service;

import it.gov.pagopa.bizeventsservice.model.filterandorder.Order.TransactionListOrder;
import it.gov.pagopa.bizeventsservice.model.response.paidnotice.NoticeDetailResponse;
import it.gov.pagopa.bizeventsservice.model.response.transaction.TransactionListResponse;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;

import javax.validation.constraints.NotBlank;

public interface ITransactionService {

    /**
     * Retrieves paged transaction list given a valid fiscal code, offset and page size
     *
     * @param fiscalCode        fiscal code to filter transaction list
     * @param continuationToken continuation token for paginated query
     * @param size              offset size
     * @return transaction list
     */
    TransactionListResponse getTransactionList(
            String fiscalCode,
            Boolean isPayer,
            Boolean isDebtor,
            String continuationToken,
            Integer size,
            TransactionListOrder orderBy,
            Direction ordering
    );

    NoticeDetailResponse getPaidNoticeDetail(String fiscalCode, String eventId);

    ResponseEntity<Resource> getPDFReceiptResponse(String fiscalCode, @NotBlank String eventId);

    /**
     * This method disable a paid notice for the given fiscal code and event id
     *
     * @param fiscalCode the fiscal code
     * @param eventId    the event id
     */
    void disablePaidNotice(String fiscalCode, String eventId);
}
