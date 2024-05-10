package it.gov.pagopa.bizeventsservice.model.response;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * Model class for the attachment details response
 */
@Getter
@Builder
@Jacksonized
public class AttachmentsDetailsResponse {

    private List<Attachment> attachments;
}
