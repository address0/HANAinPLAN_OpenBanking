package com.hanainplan.hana.fund.repository;

import com.hanainplan.hana.fund.entity.FundTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FundTransactionRepository extends JpaRepository<FundTransaction, Long> {

    List<FundTransaction> findByCustomerCiOrderByTransactionDateDesc(String customerCi);

    List<FundTransaction> findBySubscriptionIdOrderByTransactionDateDesc(Long subscriptionId);

    @Query("SELECT t FROM FundTransaction t " +
           "WHERE t.customerCi = :customerCi " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.transactionDate DESC")
    List<FundTransaction> findByCustomerCiAndDateRange(
            @Param("customerCi") String customerCi,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT t FROM FundTransaction t " +
           "WHERE t.customerCi = :customerCi " +
           "AND t.transactionType = :transactionType " +
           "ORDER BY t.transactionDate DESC")
    List<FundTransaction> findByCustomerCiAndTransactionType(
            @Param("customerCi") String customerCi,
            @Param("transactionType") String transactionType
    );

    @Query("SELECT COALESCE(SUM(t.profit), 0) FROM FundTransaction t " +
           "WHERE t.customerCi = :customerCi " +
           "AND t.transactionType = 'SELL' " +
           "AND t.status = 'COMPLETED'")
    java.math.BigDecimal getTotalRealizedProfit(@Param("customerCi") String customerCi);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM FundTransaction t " +
           "WHERE t.customerCi = :customerCi " +
           "AND t.transactionType = 'BUY' " +
           "AND t.status = 'COMPLETED'")
    java.math.BigDecimal getTotalPurchaseAmount(@Param("customerCi") String customerCi);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM FundTransaction t " +
           "WHERE t.customerCi = :customerCi " +
           "AND t.transactionType = 'SELL' " +
           "AND t.status = 'COMPLETED'")
    java.math.BigDecimal getTotalRedemptionAmount(@Param("customerCi") String customerCi);

    @Query("SELECT COALESCE(SUM(t.fee), 0) FROM FundTransaction t " +
           "WHERE t.customerCi = :customerCi " +
           "AND t.status = 'COMPLETED'")
    java.math.BigDecimal getTotalFees(@Param("customerCi") String customerCi);

    List<FundTransaction> findBySettlementDateAndStatus(LocalDate settlementDate, String status);
}