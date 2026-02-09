package it.gov.pagopa.bizeventsservice.repository.primary;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.repository.Query;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for biz-events-view-user collection
 */
@Repository
public interface BizEventsViewUserRepository extends CosmosRepository<BizEventsViewUser, String> {

    @Query("SELECT * FROM c WHERE c.taxCode = @taxCode AND c.hidden = @hidden AND (IS_NULL(@isPayer) = true OR c.isPayer = @isPayer) AND (IS_NULL(@isDebtor) = true OR c.isDebtor = @isDebtor)")
    Page<BizEventsViewUser> getBizEventsViewUserByTaxCode(@Param("taxCode") String taxCode, @Param("isPayer") Boolean isPayer, @Param("isDebtor") Boolean isDebtor, @Param("hidden") Boolean hidden, Pageable pageable);

    @Query("select * from c where c.taxCode = @taxCode and c.hidden = false")
    List<BizEventsViewUser> getBizEventsViewUserByTaxCode(@Param("taxCode") String taxCode);

    @Query("select * from c where c.transactionId=@transactionId and c.taxCode = @fiscalCode and c.hidden = @hidden")
    List<BizEventsViewUser> getBizEventsViewUserByTaxCodeAndTransactionIdAndHidden(String fiscalCode, String transactionId, Boolean hidden);

    @Query("select * from c where c.transactionId=@transactionId and c.taxCode = @fiscalCode and c.hidden = @hidden and STARTSWITH(c.id, @eventId)")
    List<BizEventsViewUser> findByFiscalCodeAndTransactionIdAndEventIdAndHidden(String fiscalCode, String transactionId, String eventId, Boolean hidden);

    @Query("select * from c where c.taxCode = @fiscalCode and c.hidden = false and STARTSWITH(c.id, @eventId)")
    List<BizEventsViewUser> getBizEventsViewUserByTaxCodeAndId(String fiscalCode, String eventId);
}
