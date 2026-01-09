package it.gov.pagopa.bizeventsservice.repository.primary;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.repository.Query;
import it.gov.pagopa.bizeventsservice.entity.BizEvent;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BizEventsPrimaryRepository extends CosmosRepository<BizEvent, String> {


    @Query("select * from c where c.transactionDetails.transaction.transactionId = @cartId")
    List<BizEvent> findByCartId(@Param("cartId") String cartId);


}
