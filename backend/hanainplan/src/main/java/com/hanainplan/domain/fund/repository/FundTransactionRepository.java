package com.hanainplan.domain.fund.repository;

import com.hanainplan.domain.fund.entity.FundTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FundTransactionRepository extends JpaRepository<FundTransaction, Long> {

    // 포트폴리오 ID로 거래 내역 조회
    List<FundTransaction> findByPortfolioIdOrderByTransactionDateDesc(Long portfolioId);

    // 사용자 ID로 거래 내역 조회
    List<FundTransaction> findByUserIdOrderByTransactionDateDesc(Long userId);

    // 사용자 ID와 거래 유형으로 조회
    List<FundTransaction> findByUserIdAndTransactionTypeOrderByTransactionDateDesc(Long userId, String transactionType);

    // 펀드 코드로 거래 내역 조회
    List<FundTransaction> findByFundCodeOrderByTransactionDateDesc(String fundCode);

    // 기간별 거래 내역 조회
    @Query("SELECT t FROM FundTransaction t WHERE t.userId = :userId AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<FundTransaction> findByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // 포트폴리오별 기간 조회
    @Query("SELECT t FROM FundTransaction t WHERE t.portfolioId = :portfolioId AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<FundTransaction> findByPortfolioIdAndDateRange(
        @Param("portfolioId") Long portfolioId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    // 최근 N건 조회
    @Query("SELECT t FROM FundTransaction t WHERE t.userId = :userId ORDER BY t.transactionDate DESC LIMIT :limit")
    List<FundTransaction> findRecentTransactions(@Param("userId") Long userId, @Param("limit") int limit);

    // description에 특정 문자열 포함 여부 확인 (동기화 중복 방지용)
    boolean existsByDescriptionContaining(String description);

    // 사용자의 거래 건수
    long countByUserId(Long userId);

    // 사용자의 거래 유형별 건수
    long countByUserIdAndTransactionType(Long userId, String transactionType);

    // 사용자의 총 매수 금액 합계
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM FundTransaction t WHERE t.userId = :userId AND t.transactionType = 'BUY'")
    java.math.BigDecimal getTotalPurchaseAmountByUserId(@Param("userId") Long userId);

    // 사용자의 총 매도 금액 합계
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM FundTransaction t WHERE t.userId = :userId AND t.transactionType = 'SELL'")
    java.math.BigDecimal getTotalSellAmountByUserId(@Param("userId") Long userId);
}

