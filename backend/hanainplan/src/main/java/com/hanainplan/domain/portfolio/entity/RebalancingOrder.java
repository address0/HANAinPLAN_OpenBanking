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
@Table(name = "tb_rebalancing_order",
       indexes = {
           @Index(name = "idx_job_id", columnList = "job_id"),
           @Index(name = "idx_order_type", columnList = "order_type"),
           @Index(name = "idx_asset_type", columnList = "asset_type"),
           @Index(name = "idx_fund_code", columnList = "fund_code"),
           @Index(name = "idx_status", columnList = "status"),
           @Index(name = "idx_created_at", columnList = "created_at")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RebalancingOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 10)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false, length = 20)
    private AssetType assetType;

    @Column(name = "fund_code", length = 20)
    private String fundCode;

    @Column(name = "fund_name", length = 100)
    private String fundName;

    @Column(name = "class_code", length = 8)
    private String classCode;

    @Column(name = "expected_nav", precision = 15, scale = 4)
    private BigDecimal expectedNav;

    @Column(name = "filled_nav", precision = 15, scale = 4)
    private BigDecimal filledNav;

    @Column(name = "order_units", precision = 15, scale = 6)
    private BigDecimal orderUnits;

    @Column(name = "filled_units", precision = 15, scale = 6)
    private BigDecimal filledUnits;

    @Column(name = "order_amount", precision = 15, scale = 2)
    private BigDecimal orderAmount;

    @Column(name = "filled_amount", precision = 15, scale = 2)
    private BigDecimal filledAmount;

    @Column(name = "fee", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal fee = BigDecimal.ZERO;

    @Column(name = "fee_type", length = 20)
    private String feeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "bank_order_id", length = 50)
    private String bankOrderId; // 하나은행 주문 ID

    @Column(name = "execution_reason", length = 200)
    private String executionReason;

    @Column(name = "filled_at")
    private LocalDateTime filledAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "failure_reason", length = 200)
    private String failureReason;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum OrderType {
        BUY("매수"),
        SELL("매도");

        private final String description;

        OrderType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum AssetType {
        FUND("펀드"),
        DEPOSIT("예금"),
        CASH("현금");

        private final String description;

        AssetType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum OrderStatus {
        PENDING("대기"),
        SUBMITTED("제출"),
        FILLED("체결"),
        PARTIAL_FILLED("부분체결"),
        FAILED("실패"),
        CANCELLED("취소");

        private final String description;

        OrderStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public void submit(String bankOrderId) {
        this.status = OrderStatus.SUBMITTED;
        this.bankOrderId = bankOrderId;
    }

    public void fill(BigDecimal filledNav, BigDecimal filledUnits, BigDecimal filledAmount) {
        this.status = OrderStatus.FILLED;
        this.filledNav = filledNav;
        this.filledUnits = filledUnits;
        this.filledAmount = filledAmount;
        this.filledAt = LocalDateTime.now();
    }

    public void partialFill(BigDecimal filledNav, BigDecimal filledUnits, BigDecimal filledAmount) {
        this.status = OrderStatus.PARTIAL_FILLED;
        this.filledNav = filledNav;
        this.filledUnits = filledUnits;
        this.filledAmount = filledAmount;
        this.filledAt = LocalDateTime.now();
    }

    public void fail(String reason) {
        this.status = OrderStatus.FAILED;
        this.failedAt = LocalDateTime.now();
        this.failureReason = reason;
    }

    public void cancel(String reason) {
        this.status = OrderStatus.CANCELLED;
        this.failedAt = LocalDateTime.now();
        this.failureReason = reason;
    }

    public boolean isBuy() {
        return OrderType.BUY.equals(orderType);
    }

    public boolean isSell() {
        return OrderType.SELL.equals(orderType);
    }

    public boolean isFund() {
        return AssetType.FUND.equals(assetType);
    }

    public boolean isPending() {
        return OrderStatus.PENDING.equals(status);
    }

    public boolean isSubmitted() {
        return OrderStatus.SUBMITTED.equals(status);
    }

    public boolean isFilled() {
        return OrderStatus.FILLED.equals(status);
    }

    public boolean isPartialFilled() {
        return OrderStatus.PARTIAL_FILLED.equals(status);
    }

    public boolean isFailed() {
        return OrderStatus.FAILED.equals(status);
    }

    public boolean isCancelled() {
        return OrderStatus.CANCELLED.equals(status);
    }

    public boolean isCompleted() {
        return isFilled() || isPartialFilled();
    }

    public BigDecimal getRemainingUnits() {
        if (orderUnits == null || filledUnits == null) {
            return orderUnits;
        }
        return orderUnits.subtract(filledUnits);
    }

    public BigDecimal getRemainingAmount() {
        if (orderAmount == null || filledAmount == null) {
            return orderAmount;
        }
        return orderAmount.subtract(filledAmount);
    }

    public BigDecimal getFillRate() {
        if (orderAmount == null || orderAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (filledAmount == null) {
            return BigDecimal.ZERO;
        }
        return filledAmount.divide(orderAmount, 4, BigDecimal.ROUND_HALF_UP);
    }
}
