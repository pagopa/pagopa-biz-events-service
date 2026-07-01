package it.gov.pagopa.bizeventsservice.repository.primary;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewUser;
import it.gov.pagopa.bizeventsservice.model.filterandorder.Order.TransactionListOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Repository
public class BizEventsViewUserQueryRepository {
	
	private static final Logger log = LoggerFactory.getLogger(BizEventsViewUserQueryRepository.class);

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final String DEFAULT_ORDER_COLUMN = "transactionDate";

    private final CosmosAsyncContainer container;
    private final boolean queryMetricsEnabled;
    private final int responseContinuationTokenLimitInKb;
    private final PageFetcher pageFetcher;

    @Autowired
    public BizEventsViewUserQueryRepository(
            @Qualifier("cosmosAsyncClient") CosmosAsyncClient cosmosAsyncClient,
            @Value("${azure.cosmos.database}") String databaseName,
            @Value("${azure.cosmos.biz-events-view-user-container-name}") String containerName,
            @Value("${azure.cosmos.populate-query-metrics:false}") boolean queryMetricsEnabled,
            @Value("${azure.cosmos.responseContinuationTokenLimitInKb:7}") int responseContinuationTokenLimitInKb
    ) {
        this.container = cosmosAsyncClient
                .getDatabase(databaseName)
                .getContainer(containerName);
        this.queryMetricsEnabled = queryMetricsEnabled;
        this.responseContinuationTokenLimitInKb = responseContinuationTokenLimitInKb;
        this.pageFetcher = this::fetchFromCosmos;
    }

    /*
     * Constructor used only by unit tests that verify query generation.
     */
    BizEventsViewUserQueryRepository(
            boolean queryMetricsEnabled,
            int responseContinuationTokenLimitInKb
    ) {
        this(queryMetricsEnabled, responseContinuationTokenLimitInKb,
                (querySpec, options, continuationToken, pageSize) ->
                        new CosmosQueryPage<>(Collections.emptyList(), null));
    }

    /*
     * Constructor used only by unit tests that verify findByTaxCodeAndOptionalFilters
     * without calling Cosmos DB.
     */
    BizEventsViewUserQueryRepository(
            boolean queryMetricsEnabled,
            int responseContinuationTokenLimitInKb,
            PageFetcher pageFetcher
    ) {
        this.container = null;
        this.queryMetricsEnabled = queryMetricsEnabled;
        this.responseContinuationTokenLimitInKb = responseContinuationTokenLimitInKb;
        this.pageFetcher = pageFetcher;
    }

    public CosmosQueryPage<BizEventsViewUser> findByTaxCodeAndOptionalFilters(
            String taxCode,
            Boolean hidden,
            Boolean isPayer,
            Boolean isDebtor,
            BizEventsViewUserQueryPageRequest pageRequest
    ) {
        BizEventsViewUserQueryPageRequest request = Optional.ofNullable(pageRequest)
                .orElse(new BizEventsViewUserQueryPageRequest(null, null, null, null));

        int pageSize = Optional.ofNullable(request.size())
                .filter(s -> s > 0)
                .orElse(DEFAULT_PAGE_SIZE);

        SqlQuerySpec querySpec = buildQuerySpec(
                taxCode,
                hidden,
                isPayer,
                isDebtor,
                request.orderBy(),
                request.ordering()
        );
        
        logQuery(querySpec, request.continuationToken(), pageSize);

        CosmosQueryRequestOptions options = buildQueryRequestOptions(taxCode);

        return pageFetcher.fetch(
                querySpec,
                options,
                request.continuationToken(),
                pageSize
        );
    }
    
