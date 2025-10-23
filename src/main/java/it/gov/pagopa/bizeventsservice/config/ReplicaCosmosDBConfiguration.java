package it.gov.pagopa.bizeventsservice.config;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.data.cosmos.CosmosFactory;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.azure.spring.data.cosmos.core.convert.MappingCosmosConverter;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableCosmosRepositories("it.gov.pagopa.bizeventsservice.repository.replica")
@ConditionalOnExpression("'${info.properties.environment}'!='test'")
public class ReplicaCosmosDBConfiguration {

    @Value("${azure.cosmos.uri}")
    private String uri;

    @Value("${azure.cosmos.key}")
    private String key;

    @Value("${azure.cosmos.database}")
    private String dbName;

    @Value("${azure.cosmos.read.region}")
    private String readRegion;

    /**
     * Replica-specific CosmosAsyncClient configured with preferredRegions.
     */
    @Bean("replicaCosmosAsyncClient")
    public CosmosAsyncClient replicaCosmosAsyncClient() {
        // build a dedicated builder for replica client
        return new CosmosClientBuilder()
                .endpoint(uri)
                .key(key)
                .preferredRegions(List.of(readRegion))
                .buildAsyncClient();
    }

    /**
     * Create a CosmosFactory backed by the replica client.
     */
    @Bean("replicaCosmosFactory")
    public CosmosFactory replicaCosmosFactory(@Qualifier("replicaCosmosAsyncClient") CosmosAsyncClient client) {
        return new CosmosFactory(client, dbName);
    }

    /**
     * Create a CosmosTemplate using the existing MappingCosmosConverter and CosmosConfig beans.
     * Reuse the same MappingCosmosConverter and CosmosConfig as the primary template.
     */
    @Bean("replicaCosmosTemplate")
    public CosmosTemplate replicaCosmosTemplate(
            @Qualifier("replicaCosmosFactory") CosmosFactory replicaFactory,
            CosmosConfig cosmosConfig,
            MappingCosmosConverter mappingCosmosConverter
    ) {
        return new CosmosTemplate(replicaFactory, cosmosConfig, mappingCosmosConverter);
    }

}
