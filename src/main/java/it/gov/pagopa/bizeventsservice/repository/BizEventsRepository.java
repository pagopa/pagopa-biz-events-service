package it.gov.pagopa.bizeventsservice.repository;

import java.util.List;
import java.util.Map;

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

    @Query("select distinct" +
            " c.paymentInfo.totalNotice != \"1\" ? " +
            "   c.transactionDetails.transaction.transactionId : c.id as transactionId," +
            " c.transactionDetails.transaction != null ? " +
            "   c.transactionDetails.transaction.creationDate : c.paymentInfo.paymentDateTime as transactionDate," +
            " c.paymentInfo.totalNotice = \"1\" ? c.creditor.companyName : null as payeeName," +
            " c.paymentInfo.totalNotice = \"1\" ? " +
            "   (c.transactionDetails.transaction.grandTotal != null ? " +
            "       c.transactionDetails.transaction.grandTotal : c.paymentInfo.amount) : null as amount," +
            " c.paymentInfo.totalNotice != \"1\" ? " +
            "   \"true\" : \"false\" as isCart" +
            " from c" +
            " where c.eventStatus = \"DONE\"" +
            "   and (c.debtor.entityUniqueIdentifierValue = @fiscalCode or" +
            "        c.payer.entityUniqueIdentifierValue = @fiscalCode or" +
            "        c.transactionDetails.user.fiscalCode = @fiscalCode)" +
            " offset @start limit @size")
    List<Map<String, Object>> getTransactionPagedIds(
            @Param("fiscalCode") String fiscalCode,
            @Param("start") Integer start,
            @Param("size") Integer size);

    @Query("select c.transactionDetails.transaction.grandTotal as grandTotal, " +
            "c.paymentInfo.amount as amount," +
            "c.creditor.companyName as payeeName " +
            "from c" +
            "where c.transactionDetails.transaction.transactionId = @transactionId")
    List<Map<String, Object>> getCartData(
            @Param("transactionId") String transactionId
    );


}
