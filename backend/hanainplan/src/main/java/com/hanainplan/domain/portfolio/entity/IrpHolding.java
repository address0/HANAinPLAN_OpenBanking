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
@Table(name = "tb_irp_holding",
       indexes = {
           @Index(name = "idx_customer_id", columnList = "customer_id"),
           @Index(name = "idx_irp_account", columnList = "irp_account_number"),
           @Index(name = "idx_asset_type", columnList = "asset_type"),
           @Index(name = "idx_asset_code", columnList = "asset_code"),
           @Index(name = "idx_bank_code", columnList = "bank_code")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class IrpHolding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holding_id")
    private Long holdingId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "irp_account_number", nullable = false, length = 50)
    private String irpAccountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false, length = 20)
    private AssetType assetType;

    @Column(name = "asset_code", length = 20)
    private String assetCode; // 상품코드 (펀드/예금), 현금은 null

    @Column(name = "asset_name", length = 100)
    private String assetName;

    @Column(name = "units", precision = 15, scale = 6)
    private BigDecimal units; // 펀드 좌수, 예금/현금은 null

    @Column(name = "purchase_amount", precision = 15, scale = 2)
    private BigDecimal purchaseAmount; // 취득가

    @Column(name = "current_value", precision = 15, scale = 2)
    private BigDecimal currentValue; // 평가금액

    @Column(name = "current_nav", precision = 15, scale = 4)
    private BigDecimal currentNav; // 펀드 현재 NAV

    @Column(name = "purchase_nav", precision = 15, scale = 4)
    private BigDecimal purchaseNav; // 펀드 매수 NAV

    @Column(name = "total_return", precision = 15, scale = 2)
    private BigDecimal totalReturn; // 평가손익

    @Column(name = "return_rate", precision = 10, scale = 4)
    private BigDecimal returnRate; // 수익률 (%)

    @Column(name = "bank_code", length = 10)
    @Builder.Default
    private String bankCode = "HANA";

    @Column(name = "bank_name", length = 50)
    @Builder.Default
    private String bankName = "하나은행";

    @Column(name = "maturity_date")
    private LocalDateTime maturityDate; // 예금 만기일

    @Column(name = "interest_rate", precision = 5, scale = 4)
    private BigDecimal interestRate; // 예금 금리

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum AssetType {
        CASH("현금"),
        DEPOSIT("정기예금"),
        FUND("펀드");

        private final String description;

        AssetType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public void updateValuation(BigDecimal newNav) {
        if (assetType == AssetType.FUND && newNav != null && units != null) {
            this.currentNav = newNav;
            this.currentValue = units.multiply(newNav).setScale(2, BigDecimal.ROUND_DOWN);
            
            if (purchaseAmount != null && purchaseAmount.compareTo(BigDecimal.ZERO) > 0) {
                this.totalReturn = currentValue.subtract(purchaseAmount);
                this.returnRate = totalReturn
                    .divide(purchaseAmount, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            }
        }
    }

    public void updateCashAmount(BigDecimal amount) {
        if (assetType == AssetType.CASH) {
            this.currentValue = amount;
            this.purchaseAmount = amount; // 현금은 취득가 = 현재가
            this.totalReturn = BigDecimal.ZERO;
            this.returnRate = BigDecimal.ZERO;
        }
    }

    public void updateDepositAmount(BigDecimal amount, BigDecimal rate, LocalDateTime maturity) {
        if (assetType == AssetType.DEPOSIT) {
            this.currentValue = amount;
            this.purchaseAmount = amount; // 예금은 원금 기준
            this.interestRate = rate;
            this.maturityDate = maturity;
            this.totalReturn = BigDecimal.ZERO; // 만기까지는 손익 없음
            this.returnRate = BigDecimal.ZERO;
        }
    }

    public boolean isCash() {
        return AssetType.CASH.equals(assetType);
    }

    public boolean isDeposit() {
        return AssetType.DEPOSIT.equals(assetType);
    }

    public boolean isFund() {
        return AssetType.FUND.equals(assetType);
    }

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public boolean isMatured() {
        return maturityDate != null && LocalDateTime.now().isAfter(maturityDate);
    }
}
