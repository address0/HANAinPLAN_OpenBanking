package com.hanainplan.hana.fund.repository;

import com.hanainplan.hana.fund.entity.FundTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 펀드 거래 내역 Repository
 */
@Repository
public interface FundTransactionRepository extends JpaRepository<FundTransaction, Long> {

    /**
     * 고객의 모든 거래 내역 조회 (최신순)
     */
    List<FundTransaction> findByCustomerCiOrderByTransactionDateDesc(String customerCi);

    /**
     * 특정 가입의 거래 내역 조회 (최신순)
     */
    List<FundTransaction> findBySubscriptionIdOrderByTransactionDateDesc(Long subscriptionId);

    /**
     * 고객의 특정 기간 거래 내역 조회
     */
    @Query("SELECT t FROM FundTransaction t " +
           "WHERE t.customerCi = :customerCi " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.transactionDate DESC")
    List<FundTransaction> findByCustomerCiAndDateRange(
            @Param("customerCi") String customerCi,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 고객의 매수/매도 내역만 조회
     */
    @Query("SELECT t FROM FundTransaction t " +
           "WHERE t.customerCi = :customerCi " +
           "AND t.transactionType = :transactionType " +
           "ORDER BY t.transactionDate DESC")
    List<FundTransaction> findByCustomerCiAndTransactionType(
            @Param("customerCi") String customerCi,
            @Param("transactionType") String transactionType
    );

    /**
     * 고객의 실현 손익 합계 조회 (매도 거래만)
     */
    @Query("SELECT COALESCE(SUM(t.profit), 0) FROM FundTransaction t " +
           "WHERE t.customerCi = :customerCi " +
           "AND t.transactionType = 'SELL' " +
           "AND t.status = 'COMPLETED'")
    java.math.BigDecimal getTotalRealizedProfit(@Param("customerCi") String customerCi);

    /**
     * 고객의 총 매수 금액 조회
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM FundTransaction t " +
           "WHERE t.customerCi = :customerCi " +
           "AND t.transactionType = 'BUY' " +
           "AND t.status = 'COMPLETED'")
    java.math.BigDecimal getTotalPurchaseAmount(@Param("customerCi") String customerCi);

    /**
     * 고객의 총 매도 금액 조회
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM FundTransaction t " +
           "WHERE t.customerCi = :customerCi " +
           "AND t.transactionType = 'SELL' " +
           "AND t.status = 'COMPLETED'")
    java.math.BigDecimal getTotalRedemptionAmount(@Param("customerCi") String customerCi);

    /**
     * 고객의 총 수수료 조회
     */
    @Query("SELECT COALESCE(SUM(t.fee), 0) FROM FundTransaction t " +
           "WHERE t.customerCi = :customerCi " +
           "AND t.status = 'COMPLETED'")
    java.math.BigDecimal getTotalFees(@Param("customerCi") String customerCi);
}

