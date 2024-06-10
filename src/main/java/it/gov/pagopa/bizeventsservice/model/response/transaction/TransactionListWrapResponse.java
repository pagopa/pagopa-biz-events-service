package it.gov.pagopa.bizeventsservice.model.response.transaction;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import it.gov.pagopa.bizeventsservice.model.PageInfo;

@Builder
@Getter
@JsonInclude(Include.NON_NULL)
public class TransactionListWrapResponse {
    private List<TransactionListItem> transactions;
    @JsonProperty("page_info")
    private PageInfo pageInfo;
}
