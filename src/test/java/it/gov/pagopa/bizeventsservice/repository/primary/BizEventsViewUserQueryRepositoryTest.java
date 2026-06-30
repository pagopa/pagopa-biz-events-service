package it.gov.pagopa.bizeventsservice.repository.primary;

import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewUser;
import it.gov.pagopa.bizeventsservice.model.filterandorder.Order.TransactionListOrder;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort.Direction;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BizEventsViewUserQueryRepositoryTest {

    private static final String TAX_CODE = "AAAAAA00A00A000A";
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

        BizEventsViewUserQueryRepository repository = new BizEventsViewUserQueryRepository(
                true,
                7,
                pageFetcher
        );

        CosmosQueryPage<BizEventsViewUser> result = repository.findByTaxCodeAndOptionalFilters(
                TAX_CODE,
                false,
                true,
                null,
                CONTINUATION_TOKEN,
                5,
                TransactionListOrder.TRANSACTION_DATE,
                Direction.ASC
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
    void findByTaxCodeAndOptionalFiltersShouldUseDefaultPageSizeWhenSizeIsNull() {
        CapturingPageFetcher pageFetcher = new CapturingPageFetcher(
                new CosmosQueryPage<>(Collections.emptyList(), null)
        );

        BizEventsViewUserQueryRepository repository = new BizEventsViewUserQueryRepository(
                false,
                0,
                pageFetcher
        );

        CosmosQueryPage<BizEventsViewUser> result = repository.findByTaxCodeAndOptionalFilters(
                TAX_CODE,
                false,
                null,
                null,
                null,
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

        BizEventsViewUserQueryRepository repository = new BizEventsViewUserQueryRepository(
                false,
                -1,
                pageFetcher
        );

        CosmosQueryPage<BizEventsViewUser> result = repository.findByTaxCodeAndOptionalFilters(
                TAX_CODE,
                true,
                null,
                true,
                null,
                0,
                TransactionListOrder.TRANSACTION_DATE,
                Direction.DESC
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
}