    SqlQuerySpec buildQuerySpec(
            String taxCode,
            Boolean hidden,
            Boolean isPayer,
            Boolean isDebtor,
            TransactionListOrder orderBy,
            Direction ordering
    ) {
        String orderColumn = Optional.ofNullable(orderBy)
                .map(TransactionListOrder::getColumnName)
                .orElse(DEFAULT_ORDER_COLUMN);

        Direction sortDirection = Optional.ofNullable(ordering)
                .orElse(Direction.DESC);

        StringBuilder query = new StringBuilder(
                "SELECT * FROM c WHERE c.taxCode = @taxCode AND c.hidden = @hidden"
        );

        List<SqlParameter> parameters = new ArrayList<>();
        parameters.add(new SqlParameter("@taxCode", taxCode));
        parameters.add(new SqlParameter("@hidden", hidden));

        if (isPayer != null) {
            query.append(" AND c.isPayer = @isPayer");
            parameters.add(new SqlParameter("@isPayer", isPayer));
        }

        if (isDebtor != null) {
            query.append(" AND c.isDebtor = @isDebtor");
            parameters.add(new SqlParameter("@isDebtor", isDebtor));
        }

        query.append(" ORDER BY c.")
                .append(orderColumn)
                .append(" ")
                .append(sortDirection.name());

        return new SqlQuerySpec(query.toString(), parameters);
    }

    private CosmosQueryRequestOptions buildQueryRequestOptions(String taxCode) {
        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setPartitionKey(new PartitionKey(taxCode));
        options.setQueryMetricsEnabled(queryMetricsEnabled);

        if (responseContinuationTokenLimitInKb > 0) {
            options.setResponseContinuationTokenLimitInKb(responseContinuationTokenLimitInKb);
        }

        return options;
    }

    private CosmosQueryPage<BizEventsViewUser> fetchFromCosmos(
            SqlQuerySpec querySpec,
            CosmosQueryRequestOptions options,
            String continuationToken,
            int pageSize
    ) {
        FeedResponse<BizEventsViewUser> page = StringUtils.hasText(continuationToken)
                ? container
                        .queryItems(querySpec, options, BizEventsViewUser.class)
                        .byPage(continuationToken, pageSize)
                        .next()
                        .block()
                : container
                        .queryItems(querySpec, options, BizEventsViewUser.class)
                        .byPage(pageSize)
                        .next()
                        .block();
        
        if (page != null && log.isDebugEnabled()) {
            log.debug(
                    "Cosmos biz-events-view-user query completed. requestCharge={}, resultCount={}, hasContinuationToken={}",
                    page.getRequestCharge(),
                    page.getResults().size(),
                    StringUtils.hasText(page.getContinuationToken())
            );
        }

        if (page == null || page.getResults().isEmpty()) {
            return new CosmosQueryPage<>(Collections.emptyList(), null);
        }

        return new CosmosQueryPage<>(
                page.getResults(),
                page.getContinuationToken()
        );
    }
    
    private void logQuery(SqlQuerySpec querySpec, String continuationToken, int pageSize) {
        if (log.isDebugEnabled()) {
            log.debug("Cosmos biz-events-view-user query text: {}", querySpec.getQueryText());
            log.debug(
                    "Cosmos biz-events-view-user query parameters: {}",
                    querySpec.getParameters()
                            .stream()
                            .map(parameter -> parameter.getName() + "=" + maskSensitiveValue(
                                    parameter.getName(),
                                    parameter.getValue(Object.class)
                            ))
                            .collect(Collectors.joining(", "))
            );
            log.debug(
                    "Cosmos biz-events-view-user query pagination: continuationTokenPresent={}, pageSize={}",
                    StringUtils.hasText(continuationToken),
                    pageSize
            );
        }
    }

    private Object maskSensitiveValue(String parameterName, Object value) {
        if ("@taxCode".equals(parameterName) && value instanceof String taxCode && taxCode.length() > 4) {
            return "***" + taxCode.substring(taxCode.length() - 4);
        }

        return value;
    }

   

    /*
     * Interface used to isolate the Cosmos DB fetch operation from query-building logic.
     * This allows unit tests to verify pagination, query options, and generated SQL without
     * invoking the Azure Cosmos SDK directly.
     */
    @FunctionalInterface
    interface PageFetcher {
        CosmosQueryPage<BizEventsViewUser> fetch(
                SqlQuerySpec querySpec,
                CosmosQueryRequestOptions options,
                String continuationToken,
                int pageSize
        );
    }
}