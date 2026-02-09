package it.gov.pagopa.bizeventsservice.config.openapi;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.core.MethodParameter;
import io.swagger.v3.oas.models.Operation;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.method.HandlerMethod;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class VisibleForOperationCustomizer implements OperationCustomizer {

    private final OpenApiScope scope;

    public VisibleForOperationCustomizer(OpenApiScope scope) {
        this.scope = scope;
    }

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {

        Set<String> paramsToRemove = new HashSet<>();

        for (MethodParameter parameter : handlerMethod.getMethodParameters()) {

            VisibleFor visibleFor = parameter.getParameterAnnotation(VisibleFor.class);
            if (visibleFor == null || Arrays.asList(visibleFor.value()).contains(scope)) {
                continue;
            }

            RequestParam requestParam = parameter.getParameterAnnotation(RequestParam.class);
            String paramName = (requestParam != null && !requestParam.name().isEmpty())
                    ? requestParam.name()
                    : parameter.getParameterName();

            paramsToRemove.add(paramName);
        }

        if (operation.getParameters() != null) {
            operation.getParameters().removeIf(p -> paramsToRemove.contains(p.getName()));
        }

        return operation;
    }
}

