package it.gov.pagopa.bizeventsservice.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageInfo {

    @JsonProperty("page")
    @Schema(description = "Page number", requiredMode = Schema.RequiredMode.REQUIRED)
    @PositiveOrZero
    Integer page;

    @JsonProperty("limit")
    @Schema(description = "Required number of items per page", requiredMode = Schema.RequiredMode.REQUIRED)
    @Positive
    Integer limit;

    @JsonProperty("items_found")
    @Schema(description = "Number of items found. (The last page may have fewer elements than required)", requiredMode = Schema.RequiredMode.REQUIRED)
    @PositiveOrZero
    Integer itemsFound;

    @JsonProperty("total_pages")
    @Schema(description = "Total number of pages", requiredMode = Schema.RequiredMode.REQUIRED)
    @PositiveOrZero
    Integer totalPages;
}
