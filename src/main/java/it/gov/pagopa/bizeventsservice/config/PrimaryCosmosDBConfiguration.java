package it.gov.pagopa.bizeventsservice.config;


import com.azure.core.credential.AzureKeyCredential;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.ResponseDiagnostics;
import com.azure.spring.data.cosmos.core.ResponseDiagnosticsProcessor;
import com.azure.spring.data.cosmos.core.mapping.EnableCosmosAuditing;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

@Configuration
@EnableCosmosRepositories("it.gov.pagopa.bizeventsservice.repository.primary")
@EnableCosmosAuditing
@ConditionalOnExpression("'${info.properties.environment}'!='test'")
@Slf4j
public class PrimaryCosmosDBConfiguration extends AbstractCosmosConfiguration {

    @Value("${azure.cosmos.uri}")
    private String uri;

    @Value("${azure.cosmos.key}")
    private String key;

    @Value("${azure.cosmos.database}")
    private String dbName;

    @Value("${azure.cosmos.populate-query-metrics}")
    private boolean queryMetricsEnabled;

    @Bean
    CosmosClientBuilder getCosmosClientBuilder() {
        AzureKeyCredential azureKeyCredential = new AzureKeyCredential(key);
        DirectConnectionConfig directConnectionConfig = new DirectConnectionConfig();
        return new CosmosClientBuilder()
                .endpoint(uri)
                .credential(azureKeyCredential)
                .directMode(directConnectionConfig);
    }

    @Bean("primaryCosmosFactory")
    public CosmosFactory primaryCosmosFactory(CosmosFactory cosmosFactory) {
        return cosmosFactory;
    }

    @Bean("primaryCosmosTemplate")
    public CosmosTemplate primaryCosmosTemplate(CosmosTemplate cosmosTemplate) {
        return cosmosTemplate;
    }

    @Override
    public CosmosConfig cosmosConfig() {
        return CosmosConfig.builder()
                .enableQueryMetrics(queryMetricsEnabled)
                .responseDiagnosticsProcessor(new ResponseDiagnosticsProcessorImplementation())
                .build();
    }

    @Override
    protected String getDatabaseName() {
        return dbName;
    }

    private static class ResponseDiagnosticsProcessorImplementation implements ResponseDiagnosticsProcessor {

        @Override
        public void processResponseDiagnostics(@Nullable ResponseDiagnostics responseDiagnostics) {
            log.debug("Response Diagnostics {}", responseDiagnostics);
        }
    }


//    @Bean("primaryCosmosAsyncClient")
//    public CosmosAsyncClient primaryCosmosAsyncClient() {
//        // build a dedicated builder for replica client
//        return new CosmosClientBuilder()
//                .endpoint(uri)
//                .key(key)
//                .buildAsyncClient();
//    }
//
//    @Bean("primaryCosmosFactory")
//    public CosmosFactory primaryCosmosFactory(@Qualifier("primaryCosmosAsyncClient") CosmosAsyncClient client) {
//        return new CosmosFactory(client, dbName);
//    }
//
//    @Bean("primaryCosmosTemplate")
//    public CosmosTemplate primaryCosmosTemplate(
//            @Qualifier("primaryCosmosFactory") CosmosFactory replicaFactory,
//            CosmosConfig cosmosConfig,
//            MappingCosmosConverter mappingCosmosConverter
//    ) {
//        return new CosmosTemplate(replicaFactory, cosmosConfig, mappingCosmosConverter);
//    }

}
