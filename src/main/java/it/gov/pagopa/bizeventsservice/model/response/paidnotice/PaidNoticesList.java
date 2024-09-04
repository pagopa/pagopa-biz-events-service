package it.gov.pagopa.bizeventsservice.model.response.paidnotice;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaidNoticesList {
    private List<PaidNotice> paidNoticeList;
}
