package it.gov.pagopa.bizeventsservice.repository.primary;

import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import it.gov.pagopa.bizeventsservice.model.filterandorder.Order.TransactionListOrder;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort.Direction;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BizEventsViewUserQueryRepositoryTest {

    private static final String TAX_CODE = "AAAAAA00A00A000A";

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

    private static void assertParameterNames(SqlQuerySpec querySpec, String... expectedNames) {
        List<String> actualNames = querySpec.getParameters()
                .stream()
                .map(SqlParameter::getName)
                .toList();

        assertEquals(List.of(expectedNames), actualNames);
    }
}