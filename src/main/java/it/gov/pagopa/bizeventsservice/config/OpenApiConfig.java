package it.gov.pagopa.bizeventsservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static it.gov.pagopa.bizeventsservice.util.Constants.HEADER_REQUEST_ID;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI customOpenAPI(@Value("${info.application.name}") String appTitle,
                          @Value("${info.application.description}") String appDescription,
                          @Value("${info.application.version}") String appVersion) {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("ApiKey", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .description("The API key to access this function app.")
                                .name("Ocp-Apim-Subscription-Key")
                                .in(SecurityScheme.In.HEADER))
                )
                .info(new Info()
                        .title(appTitle)
                        .version(appVersion)
                        .description(appDescription)
                        .termsOfService("https://www.pagopa.gov.it/"));

    }

    @Bean
    OpenApiCustomiser sortOperationsAlphabetically() {
        return openApi -> {
            Paths paths = openApi.getPaths().entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Paths::new, (map, item) -> map.addPathItem(item.getKey(), item.getValue()), Paths::putAll);

            paths.forEach((key, value) -> value.readOperations().forEach(operation -> {
                var responses = operation.getResponses().entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .collect(ApiResponses::new, (map, item) -> map.addApiResponse(item.getKey(), item.getValue()), ApiResponses::putAll);
                operation.setResponses(responses);
            }));
            openApi.setPaths(paths);
        };
    }

    @Bean
    OpenApiCustomiser addCommonHeaders() {
        return openApi -> openApi.getPaths().forEach((key, value) -> {

            // add Request-ID as request header        	
            var header = Optional.ofNullable(value.getParameters())
                    .orElse(Collections.emptyList())
                    .parallelStream()
                    .filter(Objects::nonNull)
                    .anyMatch(elem -> HEADER_REQUEST_ID.equals(elem.getName()));
            if (!header) {
                value.addParametersItem(new Parameter().in("header")
                        .name(HEADER_REQUEST_ID)
                        .schema(new StringSchema())
                        .description("This header identifies the call, if not passed it is self-generated. This ID is returned in the response.")
                        .required(false));
            }

            // add Request-ID as response header
            value.readOperations()
                    .forEach(operation -> operation.getResponses().values()
                            .forEach(response -> response.addHeaderObject(HEADER_REQUEST_ID, new Header()
                                    .schema(new StringSchema())
                                    .description("This header identifies the call"))));
        });
    }

    @Bean
    public Map<String, GroupedOpenApi> configureGroupOpenApi(Map<String, GroupedOpenApi> groupOpenApi) {
        groupOpenApi.forEach((id, groupedOpenApi) -> groupedOpenApi
                .getOpenApiCustomisers()
                .add(addCommonHeaders()));
        return groupOpenApi;
    }
}
