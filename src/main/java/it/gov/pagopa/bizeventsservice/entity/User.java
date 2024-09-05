package it.gov.pagopa.bizeventsservice.entity;

import it.gov.pagopa.bizeventsservice.model.response.enumeration.UserType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private String fullName;
    private UserType type;
    private String fiscalCode;
    private String notificationEmail;
    private String userId;
    private String userStatus;
    private String userStatusDescription;
}
