package it.gov.pagopa.bizeventsservice.repository;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import it.gov.pagopa.bizeventsservice.entity.view.BizEventsViewGeneral;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for biz-events-view-general collection
 */
@Repository
public interface BizEventsViewGeneralRepository extends CosmosRepository<BizEventsViewGeneral, String> { }
