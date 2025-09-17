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
    
    // 계좌 ID로 거래 내역 조회 (페이징)
    Page<Transaction> findByFromAccountIdOrToAccountIdOrderByTransactionDateDesc(Long fromAccountId, Long toAccountId, Pageable pageable);
    
    // 계좌 ID로 거래 내역 조회 (전체)
    List<Transaction> findByFromAccountIdOrToAccountIdOrderByTransactionDateDesc(Long fromAccountId, Long toAccountId);
    
    // 거래번호로 거래 조회
    Optional<Transaction> findByTransactionNumber(String transactionNumber);
    
    // 계좌 ID와 거래 유형으로 거래 내역 조회
    List<Transaction> findByFromAccountIdAndTransactionTypeOrToAccountIdAndTransactionTypeOrderByTransactionDateDesc(
        Long fromAccountId, Transaction.TransactionType transactionType1,
        Long toAccountId, Transaction.TransactionType transactionType2);
    
    // 계좌 ID와 거래 상태로 거래 내역 조회
    List<Transaction> findByFromAccountIdAndTransactionStatusOrToAccountIdAndTransactionStatusOrderByTransactionDateDesc(
        Long fromAccountId, Transaction.TransactionStatus transactionStatus1,
        Long toAccountId, Transaction.TransactionStatus transactionStatus2);
    
    // 기간별 거래 내역 조회
    @Query("SELECT t FROM Transaction t WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId) " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findTransactionsByAccountAndDateRange(
        @Param("accountId") Long accountId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);
    
    // 기간별 거래 내역 조회 (페이징)
    @Query("SELECT t FROM Transaction t WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId) " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    Page<Transaction> findTransactionsByAccountAndDateRange(
        @Param("accountId") Long accountId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable);
    
    // 거래 유형별 거래 내역 조회
    @Query("SELECT t FROM Transaction t WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId) " +
           "AND t.transactionType = :transactionType ORDER BY t.transactionDate DESC")
    List<Transaction> findTransactionsByAccountAndType(
        @Param("accountId") Long accountId,
        @Param("transactionType") Transaction.TransactionType transactionType);
    
    // 거래 분류별 거래 내역 조회
    @Query("SELECT t FROM Transaction t WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId) " +
           "AND t.transactionCategory = :transactionCategory ORDER BY t.transactionDate DESC")
    List<Transaction> findTransactionsByAccountAndCategory(
        @Param("accountId") Long accountId,
        @Param("transactionCategory") Transaction.TransactionCategory transactionCategory);
    
    // 계좌별 거래 금액 합계 조회
    @Query("SELECT t.transactionType, SUM(t.amount) FROM Transaction t " +
           "WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId) " +
           "AND t.transactionStatus = 'COMPLETED' " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate " +
           "GROUP BY t.transactionType")
    List<Object[]> getTransactionSumByType(
        @Param("accountId") Long accountId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);
    
    // 계좌별 월별 거래 통계
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
    
    // 최근 거래 내역 조회 (최대 10건)
    @Query("SELECT t FROM Transaction t WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId) " +
           "ORDER BY t.transactionDate DESC")
    List<Transaction> findRecentTransactions(@Param("accountId") Long accountId, Pageable pageable);
    
    // 거래 상태별 거래 수 조회
    long countByFromAccountIdAndTransactionStatusOrToAccountIdAndTransactionStatus(
        Long fromAccountId, Transaction.TransactionStatus transactionStatus1,
        Long toAccountId, Transaction.TransactionStatus transactionStatus2);
    
    // 실패한 거래 조회
    @Query("SELECT t FROM Transaction t WHERE (t.fromAccountId = :accountId OR t.toAccountId = :accountId) " +
           "AND t.transactionStatus = 'FAILED' ORDER BY t.transactionDate DESC")
    List<Transaction> findFailedTransactions(@Param("accountId") Long accountId);
}
