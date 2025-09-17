package com.hanainplan.domain.banking.repository;

import com.hanainplan.domain.banking.entity.AutoTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AutoTransferRepository extends JpaRepository<AutoTransfer, Long> {
    
    // 출금 계좌 ID로 자동이체 목록 조회
    List<AutoTransfer> findByFromAccountIdOrderByCreatedAtDesc(Long fromAccountId);
    
    // 입금 계좌 ID로 자동이체 목록 조회
    List<AutoTransfer> findByToAccountIdOrderByCreatedAtDesc(Long toAccountId);
    
    // 계좌 ID로 자동이체 목록 조회 (출금 또는 입금)
    @Query("SELECT at FROM AutoTransfer at WHERE at.fromAccountId = :accountId OR at.toAccountId = :accountId ORDER BY at.createdAt DESC")
    List<AutoTransfer> findByAccountId(@Param("accountId") Long accountId);
    
    // 자동이체 상태별 조회
    List<AutoTransfer> findByFromAccountIdAndTransferStatusOrderByCreatedAtDesc(Long fromAccountId, AutoTransfer.TransferStatus transferStatus);
    
    // 활성 자동이체 조회
    List<AutoTransfer> findByFromAccountIdAndTransferStatusOrderByNextTransferDateAsc(Long fromAccountId, AutoTransfer.TransferStatus transferStatus);
    
    // 실행 가능한 자동이체 조회 (오늘 실행해야 할 것들)
    @Query("SELECT at FROM AutoTransfer at WHERE at.transferStatus = 'ACTIVE' " +
           "AND at.nextTransferDate = :today " +
           "AND (at.maxTransferCount IS NULL OR at.transferCount < at.maxTransferCount) " +
           "AND at.failureCount < at.maxFailureCount " +
           "ORDER BY at.nextTransferDate ASC")
    List<AutoTransfer> findExecutableAutoTransfers(@Param("today") LocalDate today);
    
    // 특정 계좌의 실행 가능한 자동이체 조회
    @Query("SELECT at FROM AutoTransfer at WHERE at.fromAccountId = :accountId " +
           "AND at.transferStatus = 'ACTIVE' " +
           "AND at.nextTransferDate = :today " +
           "AND (at.maxTransferCount IS NULL OR at.transferCount < at.maxTransferCount) " +
           "AND at.failureCount < at.maxFailureCount " +
           "ORDER BY at.nextTransferDate ASC")
    List<AutoTransfer> findExecutableAutoTransfersByAccount(@Param("accountId") Long accountId, @Param("today") LocalDate today);
    
    // 자동이체명으로 검색
    List<AutoTransfer> findByFromAccountIdAndTransferNameContainingIgnoreCaseOrderByCreatedAtDesc(Long fromAccountId, String transferName);
    
    // 상대방 계좌번호로 검색
    List<AutoTransfer> findByFromAccountIdAndCounterpartAccountNumberOrderByCreatedAtDesc(Long fromAccountId, String counterpartAccountNumber);
    
    // 상대방 이름으로 검색
    List<AutoTransfer> findByFromAccountIdAndCounterpartNameContainingIgnoreCaseOrderByCreatedAtDesc(Long fromAccountId, String counterpartName);
    
    // 자동이체 상태별 개수 조회
    long countByFromAccountIdAndTransferStatus(Long fromAccountId, AutoTransfer.TransferStatus transferStatus);
    
    // 만료된 자동이체 조회 (종료일이 지난 것들)
    @Query("SELECT at FROM AutoTransfer at WHERE at.transferStatus = 'ACTIVE' " +
           "AND at.endDate IS NOT NULL AND at.endDate < :today")
    List<AutoTransfer> findExpiredAutoTransfers(@Param("today") LocalDate today);
    
    // 최대 실패 횟수에 도달한 자동이체 조회
    @Query("SELECT at FROM AutoTransfer at WHERE at.transferStatus = 'ACTIVE' " +
           "AND at.failureCount >= at.maxFailureCount")
    List<AutoTransfer> findMaxFailureReachedAutoTransfers();
    
    // 자동이체 실행 통계
    @Query("SELECT at.transferStatus, COUNT(at) FROM AutoTransfer at WHERE at.fromAccountId = :accountId GROUP BY at.transferStatus")
    List<Object[]> getAutoTransferStatsByAccount(@Param("accountId") Long accountId);
    
    // 월별 자동이체 금액 합계
    @Query("SELECT YEAR(at.createdAt) as year, MONTH(at.createdAt) as month, " +
           "COUNT(at) as count, SUM(at.amount) as totalAmount " +
           "FROM AutoTransfer at " +
           "WHERE at.fromAccountId = :accountId " +
           "AND at.transferStatus = 'ACTIVE' " +
           "GROUP BY YEAR(at.createdAt), MONTH(at.createdAt) " +
           "ORDER BY year DESC, month DESC")
    List<Object[]> getMonthlyAutoTransferStats(@Param("accountId") Long accountId);
    
    // 자동이체 ID와 계좌 ID로 조회 (권한 확인용)
    Optional<AutoTransfer> findByAutoTransferIdAndFromAccountId(Long autoTransferId, Long fromAccountId);
    
    // 자동이체 ID와 계좌 ID로 조회 (입금 계좌 포함)
    @Query("SELECT at FROM AutoTransfer at WHERE at.autoTransferId = :autoTransferId " +
           "AND (at.fromAccountId = :accountId OR at.toAccountId = :accountId)")
    Optional<AutoTransfer> findByAutoTransferIdAndAccountId(@Param("autoTransferId") Long autoTransferId, @Param("accountId") Long accountId);
}
