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

    // 고객 CI로 펀드 가입 목록 조회
    List<FundSubscription> findByCustomerCiOrderByCreatedAtDesc(String customerCi);

    // 고객 CI와 상태로 조회
    List<FundSubscription> findByCustomerCiAndStatusOrderByCreatedAtDesc(String customerCi, String status);

    // 활성 펀드 가입 목록 조회 (보유중 + 일부매도)
    @Query("SELECT f FROM FundSubscription f WHERE f.customerCi = :customerCi AND f.status IN ('ACTIVE', 'PARTIAL_SOLD') ORDER BY f.createdAt DESC")
    List<FundSubscription> findActiveSubscriptionsByCustomerCi(@Param("customerCi") String customerCi);

    // 고객 CI와 클래스 펀드 코드로 활성 가입 조회
    @Query("SELECT f FROM FundSubscription f WHERE f.customerCi = :customerCi AND f.childFundCd = :childFundCd AND f.status IN ('ACTIVE', 'PARTIAL_SOLD')")
    Optional<FundSubscription> findActiveSubscription(
        @Param("customerCi") String customerCi,
        @Param("childFundCd") String childFundCd
    );

    // IRP 계좌번호로 조회
    List<FundSubscription> findByIrpAccountNumberOrderByCreatedAtDesc(String irpAccountNumber);

    // 펀드 코드로 모든 가입 조회
    List<FundSubscription> findByFundCodeOrderByCreatedAtDesc(String fundCode);

    // 모든 활성 펀드 가입 조회
    @Query("SELECT f FROM FundSubscription f WHERE f.status IN ('ACTIVE', 'PARTIAL_SOLD') ORDER BY f.createdAt DESC")
    List<FundSubscription> findAllActiveSubscriptions();

    // 고객의 펀드 가입 개수
    long countByCustomerCi(String customerCi);

    // 고객의 활성 펀드 가입 개수
    @Query("SELECT COUNT(f) FROM FundSubscription f WHERE f.customerCi = :customerCi AND f.status IN ('ACTIVE', 'PARTIAL_SOLD')")
    long countActiveSubscriptionsByCustomerCi(@Param("customerCi") String customerCi);

    // 특정 펀드의 총 가입자 수
    long countByFundCodeAndStatus(String fundCode, String status);

    // 상태별 펀드 가입 조회 (배치용)
    List<FundSubscription> findByStatusIn(List<String> statuses);
}

