package com.hanainplan.domain.banking.repository;

import com.hanainplan.domain.banking.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByFromAccountIdOrToAccountIdOrderByTransactionDateDesc(Long fromAccountId, Long toAccountId, Pageable pageable);

    List<Transaction> findByFromAccountIdOrToAccountIdOrderByTransactionDateDesc(Long fromAccountId, Long toAccountId);

    Optional<Transaction> findByTransactionNumber(String transactionNumber);

    List<Transaction> findByFromAccountIdAndTransactionTypeOrToAccountIdAndTransactionTypeOrderByTransactionDateDesc(
        Long fromAccountId, Transaction.TransactionType transactionType1,
        Long toAccountId, Transaction.TransactionType transactionType2);

    List<Transaction> findByFromAccountIdAndTransactionStatusOrToAccountIdAndTransactionStatusOrderByTransactionDateDesc(
        Long fromAccountId, Transaction.TransactionStatus transactionStatus1,
        Long toAccountId, Transaction.TransactionStatus transactionStatus2);

    @Query("SELECT t FROM Transaction t WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId) " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findTransactionsByAccountAndDateRange(
        @Param("accountId") Long accountId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM Transaction t WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId) " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    Page<Transaction> findTransactionsByAccountAndDateRange(
        @Param("accountId") Long accountId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId) " +
           "AND t.transactionType = :transactionType ORDER BY t.transactionDate DESC")
    List<Transaction> findTransactionsByAccountAndType(
        @Param("accountId") Long accountId,
        @Param("transactionType") Transaction.TransactionType transactionType);

    @Query("SELECT t FROM Transaction t WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId) " +
           "AND t.transactionCategory = :transactionCategory ORDER BY t.transactionDate DESC")
    List<Transaction> findTransactionsByAccountAndCategory(
        @Param("accountId") Long accountId,
        @Param("transactionCategory") Transaction.TransactionCategory transactionCategory);

    @Query("SELECT t.transactionType, SUM(t.amount) FROM Transaction t " +
           "WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId) " +
           "AND t.transactionStatus = 'COMPLETED' " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "GROUP BY t.transactionType")
    List<Object[]> getTransactionSumByType(
        @Param("accountId") Long accountId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT YEAR(t.transactionDate) as year, MONTH(t.transactionDate) as month, " +
           "t.transactionType, COUNT(t) as count, SUM(t.amount) as totalAmount " +
           "FROM Transaction t " +
           "WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId) " +
           "AND t.transactionStatus = 'COMPLETED' " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate), t.transactionType " +
           "ORDER BY year DESC, month DESC")
    List<Object[]> getMonthlyTransactionStats(
        @Param("accountId") Long accountId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM Transaction t WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId) " +
           "ORDER BY t.transactionDate DESC")
    List<Transaction> findRecentTransactions(@Param("accountId") Long accountId, Pageable pageable);

    long countByFromAccountIdAndTransactionStatusOrToAccountIdAndTransactionStatus(
        Long fromAccountId, Transaction.TransactionStatus transactionStatus1,
        Long toAccountId, Transaction.TransactionStatus transactionStatus2);

    @Query("SELECT t FROM Transaction t WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId) " +
           "AND t.transactionStatus = 'FAILED' ORDER BY t.transactionDate DESC")
    List<Transaction> findFailedTransactions(@Param("accountId") Long accountId);

    @Query("SELECT t FROM Transaction t WHERE t.fromAccountNumber = :accountNumber OR t.toAccountNumber = :accountNumber ORDER BY t.transactionDate DESC")
    List<Transaction> findByAccountNumber(@Param("accountNumber") String accountNumber);

    @Query("SELECT t FROM Transaction t JOIN BankingAccount a ON (t.fromAccountId = a.accountId OR t.toAccountId = a.accountId) " +
           "WHERE a.accountNumber = :accountNumber ORDER BY t.transactionDate DESC")
    List<Transaction> findTop1ByAccountAccountNumberOrderByTransactionDateDesc(@Param("accountNumber") String accountNumber);

    @Query("SELECT t FROM Transaction t JOIN BankingAccount a ON (t.fromAccountId = a.accountId OR t.toAccountId = a.accountId) " +
           "WHERE a.accountNumber = :accountNumber ORDER BY t.transactionDate DESC")
    Page<Transaction> findByAccountNumberOrderByTransactionDateDesc(@Param("accountNumber") String accountNumber, Pageable pageable);

    @Query("SELECT t FROM Transaction t JOIN BankingAccount a ON (t.fromAccountId = a.accountId OR t.toAccountId = a.accountId) " +
           "WHERE a.accountNumber = :accountNumber ORDER BY t.transactionDate DESC")
    List<Transaction> findByAccountNumberOrderByTransactionDateDesc(@Param("accountNumber") String accountNumber);

    @Query("SELECT t FROM Transaction t JOIN BankingAccount a ON (t.fromAccountId = a.accountId OR t.toAccountId = a.accountId) " +
           "WHERE a.accountNumber = :accountNumber AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    Page<Transaction> findByAccountNumberAndDateRange(
        @Param("accountNumber") String accountNumber,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable);

    @Query("SELECT t FROM Transaction t JOIN BankingAccount a ON (t.fromAccountId = a.accountId OR t.toAccountId = a.accountId) " +
           "WHERE a.accountNumber = :accountNumber AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findByAccountNumberAndDateRange(
        @Param("accountNumber") String accountNumber,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "JOIN BankingAccount a ON (t.toAccountId = a.accountId) " +
           "WHERE a.customerCi = :customerCi " +
           "AND a.accountType = 6 " +
           "AND t.transactionType = 'DEPOSIT' " +
           "AND t.transactionDate >= :startDate " +
           "AND t.transactionDate < :endDate " +
           "AND t.transactionStatus = 'COMPLETED'")
    Optional<java.math.BigDecimal> sumIrpDepositsByCustomerCiAndYear(
        @Param("customerCi") String customerCi,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);
}