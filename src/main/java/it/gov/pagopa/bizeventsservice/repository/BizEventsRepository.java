package it.gov.pagopa.bizeventsservice.repository;

import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.repository.Query;

import it.gov.pagopa.bizeventsservice.entity.BizEvent;

@Repository
public interface BizEventsRepository extends CosmosRepository<BizEvent, String> {

    // TODO when available replace idPa with the paFiscalCode field
    @Query("select * from c where c.creditor.idPA = @organizationFiscalCode and c.paymentInfo.paymentToken = @iur and c.debtorPosition.iuv = @iuv and StringToNumber(c.debtorPosition.modelType) > 1")
    List<BizEvent> getBizEventByOrgFiscCodeIuvAndIur(@Param("organizationFiscalCode") String organizationFiscalCode,
                                                     @Param("iur") String iur, @Param("iuv") String iuv);

    @Query("select * from c where c.creditor.idPA = @organizationFiscalCode and c.debtorPosition.iuv = @iuv")
    List<BizEvent> getBizEventByOrgFiscalCodeAndIuv(@Param("organizationFiscalCode") String organizationFiscalCode,
                                                    @Param("iuv") String iuv);

    @Query("select distinct value c from c JOIN t IN c.transferList where t.fiscalCodePA = @organizationFiscalCode and c.paymentInfo.paymentToken = @iur and StringToNumber(c.debtorPosition.modelType) > 1")
    List<BizEvent> getBizEventByOrgFiscCodeAndIur(@Param("organizationFiscalCode") String organizationFiscalCode,
                                                     @Param("iur") String iur);

}
