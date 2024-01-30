package it.gov.pagopa.bizeventsservice.repository;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.repository.Query;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewCart;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for biz-events-view-cart collection
 */
@Repository
public interface BizEventsViewCartRepository extends CosmosRepository<BizEventsViewCart, String> {
    @Query("select * from c where c.transactionId = @transactionId")
    List<BizEventsViewCart> getBizEventsViewCartByTransactionId(@Param("transactionId") String transactionId);

    @Query("select * from c where " +
            "c.transactionId = @transactionId and " +
            "(c.payer.taxCode = @taxCode or c.debtor.taxCode = @taxCode)")
    List<BizEventsViewCart> getBizEventsViewCartByTransactionIdAndFilteredByFiscalCode(
            @Param("transactionId") String transactionId,
            @Param("taxCode") String taxCode);
}
