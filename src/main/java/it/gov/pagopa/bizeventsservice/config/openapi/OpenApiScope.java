package it.gov.pagopa.bizeventsservice.config.openapi;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OpenApiScope {
    PUBLIC("public"),
    HELPDESK("helpdesk"),
    EC("ec"),
    LAP("lap"),
    LAP_JWT("lap_jwt");

    private final String name;
}
