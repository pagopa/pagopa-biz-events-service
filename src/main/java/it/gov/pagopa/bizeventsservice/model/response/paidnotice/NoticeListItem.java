package it.gov.pagopa.bizeventsservice.model.response.paidnotice;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
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

    @Schema(required = true)
    @NotNull
    private String eventId;

    private String payeeName;

    @Schema(required = true)
    @NotNull
    private String payeeTaxCode;

    @Schema(required = true)
    @NotNull
    private String amount;

    @Schema(required = true)
    @NotNull
    private String noticeDate;

    @Schema(required = true)
    @NotNull
    private Boolean isCart;

    @Schema(required = true)
    @NotNull
    private Boolean isPayer;

    @Schema(required = true)
    @NotNull
    @Builder.Default
    private Boolean isDebtor = false;
}
