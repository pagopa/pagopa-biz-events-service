package it.gov.pagopa.bizeventsservice.repository.primary;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedFlux;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewUser;
import it.gov.pagopa.bizeventsservice.model.filterandorder.Order.TransactionListOrder;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort.Direction;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class BizEventsViewUserQueryRepositoryTest {

    private static final String TAX_CODE = "AAAAAA00A00A000A";
    private static final String DATABASE_NAME = "db";
    private static final String CONTAINER_NAME = "biz-events-view-user";
    private static final String CONTINUATION_TOKEN = "continuation-token";
    private static final String NEXT_CONTINUATION_TOKEN = "next-continuation-token";

    private final BizEventsViewUserQueryRepository repository =
            new BizEventsViewUserQueryRepository(false, 7);

    @Test
    void buildQuerySpecWithoutOptionalFiltersShouldNotContainOrAndIsNull() {
        SqlQuerySpec querySpec = repository.buildQuerySpec(
                TAX_CODE,
                false,
                null,
                null,
                TransactionListOrder.TRANSACTION_DATE,
                Direction.DESC
        );

        String query = querySpec.getQueryText();

        assertEquals(
                "SELECT * FROM c WHERE c.taxCode = @taxCode AND c.hidden = @hidden ORDER BY c.transactionDate DESC",
                query
        );
        assertFalse(query.contains("IS_NULL"));
        assertFalse(query.contains(" OR "));
        assertFalse(query.contains("c.isPayer"));
        assertFalse(query.contains("c.isDebtor"));

        assertParameterNames(querySpec, "@taxCode", "@hidden");
    }

    @Test
    void buildQuerySpecWithIsPayerShouldAddOnlyIsPayerFilter() {
        SqlQuerySpec querySpec = repository.buildQuerySpec(
                TAX_CODE,
                false,
                true,
                null,
                TransactionListOrder.TRANSACTION_DATE,
                Direction.DESC
        );

        String query = querySpec.getQueryText();

        assertEquals(
                "SELECT * FROM c WHERE c.taxCode = @taxCode AND c.hidden = @hidden AND c.isPayer = @isPayer ORDER BY c.transactionDate DESC",
                query
        );
        assertFalse(query.contains("IS_NULL"));
        assertFalse(query.contains(" OR "));
        assertTrue(query.contains("c.isPayer = @isPayer"));
        assertFalse(query.contains("c.isDebtor"));

        assertParameterNames(querySpec, "@taxCode", "@hidden", "@isPayer");
    }

    @Test
    void buildQuerySpecWithIsDebtorShouldAddOnlyIsDebtorFilter() {
        SqlQuerySpec querySpec = repository.buildQuerySpec(
                TAX_CODE,
                false,
                null,
                true,
                TransactionListOrder.TRANSACTION_DATE,
                Direction.DESC
        );

        String query = querySpec.getQueryText();

        assertEquals(
                "SELECT * FROM c WHERE c.taxCode = @taxCode AND c.hidden = @hidden AND c.isDebtor = @isDebtor ORDER BY c.transactionDate DESC",
                query
        );
        assertFalse(query.contains("IS_NULL"));
        assertFalse(query.contains(" OR "));
        assertFalse(query.contains("c.isPayer"));
        assertTrue(query.contains("c.isDebtor = @isDebtor"));

        assertParameterNames(querySpec, "@taxCode", "@hidden", "@isDebtor");
    }

    @Test
    void buildQuerySpecWithBothOptionalFiltersShouldAddBothFiltersWithoutOrAndIsNull() {
        SqlQuerySpec querySpec = repository.buildQuerySpec(
                TAX_CODE,
                false,
                true,
                true,
                TransactionListOrder.TRANSACTION_DATE,
                Direction.DESC
        );

        String query = querySpec.getQueryText();

        assertEquals(
                "SELECT * FROM c WHERE c.taxCode = @taxCode AND c.hidden = @hidden AND c.isPayer = @isPayer AND c.isDebtor = @isDebtor ORDER BY c.transactionDate DESC",
                query
        );
        assertFalse(query.contains("IS_NULL"));
        assertFalse(query.contains(" OR "));
        assertTrue(query.contains("c.isPayer = @isPayer"));
        assertTrue(query.contains("c.isDebtor = @isDebtor"));

        assertParameterNames(querySpec, "@taxCode", "@hidden", "@isPayer", "@isDebtor");
    }

    @Test
    void buildQuerySpecShouldNeverUseOptionalFilterAntiPattern() {
        SqlQuerySpec querySpec = repository.buildQuerySpec(
                TAX_CODE,
                false,
                null,
                null,
                TransactionListOrder.TRANSACTION_DATE,
                Direction.DESC
        );

        String query = querySpec.getQueryText();

        assertFalse(query.contains("IS_NULL"));
        assertFalse(query.contains(" OR "));
        assertFalse(query.contains("count"));
        assertFalse(query.toLowerCase().contains("count("));
    }

    @Test
    void findByTaxCodeAndOptionalFiltersShouldUseProvidedPageSizeContinuationTokenAndReturnFetchedPage() {
        CapturingPageFetcher pageFetcher = new CapturingPageFetcher(
                new CosmosQueryPage<>(Collections.emptyList(), NEXT_CONTINUATION_TOKEN)
        );

        BizEventsViewUserQueryRepository repo = new BizEventsViewUserQueryRepository(
                true,
                7,
                pageFetcher
        );

        CosmosQueryPage<BizEventsViewUser> result = repo.findByTaxCodeAndOptionalFilters(
                TAX_CODE,
                false,
                true,
                null,
                new BizEventsViewUserQueryPageRequest(
                        CONTINUATION_TOKEN,
                        5,
                        TransactionListOrder.TRANSACTION_DATE,
                        Direction.ASC
                )
        );

        assertNotNull(result);
        assertEquals(NEXT_CONTINUATION_TOKEN, result.getContinuationToken());
        assertSame(pageFetcher.response, result);

        assertEquals(CONTINUATION_TOKEN, pageFetcher.continuationToken);
        assertEquals(5, pageFetcher.pageSize);

        assertEquals(
                "SELECT * FROM c WHERE c.taxCode = @taxCode AND c.hidden = @hidden AND c.isPayer = @isPayer ORDER BY c.transactionDate ASC",
                pageFetcher.querySpec.getQueryText()
        );
        assertParameterNames(pageFetcher.querySpec, "@taxCode", "@hidden", "@isPayer");
        assertNotNull(pageFetcher.options);
    }

    @Test
    void findByTaxCodeAndOptionalFiltersShouldUseDefaultPageSizeWhenPageRequestIsNull() {
        CapturingPageFetcher pageFetcher = new CapturingPageFetcher(
                new CosmosQueryPage<>(Collections.emptyList(), null)
        );

        BizEventsViewUserQueryRepository repo = new BizEventsViewUserQueryRepository(
                false,
                0,
                pageFetcher
        );

        CosmosQueryPage<BizEventsViewUser> result = repo.findByTaxCodeAndOptionalFilters(
                TAX_CODE,
                false,
                null,
                null,
                null
        );

        assertNotNull(result);
        assertNull(result.getContinuationToken());

        assertNull(pageFetcher.continuationToken);
        assertEquals(10, pageFetcher.pageSize);

        assertEquals(
                "SELECT * FROM c WHERE c.taxCode = @taxCode AND c.hidden = @hidden ORDER BY c.transactionDate DESC",
                pageFetcher.querySpec.getQueryText()
        );
        assertParameterNames(pageFetcher.querySpec, "@taxCode", "@hidden");
        assertNotNull(pageFetcher.options);
    }

    @Test
    void findByTaxCodeAndOptionalFiltersShouldUseDefaultPageSizeWhenSizeIsNotPositive() {
        CapturingPageFetcher pageFetcher = new CapturingPageFetcher(
                new CosmosQueryPage<>(Collections.emptyList(), null)
        );

        BizEventsViewUserQueryRepository repo = new BizEventsViewUserQueryRepository(
                false,
                -1,
                pageFetcher
        );

        CosmosQueryPage<BizEventsViewUser> result = repo.findByTaxCodeAndOptionalFilters(
                TAX_CODE,
                true,
                null,
                true,
                new BizEventsViewUserQueryPageRequest(
                        null,
                        0,
                        TransactionListOrder.TRANSACTION_DATE,
                        Direction.DESC
                )
        );

        assertNotNull(result);
        assertEquals(10, pageFetcher.pageSize);

        assertEquals(
                "SELECT * FROM c WHERE c.taxCode = @taxCode AND c.hidden = @hidden AND c.isDebtor = @isDebtor ORDER BY c.transactionDate DESC",
                pageFetcher.querySpec.getQueryText()
        );
        assertParameterNames(pageFetcher.querySpec, "@taxCode", "@hidden", "@isDebtor");
        assertNotNull(pageFetcher.options);
    }

    @Test
    void findByTaxCodeAndOptionalFiltersShouldFetchFromCosmosWithContinuationTokenAndReturnResults() {
        RepositoryMocks mocks = createRepositoryMocks(true, 7);

        BizEventsViewUser viewUser = new BizEventsViewUser();
        FeedResponse<BizEventsViewUser> feedResponse = mock(FeedResponse.class);

        doReturn(List.of(viewUser)).when(feedResponse).getResults();
        doReturn(NEXT_CONTINUATION_TOKEN).when(feedResponse).getContinuationToken();

        doReturn(Flux.just(feedResponse))
                .when(mocks.pagedFlux)
                .byPage(CONTINUATION_TOKEN, 5);

        CosmosQueryPage<BizEventsViewUser> result = mocks.repository.findByTaxCodeAndOptionalFilters(
                TAX_CODE,
                false,
                true,
                null,
                new BizEventsViewUserQueryPageRequest(
                        CONTINUATION_TOKEN,
                        5,
                        TransactionListOrder.TRANSACTION_DATE,
                        Direction.ASC
                )
        );

        assertNotNull(result);
        assertEquals(1, result.getResults().size());
        assertEquals(NEXT_CONTINUATION_TOKEN, result.getContinuationToken());

        verify(mocks.cosmosAsyncClient).getDatabase(DATABASE_NAME);
        verify(mocks.database).getContainer(CONTAINER_NAME);
        verify(mocks.container).queryItems(
                any(SqlQuerySpec.class),
                any(CosmosQueryRequestOptions.class),
                eq(BizEventsViewUser.class)
        );
        verify(mocks.pagedFlux).byPage(CONTINUATION_TOKEN, 5);
    }

    @Test
    void findByTaxCodeAndOptionalFiltersShouldFetchFromCosmosWithoutContinuationTokenAndReturnEmptyPageWhenResultsAreEmpty() {
        RepositoryMocks mocks = createRepositoryMocks(false, 0);

        FeedResponse<BizEventsViewUser> feedResponse = mock(FeedResponse.class);

        doReturn(Collections.emptyList()).when(feedResponse).getResults();

        doReturn(Flux.just(feedResponse))
                .when(mocks.pagedFlux)
                .byPage(10);

        CosmosQueryPage<BizEventsViewUser> result = mocks.repository.findByTaxCodeAndOptionalFilters(
                TAX_CODE,
                false,
                null,
                null,
                new BizEventsViewUserQueryPageRequest(
                        null,
                        null,
                        null,
                        null
                )
        );

        assertNotNull(result);
        assertTrue(result.getResults().isEmpty());
        assertNull(result.getContinuationToken());

        verify(mocks.pagedFlux).byPage(10);
        verify(mocks.pagedFlux, never()).byPage(eq(CONTINUATION_TOKEN), anyInt());
    }

    @Test
    void findByTaxCodeAndOptionalFiltersShouldReturnEmptyPageWhenCosmosReturnsNoPage() {
        RepositoryMocks mocks = createRepositoryMocks(false, -1);

        doReturn(Flux.empty())
                .when(mocks.pagedFlux)
                .byPage(10);

        CosmosQueryPage<BizEventsViewUser> result = mocks.repository.findByTaxCodeAndOptionalFilters(
                TAX_CODE,
                true,
                null,
                true,
                new BizEventsViewUserQueryPageRequest(
                        null,
                        0,
                        TransactionListOrder.TRANSACTION_DATE,
                        Direction.DESC
                )
        );

        assertNotNull(result);
        assertTrue(result.getResults().isEmpty());
        assertNull(result.getContinuationToken());

        verify(mocks.pagedFlux).byPage(10);
    }

    private static void assertParameterNames(SqlQuerySpec querySpec, String... expectedNames) {
        List<String> actualNames = querySpec.getParameters()
                .stream()
                .map(SqlParameter::getName)
                .toList();

        assertEquals(List.of(expectedNames), actualNames);
    }

    private static class CapturingPageFetcher implements BizEventsViewUserQueryRepository.PageFetcher {

        private final CosmosQueryPage<BizEventsViewUser> response;

        private SqlQuerySpec querySpec;
        private CosmosQueryRequestOptions options;
        private String continuationToken;
        private int pageSize;

        private CapturingPageFetcher(CosmosQueryPage<BizEventsViewUser> response) {
            this.response = response;
        }

        @Override
        public CosmosQueryPage<BizEventsViewUser> fetch(
                SqlQuerySpec querySpec,
                CosmosQueryRequestOptions options,
                String continuationToken,
                int pageSize
        ) {
            this.querySpec = querySpec;
            this.options = options;
            this.continuationToken = continuationToken;
            this.pageSize = pageSize;
            return response;
        }
    }

    @SuppressWarnings("unchecked")
    private static RepositoryMocks createRepositoryMocks(
            boolean queryMetricsEnabled,
            int responseContinuationTokenLimitInKb
    ) {
        CosmosAsyncClient cosmosAsyncClient = mock(CosmosAsyncClient.class);
        CosmosAsyncDatabase database = mock(CosmosAsyncDatabase.class);
        CosmosAsyncContainer container = mock(CosmosAsyncContainer.class);
        CosmosPagedFlux<BizEventsViewUser> pagedFlux = mock(CosmosPagedFlux.class);

        doReturn(database)
                .when(cosmosAsyncClient)
                .getDatabase(DATABASE_NAME);

        doReturn(container)
                .when(database)
                .getContainer(CONTAINER_NAME);

        doReturn(pagedFlux)
                .when(container)
                .queryItems(
                        any(SqlQuerySpec.class),
                        any(CosmosQueryRequestOptions.class),
                        eq(BizEventsViewUser.class)
                );

        BizEventsViewUserQueryRepository repository = new BizEventsViewUserQueryRepository(
                cosmosAsyncClient,
                DATABASE_NAME,
                CONTAINER_NAME,
                queryMetricsEnabled,
                responseContinuationTokenLimitInKb
        );

        return new RepositoryMocks(
                repository,
                cosmosAsyncClient,
                database,
                container,
                pagedFlux
        );
    }

    private static class RepositoryMocks {

        private final BizEventsViewUserQueryRepository repository;
        private final CosmosAsyncClient cosmosAsyncClient;
        private final CosmosAsyncDatabase database;
        private final CosmosAsyncContainer container;
        private final CosmosPagedFlux<BizEventsViewUser> pagedFlux;

        private RepositoryMocks(
                BizEventsViewUserQueryRepository repository,
                CosmosAsyncClient cosmosAsyncClient,
                CosmosAsyncDatabase database,
                CosmosAsyncContainer container,
                CosmosPagedFlux<BizEventsViewUser> pagedFlux
        ) {
            this.repository = repository;
            this.cosmosAsyncClient = cosmosAsyncClient;
            this.database = database;
            this.container = container;
            this.pagedFlux = pagedFlux;
        }
    }
}