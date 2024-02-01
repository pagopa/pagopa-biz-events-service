package it.gov.pagopa.bizeventsservice.util;

import com.azure.spring.data.cosmos.common.ExpressionResolver;
import it.gov.pagopa.bizeventsservice.repository.BizEventsRepository;
import it.gov.pagopa.bizeventsservice.repository.BizEventsViewCartRepository;
import it.gov.pagopa.bizeventsservice.repository.BizEventsViewGeneralRepository;
import it.gov.pagopa.bizeventsservice.repository.BizEventsViewUserRepository;
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
    @Bean
    @Primary
    BizEventsViewUserRepository bizEventsViewUserRepository() {
        return Mockito.mock(BizEventsViewUserRepository.class);
    }
    @Bean
    @Primary
    BizEventsViewCartRepository bizEventsViewCartRepository() {
        return Mockito.mock(BizEventsViewCartRepository.class);
    }
    @Bean
    @Primary
    BizEventsViewGeneralRepository bizEventsViewGeneralRepository() {
        return Mockito.mock(BizEventsViewGeneralRepository.class);
    }
}
