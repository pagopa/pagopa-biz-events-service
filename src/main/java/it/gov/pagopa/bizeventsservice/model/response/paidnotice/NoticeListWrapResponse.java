package it.gov.pagopa.bizeventsservice.model.response.paidnotice;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.util.List;

@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NoticeListWrapResponse {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private List<NoticeListItem> notices;
}
