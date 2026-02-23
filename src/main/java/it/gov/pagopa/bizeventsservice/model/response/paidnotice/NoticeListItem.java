package it.gov.pagopa.bizeventsservice.model.response.paidnotice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Response model for transaction list API
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(Include.NON_NULL)
public class NoticeListItem implements Serializable {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String eventId;

    private String payeeName;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String payeeTaxCode;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String amount;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private String noticeDate;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Boolean isCart;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Boolean isPayer;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    @Builder.Default
    private Boolean isDebtor = false;
}
