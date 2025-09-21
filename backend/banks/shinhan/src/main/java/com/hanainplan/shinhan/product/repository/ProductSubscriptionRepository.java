package com.hanainplan.shinhan.product.repository;

import com.hanainplan.shinhan.product.entity.ProductSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductSubscriptionRepository extends JpaRepository<ProductSubscription, Long> {

    // 고객 CI로 가입 목록 조회
    List<ProductSubscription> findByCustomerCiOrderBySubscriptionDateDesc(String customerCi);

    // 계좌번호로 가입 조회
    Optional<ProductSubscription> findByAccountNumber(String accountNumber);

    // 상품코드로 가입 목록 조회
    List<ProductSubscription> findByProductCode(String productCode);

    // 상태별 가입 목록 조회
    List<ProductSubscription> findByStatus(String status);

    // 고객 CI와 상태로 가입 목록 조회
    List<ProductSubscription> findByCustomerCiAndStatus(String customerCi, String status);

    // 만기일이 임박한 가입 목록 조회 (30일 이내)
    @Query("SELECT s FROM ProductSubscription s WHERE s.maturityDate BETWEEN :startDate AND :endDate AND s.status = 'ACTIVE'")
    List<ProductSubscription> findUpcomingMaturities(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // 이자지급 예정일이 임박한 가입 목록 조회 (7일 이내)
    @Query("SELECT s FROM ProductSubscription s WHERE s.nextInterestPaymentDate BETWEEN :startDate AND :endDate AND s.status = 'ACTIVE'")
    List<ProductSubscription> findUpcomingInterestPayments(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // 특정 기간 내 가입한 상품 목록 조회
    @Query("SELECT s FROM ProductSubscription s WHERE s.subscriptionDate BETWEEN :startDate AND :endDate")
    List<ProductSubscription> findBySubscriptionDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // 미납 회차가 있는 가입 목록 조회
    @Query("SELECT s FROM ProductSubscription s WHERE s.missedInstallments > 0 AND s.status = 'ACTIVE'")
    List<ProductSubscription> findWithMissedInstallments();

    // 특정 금리 이상의 가입 목록 조회
    @Query("SELECT s FROM ProductSubscription s WHERE s.finalAppliedRate >= :minRate AND s.status = 'ACTIVE'")
    List<ProductSubscription> findByMinInterestRate(@Param("minRate") java.math.BigDecimal minRate);

    // 취급점별 가입 목록 조회
    List<ProductSubscription> findByBranchName(String branchName);

    // 고객 CI와 상품코드로 중복 가입 확인
    boolean existsByCustomerCiAndProductCode(String customerCi, String productCode);
}
