package it.gov.pagopa.bizeventsservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * This configuration is required ignore the 'accept' header in the request.
 * If the client uses a different value of the response then SpringBoot goes wrong because the response type doesn't match.
 * We can ignore the accept header from the client.
 * <p>
 * see more information in this GitHub <a href="https://github.com/swagger-api/swagger-ui/issues/5649">issue</a>
 */
// TODO should I remove this configuration?
@Configuration
public class ContentNegotiationConf implements WebMvcConfigurer {

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
                .ignoreAcceptHeader(true)
                .defaultContentType(MediaType.ALL);
    }
}