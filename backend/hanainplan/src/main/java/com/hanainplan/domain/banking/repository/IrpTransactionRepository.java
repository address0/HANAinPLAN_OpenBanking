package com.hanainplan.domain.banking.repository;

import com.hanainplan.domain.banking.entity.IrpAccount;
import com.hanainplan.domain.banking.entity.IrpTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * IRP 거래 내역 리포지토리
 */
@Repository
public interface IrpTransactionRepository extends JpaRepository<IrpTransaction, Long> {

    /**
     * 특정 계좌의 모든 거래 내역 조회 (최신순)
     */
    List<IrpTransaction> findByIrpAccountOrderByTransactionDateDescCreatedDateDesc(IrpAccount irpAccount);

    /**
     * 특정 계좌의 모든 거래 내역 조회 (페이징)
     */
    Page<IrpTransaction> findByIrpAccountOrderByTransactionDateDescCreatedDateDesc(IrpAccount irpAccount, Pageable pageable);

    /**
     * 특정 계좌의 특정 기간 거래 내역 조회
     */
    List<IrpTransaction> findByIrpAccountAndTransactionDateBetweenOrderByTransactionDateDesc(
        IrpAccount irpAccount, LocalDate startDate, LocalDate endDate);

    /**
     * 특정 계좌의 특정 기간 거래 내역 조회 (페이징)
     */
    Page<IrpTransaction> findByIrpAccountAndTransactionDateBetweenOrderByTransactionDateDesc(
        IrpAccount irpAccount, LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * 특정 고객의 모든 은행 IRP 거래 내역 조회
     */
    List<IrpTransaction> findByCustomerCiOrderByTransactionDateDescCreatedDateDesc(String customerCi);

    /**
     * 특정 고객의 특정 기간 IRP 거래 내역 조회
     */
    List<IrpTransaction> findByCustomerCiAndTransactionDateBetweenOrderByTransactionDateDesc(
        String customerCi, LocalDate startDate, LocalDate endDate);

    /**
     * 특정 은행의 모든 거래 내역 조회
     */
    List<IrpTransaction> findByBankCodeOrderByTransactionDateDescCreatedDateDesc(String bankCode);

    /**
     * 특정 은행의 특정 기간 거래 내역 조회
     */
    List<IrpTransaction> findByBankCodeAndTransactionDateBetweenOrderByTransactionDateDesc(
        String bankCode, LocalDate startDate, LocalDate endDate);

    /**
     * 특정 거래 유형의 내역 조회
     */
    List<IrpTransaction> findByIrpAccountAndTransactionTypeOrderByTransactionDateDesc(
        IrpAccount irpAccount, String transactionType);

    /**
     * 특정 거래 유형의 내역 조회 (페이징)
     */
    Page<IrpTransaction> findByIrpAccountAndTransactionTypeOrderByTransactionDateDesc(
        IrpAccount irpAccount, String transactionType, Pageable pageable);

    /**
     * 수익 거래 내역 조회
     */
    @Query("SELECT it FROM IrpTransaction it WHERE it.irpAccount = :account AND it.transactionType = 'RETURN' ORDER BY it.transactionDate DESC")
    List<IrpTransaction> findReturnTransactionsByAccount(@Param("account") IrpAccount account);

    /**
     * 수수료 거래 내역 조회
     */
    @Query("SELECT it FROM IrpTransaction it WHERE it.irpAccount = :account AND it.transactionType = 'FEE' ORDER BY it.transactionDate DESC")
    List<IrpTransaction> findFeeTransactionsByAccount(@Param("account") IrpAccount account);

    /**
     * 최근 거래 내역 조회 (특정 계좌의 최근 N건)
     */
    @Query("SELECT it FROM IrpTransaction it WHERE it.irpAccount = :account ORDER BY it.transactionDate DESC, it.createdDate DESC")
    List<IrpTransaction> findRecentTransactionsByAccount(@Param("account") IrpAccount account, Pageable pageable);

    /**
     * 특정 계좌의 총 납입금액 조회
     */
    @Query("SELECT COALESCE(SUM(it.amount), 0) FROM IrpTransaction it WHERE it.irpAccount = :account AND it.transactionType = 'CONTRIBUTION'")
    BigDecimal getTotalContributionsByAccount(@Param("account") IrpAccount account);

    /**
     * 특정 계좌의 총 수익금액 조회
     */
    @Query("SELECT COALESCE(SUM(it.amount), 0) FROM IrpTransaction it WHERE it.irpAccount = :account AND it.transactionType = 'RETURN'")
    BigDecimal getTotalReturnsByAccount(@Param("account") IrpAccount account);

    /**
     * 특정 계좌의 총 수수료 조회
     */
    @Query("SELECT COALESCE(SUM(it.amount), 0) FROM IrpTransaction it WHERE it.irpAccount = :account AND it.transactionType = 'FEE'")
    BigDecimal getTotalFeesByAccount(@Param("account") IrpAccount account);

    /**
     * 특정 기간의 납입 거래 내역 조회
     */
    List<IrpTransaction> findByTransactionDateBetweenAndTransactionTypeOrderByTransactionDateDesc(
        LocalDate startDate, LocalDate endDate, String transactionType);

    /**
     * 동기화가 필요한 거래 내역 조회
     */
    @Query("SELECT it FROM IrpTransaction it WHERE it.processingStatus = 'PENDING' OR it.syncDate < :threshold")
    List<IrpTransaction> findTransactionsNeedingSync(@Param("threshold") LocalDateTime threshold);

    /**
     * 처리 실패한 거래 내역 조회
     */
    List<IrpTransaction> findByProcessingStatusOrderByCreatedDateDesc(String processingStatus);

    /**
     * 특정 계좌의 최신 거래 여부 확인
     */
    @Query("SELECT COUNT(it) > 0 FROM IrpTransaction it WHERE it.irpAccount = :account AND it.isLatest = true")
    boolean hasLatestTransactionByAccount(@Param("account") IrpAccount account);

    /**
     * 특정 계좌의 최신 거래 조회
     */
    @Query("SELECT it FROM IrpTransaction it WHERE it.irpAccount = :account AND it.isLatest = true ORDER BY it.transactionDate DESC")
    Optional<IrpTransaction> findLatestTransactionByAccount(@Param("account") IrpAccount account);

    /**
     * 특정 기간 동안의 거래 건수 조회
     */
    @Query("SELECT COUNT(it) FROM IrpTransaction it WHERE it.transactionDate BETWEEN :startDate AND :endDate")
    long countTransactionsByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 특정 기간 동안의 특정 은행 거래 건수 조회
     */
    @Query("SELECT COUNT(it) FROM IrpTransaction it WHERE it.bankCode = :bankCode AND it.transactionDate BETWEEN :startDate AND :endDate")
    long countTransactionsByBankAndDateRange(@Param("bankCode") String bankCode,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    /**
     * 특정 기간 동안의 총 거래금액 조회
     */
    @Query("SELECT COALESCE(SUM(it.amount), 0) FROM IrpTransaction it WHERE it.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalTransactionAmountByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 특정 기간 동안의 특정 은행 총 거래금액 조회
     */
    @Query("SELECT COALESCE(SUM(it.amount), 0) FROM IrpTransaction it WHERE it.bankCode = :bankCode AND it.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalTransactionAmountByBankAndDateRange(@Param("bankCode") String bankCode,
                                                         @Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);

    /**
     * 중복 거래 확인 (은행별 거래 ID로)
     */
    Optional<IrpTransaction> findByBankCodeAndExternalTransactionId(String bankCode, String externalTransactionId);

    /**
     * 참조번호로 거래 조회
     */
    Optional<IrpTransaction> findByReferenceNumber(String referenceNumber);

    /**
     * 특정 계좌의 특정 날짜 거래 내역 존재 여부 확인
     */
    @Query("SELECT COUNT(it) > 0 FROM IrpTransaction it WHERE it.irpAccount = :account AND it.transactionDate = :date")
    boolean existsByAccountAndDate(@Param("account") IrpAccount account, @Param("date") LocalDate date);

    /**
     * 특정 계좌의 월별 납입 내역 조회
     */
    @Query("SELECT it FROM IrpTransaction it WHERE it.irpAccount = :account AND it.transactionType = 'CONTRIBUTION' " +
           "AND FUNCTION('YEAR', it.transactionDate) = FUNCTION('YEAR', :yearMonth) " +
           "AND FUNCTION('MONTH', it.transactionDate) = FUNCTION('MONTH', :yearMonth) " +
           "ORDER BY it.transactionDate DESC")
    List<IrpTransaction> findMonthlyContributionsByAccount(@Param("account") IrpAccount account, @Param("yearMonth") LocalDate yearMonth);
}
