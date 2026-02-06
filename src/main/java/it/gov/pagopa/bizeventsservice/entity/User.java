package it.gov.pagopa.bizeventsservice.entity;

import it.gov.pagopa.bizeventsservice.model.response.enumeration.UserType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @ToString.Exclude
    private String fullName;
    private UserType type;
    @ToString.Exclude
    private String fiscalCode;
    private String notificationEmail;
    private String userId;
    private String userStatus;
    private String userStatusDescription;
    @ToString.Exclude
    private String name;
    @ToString.Exclude
    private String surname;
}
