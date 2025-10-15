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

    List<FundTransaction> findByPortfolioIdOrderByTransactionDateDesc(Long portfolioId);

    List<FundTransaction> findByUserIdOrderByTransactionDateDesc(Long userId);

    List<FundTransaction> findByUserIdAndTransactionTypeOrderByTransactionDateDesc(Long userId, String transactionType);

    List<FundTransaction> findByFundCodeOrderByTransactionDateDesc(String fundCode);

    @Query("SELECT t FROM FundTransaction t WHERE t.userId = :userId AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<FundTransaction> findByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT t FROM FundTransaction t WHERE t.portfolioId = :portfolioId AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<FundTransaction> findByPortfolioIdAndDateRange(
        @Param("portfolioId") Long portfolioId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT t FROM FundTransaction t WHERE t.userId = :userId ORDER BY t.transactionDate DESC LIMIT :limit")
    List<FundTransaction> findRecentTransactions(@Param("userId") Long userId, @Param("limit") int limit);

    boolean existsByDescriptionContaining(String description);

    long countByUserId(Long userId);

    long countByUserIdAndTransactionType(Long userId, String transactionType);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM FundTransaction t WHERE t.userId = :userId AND t.transactionType = 'BUY'")
    java.math.BigDecimal getTotalPurchaseAmountByUserId(@Param("userId") Long userId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM FundTransaction t WHERE t.userId = :userId AND t.transactionType = 'SELL'")
    java.math.BigDecimal getTotalSellAmountByUserId(@Param("userId") Long userId);
}