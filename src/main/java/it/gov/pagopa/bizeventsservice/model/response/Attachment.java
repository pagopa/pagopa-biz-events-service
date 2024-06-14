package it.gov.pagopa.bizeventsservice.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@SuperBuilder
@Jacksonized
public class Attachment {

    private String id;
    @JsonProperty("content_type")
    private String contentType;
    private String name;
    private String url;
}
