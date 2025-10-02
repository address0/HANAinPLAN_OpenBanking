package com.hanainplan.domain.banking.repository;

import com.hanainplan.domain.banking.entity.DepositSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 예금 가입 정보 Repository
 */
@Repository
public interface DepositSubscriptionRepository extends JpaRepository<DepositSubscription, Long> {

    /**
     * 사용자 ID로 가입 내역 조회
     */
    List<DepositSubscription> findByUserId(Long userId);

    /**
     * 사용자 ID로 가입 내역 조회 (가입일 내림차순)
     */
    List<DepositSubscription> findByUserIdOrderBySubscriptionDateDesc(Long userId);

    /**
     * 고객 CI로 가입 내역 조회
     */
    List<DepositSubscription> findByCustomerCi(String customerCi);

    /**
     * 계좌번호로 가입 내역 조회
     */
    Optional<DepositSubscription> findByAccountNumber(String accountNumber);

    /**
     * 사용자 ID와 상태로 가입 내역 조회
     */
    List<DepositSubscription> findByUserIdAndStatus(Long userId, String status);

    /**
     * 고객 CI와 상태로 가입 내역 조회
     */
    List<DepositSubscription> findByCustomerCiAndStatus(String customerCi, String status);

    /**
     * 은행 코드로 가입 내역 조회
     */
    List<DepositSubscription> findByBankCode(String bankCode);

    /**
     * 사용자 ID와 은행 코드로 가입 내역 조회
     */
    List<DepositSubscription> findByUserIdAndBankCode(Long userId, String bankCode);

    /**
     * 예금 상품 코드로 가입 내역 조회
     */
    List<DepositSubscription> findByDepositCode(String depositCode);

    /**
     * 상태별 가입 내역 조회
     */
    List<DepositSubscription> findByStatus(String status);

    /**
     * 만기일 기준 가입 내역 조회
     */
    List<DepositSubscription> findByMaturityDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * 상품 유형별 조회
     */
    List<DepositSubscription> findByProductType(Integer productType);

    /**
     * 사용자 ID와 상품 유형으로 조회
     */
    List<DepositSubscription> findByUserIdAndProductType(Long userId, Integer productType);

    /**
     * 오늘 만기되는 가입 내역 조회
     */
    @Query("SELECT ds FROM DepositSubscription ds WHERE ds.maturityDate = CURRENT_DATE AND ds.status = 'ACTIVE'")
    List<DepositSubscription> findMaturingToday();

    /**
     * 이자 지급일이 도래한 가입 내역 조회
     */
    @Query("SELECT ds FROM DepositSubscription ds WHERE ds.nextInterestPaymentDate <= CURRENT_DATE AND ds.status = 'ACTIVE'")
    List<DepositSubscription> findReadyForInterestPayment();

    /**
     * 은행별 가입 통계
     */
    @Query("SELECT ds.bankCode, ds.bankName, COUNT(ds), SUM(ds.currentBalance) " +
           "FROM DepositSubscription ds WHERE ds.status = 'ACTIVE' GROUP BY ds.bankCode, ds.bankName")
    List<Object[]> getSubscriptionStatisticsByBank();

    /**
     * 사용자별 총 예금 잔액
     */
    @Query("SELECT SUM(ds.currentBalance) FROM DepositSubscription ds WHERE ds.userId = :userId AND ds.status = 'ACTIVE'")
    BigDecimal getTotalBalanceByUserId(@Param("userId") Long userId);
}
