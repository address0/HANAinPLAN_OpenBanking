package com.hanainplan.shinhan.account.repository;

import com.hanainplan.shinhan.account.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByAccountAccountNumberOrderByTransactionDatetimeDesc(String accountNumber);

    @Query("SELECT t FROM Transaction t WHERE t.account.customerCi = :ci ORDER BY t.transactionDatetime DESC")
    List<Transaction> findByCustomerCiOrderByTransactionDatetimeDesc(@Param("ci") String ci);

    @Query("SELECT t FROM Transaction t WHERE t.account.accountNumber = :accountNumber " +
           "AND t.transactionDatetime BETWEEN :startDate AND :endDate " +
           "ORDER BY t.transactionDatetime DESC")
    List<Transaction> findByAccountNumberAndDateRange(
        @Param("accountNumber") String accountNumber,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT t FROM Transaction t WHERE t.account.customerCi = :ci " +
           "AND t.transactionDatetime BETWEEN :startDate AND :endDate " +
           "ORDER BY t.transactionDatetime DESC")
    List<Transaction> findByCustomerCiAndDateRange(
        @Param("ci") String ci,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}