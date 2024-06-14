package it.gov.pagopa.bizeventsservice.model.response;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

/**
 * Model class for the attachment details response
 */
@Getter
@SuperBuilder
@Jacksonized
public class AttachmentsDetailsResponse {

    private List<Attachment> attachments;
}
