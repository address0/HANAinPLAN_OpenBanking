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

    List<AutoTransfer> findByFromAccountIdOrderByCreatedAtDesc(Long fromAccountId);

    List<AutoTransfer> findByToAccountIdOrderByCreatedAtDesc(Long toAccountId);

    @Query("SELECT at FROM AutoTransfer at WHERE at.fromAccountId = :accountId OR at.toAccountId = :accountId ORDER BY at.createdAt DESC")
    List<AutoTransfer> findByAccountId(@Param("accountId") Long accountId);

    List<AutoTransfer> findByFromAccountIdAndTransferStatusOrderByCreatedAtDesc(Long fromAccountId, AutoTransfer.TransferStatus transferStatus);

    List<AutoTransfer> findByFromAccountIdAndTransferStatusOrderByNextTransferDateAsc(Long fromAccountId, AutoTransfer.TransferStatus transferStatus);

    @Query("SELECT at FROM AutoTransfer at WHERE at.transferStatus = 'ACTIVE' " +
           "AND at.nextTransferDate = :today " +
           "AND (at.maxTransferCount IS NULL OR at.transferCount < at.maxTransferCount) " +
           "AND at.failureCount < at.maxFailureCount " +
           "ORDER BY at.nextTransferDate ASC")
    List<AutoTransfer> findExecutableAutoTransfers(@Param("today") LocalDate today);

    @Query("SELECT at FROM AutoTransfer at WHERE at.fromAccountId = :accountId " +
           "AND at.transferStatus = 'ACTIVE' " +
           "AND at.nextTransferDate = :today " +
           "AND (at.maxTransferCount IS NULL OR at.transferCount < at.maxTransferCount) " +
           "AND at.failureCount < at.maxFailureCount " +
           "ORDER BY at.nextTransferDate ASC")
    List<AutoTransfer> findExecutableAutoTransfersByAccount(@Param("accountId") Long accountId, @Param("today") LocalDate today);

    List<AutoTransfer> findByFromAccountIdAndTransferNameContainingIgnoreCaseOrderByCreatedAtDesc(Long fromAccountId, String transferName);

    List<AutoTransfer> findByFromAccountIdAndCounterpartAccountNumberOrderByCreatedAtDesc(Long fromAccountId, String counterpartAccountNumber);

    List<AutoTransfer> findByFromAccountIdAndCounterpartNameContainingIgnoreCaseOrderByCreatedAtDesc(Long fromAccountId, String counterpartName);

    long countByFromAccountIdAndTransferStatus(Long fromAccountId, AutoTransfer.TransferStatus transferStatus);

    @Query("SELECT at FROM AutoTransfer at WHERE at.transferStatus = 'ACTIVE' " +
           "AND at.endDate IS NOT NULL AND at.endDate < :today")
    List<AutoTransfer> findExpiredAutoTransfers(@Param("today") LocalDate today);

    @Query("SELECT at FROM AutoTransfer at WHERE at.transferStatus = 'ACTIVE' " +
           "AND at.failureCount >= at.maxFailureCount")
    List<AutoTransfer> findMaxFailureReachedAutoTransfers();

    @Query("SELECT at.transferStatus, COUNT(at) FROM AutoTransfer at WHERE at.fromAccountId = :accountId GROUP BY at.transferStatus")
    List<Object[]> getAutoTransferStatsByAccount(@Param("accountId") Long accountId);

    @Query("SELECT YEAR(at.createdAt) as year, MONTH(at.createdAt) as month, " +
           "COUNT(at) as count, SUM(at.amount) as totalAmount " +
           "FROM AutoTransfer at " +
           "WHERE at.fromAccountId = :accountId " +
           "AND at.transferStatus = 'ACTIVE' " +
           "GROUP BY YEAR(at.createdAt), MONTH(at.createdAt) " +
           "ORDER BY year DESC, month DESC")
    List<Object[]> getMonthlyAutoTransferStats(@Param("accountId") Long accountId);

    Optional<AutoTransfer> findByAutoTransferIdAndFromAccountId(Long autoTransferId, Long fromAccountId);

    @Query("SELECT at FROM AutoTransfer at WHERE at.autoTransferId = :autoTransferId " +
           "AND (at.fromAccountId = :accountId OR at.toAccountId = :accountId)")
    Optional<AutoTransfer> findByAutoTransferIdAndAccountId(@Param("autoTransferId") Long autoTransferId, @Param("accountId") Long accountId);
}