package com.hanainplan.hana.fund.repository;

import com.hanainplan.hana.fund.entity.FundSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FundSubscriptionRepository extends JpaRepository<FundSubscription, Long> {

    List<FundSubscription> findByCustomerCiOrderByCreatedAtDesc(String customerCi);

    List<FundSubscription> findByCustomerCiAndStatusOrderByCreatedAtDesc(String customerCi, String status);

    @Query("SELECT f FROM FundSubscription f WHERE f.customerCi = :customerCi AND f.status IN ('ACTIVE', 'PARTIAL_SOLD') ORDER BY f.createdAt DESC")
    List<FundSubscription> findActiveSubscriptionsByCustomerCi(@Param("customerCi") String customerCi);

    @Query("SELECT f FROM FundSubscription f WHERE f.customerCi = :customerCi AND f.childFundCd = :childFundCd AND f.status IN ('ACTIVE', 'PARTIAL_SOLD')")
    Optional<FundSubscription> findActiveSubscription(
        @Param("customerCi") String customerCi,
        @Param("childFundCd") String childFundCd
    );

    List<FundSubscription> findByIrpAccountNumberOrderByCreatedAtDesc(String irpAccountNumber);

    List<FundSubscription> findByFundCodeOrderByCreatedAtDesc(String fundCode);

    @Query("SELECT f FROM FundSubscription f WHERE f.status IN ('ACTIVE', 'PARTIAL_SOLD') ORDER BY f.createdAt DESC")
    List<FundSubscription> findAllActiveSubscriptions();

    long countByCustomerCi(String customerCi);

    @Query("SELECT COUNT(f) FROM FundSubscription f WHERE f.customerCi = :customerCi AND f.status IN ('ACTIVE', 'PARTIAL_SOLD')")
    long countActiveSubscriptionsByCustomerCi(@Param("customerCi") String customerCi);

    long countByFundCodeAndStatus(String fundCode, String status);

    List<FundSubscription> findByStatusIn(List<String> statuses);
}