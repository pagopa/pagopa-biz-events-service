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

@Repository
public class BizEventsViewUserQueryRepository {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final String DEFAULT_ORDER_COLUMN = "transactionDate";

    private final CosmosAsyncContainer container;
    private final boolean queryMetricsEnabled;
    private final int responseContinuationTokenLimitInKb;

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
    }

    /*
     * Constructor used only by unit tests that verify query generation.
     */
    BizEventsViewUserQueryRepository(
            boolean queryMetricsEnabled,
            int responseContinuationTokenLimitInKb
    ) {
        this.container = null;
        this.queryMetricsEnabled = queryMetricsEnabled;
        this.responseContinuationTokenLimitInKb = responseContinuationTokenLimitInKb;
    }

    public CosmosQueryPage<BizEventsViewUser> findByTaxCodeAndOptionalFilters(
            String taxCode,
            Boolean hidden,
            Boolean isPayer,
            Boolean isDebtor,
            String continuationToken,
            Integer size,
            TransactionListOrder orderBy,
            Direction ordering
    ) {
        int pageSize = Optional.ofNullable(size)
                .filter(s -> s > 0)
                .orElse(DEFAULT_PAGE_SIZE);

        SqlQuerySpec querySpec = buildQuerySpec(
                taxCode,
                hidden,
                isPayer,
                isDebtor,
                orderBy,
                ordering
        );

        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        options.setPartitionKey(new PartitionKey(taxCode));
        options.setQueryMetricsEnabled(queryMetricsEnabled);

        if (responseContinuationTokenLimitInKb > 0) {
            options.setResponseContinuationTokenLimitInKb(responseContinuationTokenLimitInKb);
        }

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

        if (page == null || page.getResults().isEmpty()) {
            return new CosmosQueryPage<>(Collections.emptyList(), null);
        }

        return new CosmosQueryPage<>(
                page.getResults(),
                page.getContinuationToken()
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
}