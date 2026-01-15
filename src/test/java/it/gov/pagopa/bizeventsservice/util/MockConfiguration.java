package it.gov.pagopa.bizeventsservice.util;

import com.azure.spring.data.cosmos.common.ExpressionResolver;
import it.gov.pagopa.bizeventsservice.repository.primary.BizEventsPrimaryRepository;
import it.gov.pagopa.bizeventsservice.repository.replica.BizEventsRepository;
import it.gov.pagopa.bizeventsservice.repository.primary.BizEventsViewCartRepository;
import it.gov.pagopa.bizeventsservice.repository.primary.BizEventsViewGeneralRepository;
import it.gov.pagopa.bizeventsservice.repository.primary.BizEventsViewUserRepository;
import org.mockito.Mockito;
import org.springframework.cache.CacheManager;
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
    BizEventsPrimaryRepository bizEventsPrimaryRepository() {
        return Mockito.mock(BizEventsPrimaryRepository.class);
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
    @Bean
    @Primary
    CacheManager cacheManager() {
        return Mockito.mock(CacheManager.class);
    }
}
