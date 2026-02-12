package it.gov.pagopa.bizeventsservice.config.openapi;

import java.lang.annotation.*;

/**
 * Annotation to toggle params and methods visibility on the generated OpenApi by declaring a list of scopes {@link OpenApiScope}
 * <p>
 * The visibility is then handled by the customizer {@link VisibleOnlyForOperationCustomizer}
 * added to the operation customizers on the openapi configuration {@link OpenApiConfig}
 **/
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VisibleOnlyFor {

    OpenApiScope[] value();
}