package it.gov.pagopa.bizeventsservice.config.openapi;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VisibleFor {

    OpenApiScope[] value();
}