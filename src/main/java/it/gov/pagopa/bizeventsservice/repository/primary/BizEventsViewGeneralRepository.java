package it.gov.pagopa.bizeventsservice.repository.primary;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.repository.Query;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewGeneral;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for biz-events-view-general collection
 */
@Repository
public interface BizEventsViewGeneralRepository extends CosmosRepository<BizEventsViewGeneral, String> {
    @Query("SELECT * FROM c WHERE c.transactionId = @transactionId")
    List<BizEventsViewGeneral> findByTransactionId(@Param("transactionId") String transactionId);
}
