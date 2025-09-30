package com.hanainplan.domain.banking.entity;

import com.hanainplan.domain.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * IRP 동기화 이력 엔티티
 * - 은행별 IRP 데이터 동기화 이력을 관리
 */
@Entity
@Table(name = "tb_irp_sync_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IrpSyncLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sync_log_id")
    private Long syncLogId;

    @Column(name = "sync_type", nullable = false, length = 30)
    private String syncType; // FULL, INCREMENTAL, ON_DEMAND, SCHEDULED

    @Column(name = "bank_code", nullable = false, length = 10)
    private String bankCode; // 동기화 대상 은행 (HANA, KOOKMIN, SHINHAN)

    @Column(name = "customer_id")
    private Long customerId; // 특정 고객 ID (전체 동기화 시 null)

    @Column(name = "customer_ci", length = 64)
    private String customerCi; // 특정 고객 CI (전체 동기화 시 null)

    @Column(name = "sync_target", nullable = false, length = 30)
    private String syncTarget; // ACCOUNT, PRODUCT, TRANSACTION, ALL

    @Column(name = "sync_status", nullable = false, length = 20)
    @Builder.Default
    private String syncStatus = "PENDING"; // PENDING, RUNNING, SUCCESS, FAILED, PARTIAL

    @Column(name = "start_time", nullable = false)
    @Builder.Default
    private LocalDateTime startTime = LocalDateTime.now();

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_ms")
    private Long durationMs; // 동기화 소요 시간 (밀리초)

    @Column(name = "records_processed")
    @Builder.Default
    private Integer recordsProcessed = 0; // 처리된 레코드 수

    @Column(name = "records_success")
    @Builder.Default
    private Integer recordsSuccess = 0; // 성공한 레코드 수

    @Column(name = "records_failed")
    @Builder.Default
    private Integer recordsFailed = 0; // 실패한 레코드 수

    @Column(name = "error_message", length = 1000)
    private String errorMessage; // 오류 메시지

    @Column(name = "error_details", length = 2000)
    private String errorDetails; // 상세 오류 정보 (JSON)

    @Column(name = "sync_config", length = 1000)
    private String syncConfig; // 동기화 설정 (JSON)

    @Column(name = "trigger_source", length = 50)
    private String triggerSource; // 동기화 트리거 소스 (SCHEDULED, API, EVENT)

    @Column(name = "last_sync_checkpoint", length = 100)
    private String lastSyncCheckpoint; // 마지막 동기화 체크포인트

    @Column(name = "next_sync_scheduled")
    private LocalDateTime nextSyncScheduled; // 다음 동기화 예정 시간

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0; // 재시도 횟수

    @Column(name = "max_retry_count")
    @Builder.Default
    private Integer maxRetryCount = 3; // 최대 재시도 횟수

    @Column(name = "is_manual")
    @Builder.Default
    private Boolean isManual = false; // 수동 동기화 여부

    /**
     * 동기화 시작 처리
     */
    public void startSync() {
        this.syncStatus = "RUNNING";
        this.startTime = LocalDateTime.now();
        this.recordsProcessed = 0;
        this.recordsSuccess = 0;
        this.recordsFailed = 0;
        this.errorMessage = null;
        this.errorDetails = null;
    }

    /**
     * 동기화 완료 처리
     */
    public void completeSync() {
        this.syncStatus = "SUCCESS";
        this.endTime = LocalDateTime.now();
        this.durationMs = java.time.Duration.between(startTime, endTime).toMillis();
    }

    /**
     * 동기화 실패 처리
     */
    public void failSync(String errorMessage, String errorDetails) {
        this.syncStatus = "FAILED";
        this.endTime = LocalDateTime.now();
        this.durationMs = java.time.Duration.between(startTime, endTime).toMillis();
        this.errorMessage = errorMessage;
        this.errorDetails = errorDetails;
    }

    /**
     * 부분 성공 처리
     */
    public void partialSync() {
        this.syncStatus = "PARTIAL";
        this.endTime = LocalDateTime.now();
        this.durationMs = java.time.Duration.between(startTime, endTime).toMillis();
    }

    /**
     * 레코드 처리 수 증가
     */
    public void incrementProcessed() {
        this.recordsProcessed++;
    }

    /**
     * 성공 레코드 수 증가
     */
    public void incrementSuccess() {
        this.recordsSuccess++;
    }

    /**
     * 실패 레코드 수 증가
     */
    public void incrementFailed() {
        this.recordsFailed++;
    }

    /**
     * 재시도 가능 여부 확인
     */
    public boolean canRetry() {
        return retryCount < maxRetryCount;
    }

    /**
     * 재시도 횟수 증가
     */
    public void incrementRetry() {
        this.retryCount++;
    }

    /**
     * 성공률 계산
     */
    public double getSuccessRate() {
        if (recordsProcessed == 0) return 0.0;
        return (double) recordsSuccess / recordsProcessed * 100.0;
    }

    /**
     * 동기화 실행 시간 계산
     */
    public long getExecutionTimeMs() {
        if (startTime == null) return 0;
        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        return java.time.Duration.between(startTime, end).toMillis();
    }

    /**
     * 동기화 요약 정보 생성
     */
    public String getSyncSummary() {
        return String.format("처리: %d건, 성공: %d건, 실패: %d건 (%.1f%%)",
            recordsProcessed, recordsSuccess, recordsFailed, getSuccessRate());
    }
}
