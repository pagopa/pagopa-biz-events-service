package it.gov.pagopa.bizeventsservice.entity;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.gov.pagopa.bizeventsservice.model.response.enumeration.StatusType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Container(containerName = "${azure.cosmos.biz-events-container-name}", autoCreateContainer = false, ru = "1000")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class BizEvent {
    private String id;
    private String version;
    private String idPaymentManager;
    private String complete;
    private String receiptId;
    private List<String> missingInfo;
    private DebtorPosition debtorPosition;
    private Creditor creditor;
    private Psp psp;
    private Debtor debtor;
    private Payer payer;
    private PaymentInfo paymentInfo;
    private List<Transfer> transferList;
    private TransactionDetails transactionDetails;

    // internal management fields
    private StatusType eventStatus;
    private Integer eventRetryEnrichmentCount;

    @JsonProperty("_ts")
    @JsonFormat(shape = JsonFormat.Shape.STRING,
    pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
    timezone = "UTC")
    private OffsetDateTime ts;
}
