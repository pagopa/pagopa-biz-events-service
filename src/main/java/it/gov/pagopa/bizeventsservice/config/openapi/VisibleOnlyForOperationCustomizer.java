package it.gov.pagopa.bizeventsservice.config.openapi;

import io.swagger.v3.oas.models.Operation;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class VisibleOnlyForOperationCustomizer implements OperationCustomizer {

    private final OpenApiScope scope;

    public VisibleOnlyForOperationCustomizer(OpenApiScope scope) {
        this.scope = scope;
    }

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        // Remove from Openapi: APIs not in scope
        VisibleOnlyFor visibleOnlyForMethod = handlerMethod.getMethodAnnotation(VisibleOnlyFor.class);
        if (visibleOnlyForMethod != null && Arrays.stream(visibleOnlyForMethod.value()).noneMatch(scope::equals)) {
            return null;
        }

        // Remove from Openapi: APIs' params not in scope
        Set<String> paramsToRemove = new HashSet<>();

        for (MethodParameter parameter : handlerMethod.getMethodParameters()) {
            VisibleOnlyFor visibleOnlyForParam = parameter.getParameterAnnotation(VisibleOnlyFor.class);
            if (visibleOnlyForParam == null || Arrays.asList(visibleOnlyForParam.value()).contains(scope)) {
                continue;
            }

            RequestParam requestParam =
                    parameter.getParameterAnnotation(RequestParam.class);

            String paramName = (requestParam != null && !requestParam.name().isEmpty())
                    ? requestParam.name()
                    : parameter.getParameterName();

            paramsToRemove.add(paramName);
        }

        if (operation.getParameters() != null && !paramsToRemove.isEmpty()) {
            operation.getParameters()
                    .removeIf(p -> paramsToRemove.contains(p.getName()));
        }

        return operation;
    }
}


