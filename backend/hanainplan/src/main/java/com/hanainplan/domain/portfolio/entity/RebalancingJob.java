package com.hanainplan.domain.portfolio.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_rebalancing_job",
       indexes = {
           @Index(name = "idx_customer_id", columnList = "customer_id"),
           @Index(name = "idx_irp_account", columnList = "irp_account_number"),
           @Index(name = "idx_job_type", columnList = "job_type"),
           @Index(name = "idx_status", columnList = "status"),
           @Index(name = "idx_created_at", columnList = "created_at")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RebalancingJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "irp_account_number", nullable = false, length = 50)
    private String irpAccountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 20)
    private JobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 20)
    private TriggerType triggerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private JobStatus status = JobStatus.PENDING;

    @Column(name = "current_portfolio", columnDefinition = "TEXT")
    private String currentPortfolio; // JSON

    @Column(name = "target_portfolio", columnDefinition = "TEXT")
    private String targetPortfolio; // JSON

    @Column(name = "rebalancing_plan", columnDefinition = "TEXT")
    private String rebalancingPlan; // JSON: 주문 목록

    @Column(name = "total_fee", precision = 15, scale = 2)
    private BigDecimal totalFee;

    @Column(name = "drift_threshold", precision = 5, scale = 2)
    private BigDecimal driftThreshold; // 밴드형 트리거 임계값

    @Column(name = "execution_reason", length = 200)
    private String executionReason;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", length = 200)
    private String cancellationReason;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum JobType {
        SIMULATION("시뮬레이션"),
        EXECUTION("실행");

        private final String description;

        JobType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum TriggerType {
        PERIODIC("주기형"),
        MANUAL("수동"),
        THRESHOLD("밴드형"),
        TIME_BASED("시간기반"),
        BAND_BASED("밴드기반"),
        TEST("테스트");

        private final String description;

        TriggerType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum JobStatus {
        PENDING("대기"),
        APPROVED("승인"),
        EXECUTING("실행중"),
        COMPLETED("완료"),
        CANCELLED("취소"),
        FAILED("실패");

        private final String description;

        JobStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public void approve() {
        this.status = JobStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
    }

    public void startExecution() {
        this.status = JobStatus.EXECUTING;
        this.executedAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = JobStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        this.status = JobStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }

    public void fail(String reason) {
        this.status = JobStatus.FAILED;
        this.cancelledAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }

    public boolean isSimulation() {
        return JobType.SIMULATION.equals(jobType);
    }

    public boolean isExecution() {
        return JobType.EXECUTION.equals(jobType);
    }

    public boolean isPending() {
        return JobStatus.PENDING.equals(status);
    }

    public boolean isApproved() {
        return JobStatus.APPROVED.equals(status);
    }

    public boolean isExecuting() {
        return JobStatus.EXECUTING.equals(status);
    }

    public boolean isCompleted() {
        return JobStatus.COMPLETED.equals(status);
    }

    public boolean isCancelled() {
        return JobStatus.CANCELLED.equals(status);
    }

    public boolean isFailed() {
        return JobStatus.FAILED.equals(status);
    }

    public boolean canBeApproved() {
        return isSimulation() && isPending();
    }

    public boolean canBeExecuted() {
        return isExecution() && isApproved();
    }
}
