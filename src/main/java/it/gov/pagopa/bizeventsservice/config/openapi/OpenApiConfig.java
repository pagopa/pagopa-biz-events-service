package it.gov.pagopa.bizeventsservice.config.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariable;
import io.swagger.v3.oas.models.servers.ServerVariables;
import it.gov.pagopa.bizeventsservice.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

import static it.gov.pagopa.bizeventsservice.util.Constants.HEADER_REQUEST_ID;
import static it.gov.pagopa.bizeventsservice.util.Constants.X_FISCAL_CODE;

@Slf4j
@Configuration
public class OpenApiConfig {

    public static final String BASE_PATH_HELPDESK = "/bizevents/helpdesk/v1";
    public static final String BASE_PATH_EC = "/bizevents/service/v1";
    public static final String BASE_PATH_LAP = "/bizevents/notices-service/v1";
    public static final String BASE_PATH_LAP_JWT = "/bizevents/notices-service-jwt/v1";
    public static final String LOCAL_PATH = "http://localhost:8080";
    public static final String APIM_DEV = "https://api.dev.platform.pagopa.it";
    public static final String APIM_UAT = "https://api.uat.platform.pagopa.it";
    public static final String APIM_PROD = "https://api.platform.pagopa.it";
    private static final String API_KEY_SECURITY_SCHEMA_KEY = "ApiKey";
    private static final String JWT_SECURITY_SCHEMA_KEY = "Authorization";

    @Bean
    OpenAPI customOpenAPI(
            @Value("${info.application.name}") String appName,
            @Value("${info.application.description}") String appDescription,
            @Value("${info.application.version}") String appVersion
    ) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(appDescription).append("""
                                
                ### APP ERROR CODES ###
                              
                                
                <details><summary>Details</summary>
                                
                | Code | Group | Domain | Description |
                | ---- | ----- | ------ | ----------- |
                """);
        for (ErrorCode errorCode : ErrorCode.values()) {
            stringBuilder
                    .append("| **")
                    .append(errorCode.getCode())
                    .append("** | *")
                    .append(errorCode.getGroup())
                    .append("* | ")
                    .append(errorCode.getDomain())
                    .append(" | ")
                    .append(errorCode.getDescription())
                    .append(" |\n");
        }
        stringBuilder.append("</details>");
        return new OpenAPI()
                .servers(List.of(new Server().url(LOCAL_PATH),
                        new Server().url("{host}{basePath}")
                                .variables(new ServerVariables()
                                        .addServerVariable("host",
                                                new ServerVariable()._enum(List.of(APIM_DEV, APIM_UAT, APIM_PROD))
                                                        ._default(APIM_PROD))
                                        .addServerVariable("basePath", new ServerVariable()._enum(List.of(BASE_PATH_LAP, BASE_PATH_EC, BASE_PATH_HELPDESK))
                                                ._default(BASE_PATH_EC))
                                )))
                .components(new Components().addSecuritySchemes(API_KEY_SECURITY_SCHEMA_KEY,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .description("The Azure Subscription Key to access this API.")
                                .name("Ocp-Apim-Subscription-Key")
                                .in(SecurityScheme.In.HEADER))
                )
                .info(new Info()
                        .title(appName)
                        .version(appVersion)
                        .description(stringBuilder.toString())
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
    GlobalOpenApiCustomizer addCommonHeaders() {
        return openApi -> openApi.getPaths().forEach((key, value) -> {

            // add Request-ID as request header        	
            var header = Optional.ofNullable(value.getParameters())
                    .orElse(Collections.emptyList())
                    .parallelStream()
                    .filter(Objects::nonNull)
                    .anyMatch(elem -> HEADER_REQUEST_ID.equals(elem.getName()));
            if (!header) {
                // add Request-ID as request header
                value.readOperations().forEach(operation -> operation.addParametersItem(new Parameter().in("header")
                        .name(HEADER_REQUEST_ID)
                        .schema(new StringSchema())
                        .description("This header identifies the call, if not passed it is self-generated. This ID is returned in the response.")
                        .required(false)));
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
        groupOpenApi.forEach((id, groupedOpenApi) -> {
            switch (id) {
                case "helpdesk" -> groupedOpenApi.getOperationCustomizers()
                        .add(new VisibleForOperationCustomizer(OpenApiScope.HELP_DESK));

                case "ec" -> groupedOpenApi.getOperationCustomizers()
                        .add(new VisibleForOperationCustomizer(OpenApiScope.EC));

                case "lap" -> groupedOpenApi.getOperationCustomizers()
                        .add(new VisibleForOperationCustomizer(OpenApiScope.LAP));

                case "lap_jwt" -> groupedOpenApi.getOperationCustomizers()
                        .add(new VisibleForOperationCustomizer(OpenApiScope.LAP_JWT));

                default -> groupedOpenApi.getOperationCustomizers()
                        .add(new VisibleForOperationCustomizer(OpenApiScope.PUBLIC));
            }

            groupedOpenApi.getOpenApiCustomisers()
                    .add(openApi -> {
                        var baseTitle = openApi.getInfo().getTitle();
                        var group = groupedOpenApi.getDisplayName();
                        openApi.getInfo().setTitle(baseTitle + " - " + group);
                        switch (id) {
                            case "helpdesk" -> {
                                openApi.getInfo().setDescription("Microservice for exposing REST APIs for bizevent Helpdesk.");
                                openApi.setServers(List.of(new Server().url(LOCAL_PATH), new Server().url(APIM_PROD + BASE_PATH_HELPDESK)));
                            }
                            case "ec" -> {
                                openApi.setServers(List.of(new Server().url(LOCAL_PATH), new Server().url(APIM_PROD + BASE_PATH_EC)));
                            }
                            case "lap" -> {
                                openApi.setServers(List.of(new Server().url(LOCAL_PATH), new Server().url(APIM_PROD + BASE_PATH_LAP)));
                            }
                            case "lap_jwt" -> {
                                openApi.setServers(List.of(new Server().url(LOCAL_PATH), new Server().url(APIM_PROD + BASE_PATH_LAP_JWT)));
                                customizeForIOAuth(openApi);
                            }
                            default -> {
                            }
                        }
                    });
        });
        return groupOpenApi;
    }

    private void customizeForIOAuth(OpenAPI openApi) {
        openApi.getComponents().setSecuritySchemes(Collections.singletonMap(
                JWT_SECURITY_SCHEMA_KEY,
                new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .description("JWT token associated to the user")
                        .scheme("bearer")));

        openApi.getPaths().forEach((key, value) -> value.readOperations().forEach(
                operation -> {
                    // remove x-fiscal-code header if present
                    Optional.ofNullable(operation.getParameters())
                            .orElse(Collections.emptyList())
                            .removeIf(elem -> X_FISCAL_CODE.equals(elem.getName()));

                    // set JWT as security requirement
                    operation.setSecurity(List.of(new SecurityRequirement().addList(JWT_SECURITY_SCHEMA_KEY)));
                }
        ));
    }
}
