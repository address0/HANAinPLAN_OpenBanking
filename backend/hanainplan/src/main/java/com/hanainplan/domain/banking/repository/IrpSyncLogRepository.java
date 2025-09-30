package com.hanainplan.domain.banking.repository;

import com.hanainplan.domain.banking.entity.IrpSyncLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * IRP 동기화 로그 리포지토리
 */
@Repository
public interface IrpSyncLogRepository extends JpaRepository<IrpSyncLog, Long> {

    /**
     * 특정 은행의 모든 동기화 로그 조회 (최신순)
     */
    List<IrpSyncLog> findByBankCodeOrderByStartTimeDesc(String bankCode);

    /**
     * 특정 은행의 특정 기간 동기화 로그 조회
     */
    List<IrpSyncLog> findByBankCodeAndStartTimeBetweenOrderByStartTimeDesc(
        String bankCode, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 특정 고객의 동기화 로그 조회
     */
    List<IrpSyncLog> findByCustomerCiOrderByStartTimeDesc(String customerCi);

    /**
     * 특정 동기화 유형의 로그 조회
     */
    List<IrpSyncLog> findBySyncTypeOrderByStartTimeDesc(String syncType);

    /**
     * 특정 동기화 상태의 로그 조회
     */
    List<IrpSyncLog> findBySyncStatusOrderByStartTimeDesc(String syncStatus);

    /**
     * 특정 기간의 동기화 로그 조회
     */
    List<IrpSyncLog> findByStartTimeBetweenOrderByStartTimeDesc(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 특정 은행의 최근 동기화 로그 조회
     */
    @Query("SELECT sl FROM IrpSyncLog sl WHERE sl.bankCode = :bankCode ORDER BY sl.startTime DESC")
    List<IrpSyncLog> findRecentSyncLogsByBank(@Param("bankCode") String bankCode);

    /**
     * 재시도 가능한 실패 동기화 로그 조회
     */
    @Query("SELECT sl FROM IrpSyncLog sl WHERE sl.syncStatus = 'FAILED' AND sl.retryCount < sl.maxRetryCount ORDER BY sl.startTime ASC")
    List<IrpSyncLog> findRetryableFailedSyncs();

    /**
     * 특정 은행의 재시도 가능한 실패 동기화 로그 조회
     */
    @Query("SELECT sl FROM IrpSyncLog sl WHERE sl.bankCode = :bankCode AND sl.syncStatus = 'FAILED' AND sl.retryCount < sl.maxRetryCount ORDER BY sl.startTime ASC")
    List<IrpSyncLog> findRetryableFailedSyncsByBank(@Param("bankCode") String bankCode);

    /**
     * 특정 기간 동안의 성공한 동기화 로그 조회
     */
    List<IrpSyncLog> findBySyncStatusAndStartTimeBetweenOrderByStartTimeDesc(
        String syncStatus, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 특정 은행의 특정 기간 성공 동기화 로그 조회
     */
    List<IrpSyncLog> findByBankCodeAndSyncStatusAndStartTimeBetweenOrderByStartTimeDesc(
        String bankCode, String syncStatus, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 진행 중인 동기화 로그 조회
     */
    List<IrpSyncLog> findBySyncStatusOrderByStartTimeAsc(String syncStatus);

    /**
     * 특정 은행의 진행 중인 동기화 로그 조회
     */
    @Query("SELECT sl FROM IrpSyncLog sl WHERE sl.bankCode = :bankCode AND sl.syncStatus = 'RUNNING' ORDER BY sl.startTime ASC")
    List<IrpSyncLog> findRunningSyncsByBank(@Param("bankCode") String bankCode);

    /**
     * 수동 동기화 로그 조회
     */
    List<IrpSyncLog> findByIsManualTrueOrderByStartTimeDesc();

    /**
     * 특정 은행의 수동 동기화 로그 조회
     */
    List<IrpSyncLog> findByBankCodeAndIsManualTrueOrderByStartTimeDesc(String bankCode);

    /**
     * 특정 동기화 대상의 로그 조회
     */
    List<IrpSyncLog> findBySyncTargetOrderByStartTimeDesc(String syncTarget);

    /**
     * 특정 은행의 특정 동기화 대상 로그 조회
     */
    List<IrpSyncLog> findByBankCodeAndSyncTargetOrderByStartTimeDesc(String bankCode, String syncTarget);

    /**
     * 동기화 통계 조회
     */
    @Query("SELECT sl.bankCode, sl.syncStatus, COUNT(sl) FROM IrpSyncLog sl " +
           "WHERE sl.startTime >= :since GROUP BY sl.bankCode, sl.syncStatus ORDER BY sl.bankCode, sl.syncStatus")
    List<Object[]> getSyncStatistics(@Param("since") LocalDateTime since);

    /**
     * 은행별 동기화 성공률 조회
     */
    @Query("SELECT sl.bankCode, " +
           "COUNT(CASE WHEN sl.syncStatus = 'SUCCESS' THEN 1 END) * 100.0 / COUNT(sl) as successRate " +
           "FROM IrpSyncLog sl WHERE sl.startTime >= :since GROUP BY sl.bankCode ORDER BY successRate DESC")
    List<Object[]> getSyncSuccessRateByBank(@Param("since") LocalDateTime since);

    /**
     * 평균 동기화 시간 조회
     */
    @Query("SELECT AVG(sl.durationMs) FROM IrpSyncLog sl WHERE sl.syncStatus = 'SUCCESS' AND sl.startTime >= :since")
    Double getAverageSyncDuration(@Param("since") LocalDateTime since);

    /**
     * 은행별 평균 동기화 시간 조회
     */
    @Query("SELECT sl.bankCode, AVG(sl.durationMs) FROM IrpSyncLog sl " +
           "WHERE sl.syncStatus = 'SUCCESS' AND sl.startTime >= :since GROUP BY sl.bankCode ORDER BY AVG(sl.durationMs)")
    List<Object[]> getAverageSyncDurationByBank(@Param("since") LocalDateTime since);

    /**
     * 최근 동기화 실패 원인 분석
     */
    @Query("SELECT sl.errorMessage, COUNT(sl) FROM IrpSyncLog sl " +
           "WHERE sl.syncStatus = 'FAILED' AND sl.startTime >= :since " +
           "GROUP BY sl.errorMessage ORDER BY COUNT(sl) DESC")
    List<Object[]> getSyncFailureAnalysis(@Param("since") LocalDateTime since);

    /**
     * 특정 기간 동안의 동기화 처리량 조회
     */
    @Query("SELECT SUM(sl.recordsProcessed), SUM(sl.recordsSuccess), SUM(sl.recordsFailed) FROM IrpSyncLog sl " +
           "WHERE sl.startTime BETWEEN :startTime AND :endTime")
    Object[] getSyncVolumeStatistics(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 특정 은행의 특정 기간 동기화 처리량 조회
     */
    @Query("SELECT SUM(sl.recordsProcessed), SUM(sl.recordsSuccess), SUM(sl.recordsFailed) FROM IrpSyncLog sl " +
           "WHERE sl.bankCode = :bankCode AND sl.startTime BETWEEN :startTime AND :endTime")
    Object[] getSyncVolumeStatisticsByBank(@Param("bankCode") String bankCode,
                                         @Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 마지막 동기화 체크포인트 조회
     */
    @Query("SELECT MAX(sl.lastSyncCheckpoint) FROM IrpSyncLog sl WHERE sl.bankCode = :bankCode AND sl.syncStatus = 'SUCCESS'")
    String getLastSyncCheckpoint(@Param("bankCode") String bankCode);

    /**
     * 특정 은행의 마지막 성공 동기화 조회
     */
    Optional<IrpSyncLog> findFirstByBankCodeAndSyncStatusOrderByEndTimeDesc(String bankCode, String syncStatus);

    /**
     * 오래된 완료된 동기화 로그 삭제를 위한 조회
     */
    @Query("SELECT sl FROM IrpSyncLog sl WHERE sl.syncStatus IN ('SUCCESS', 'FAILED') AND sl.startTime < :cutoffDate")
    List<IrpSyncLog> findOldCompletedSyncs(@Param("cutoffDate") LocalDateTime cutoffDate);
}
