package it.gov.pagopa.bizeventsservice.util;

import com.azure.spring.data.cosmos.common.ExpressionResolver;
import it.gov.pagopa.bizeventsservice.repository.BizEventsRepository;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MockConfiguration {
    @Bean
    @Primary
    ExpressionResolver expressionResolver() {
        return Mockito.mock(ExpressionResolver.class);
    }

    @Bean
    @Primary
    BizEventsRepository bizEventsRepository() {
        return Mockito.mock(BizEventsRepository.class);
    }
}
