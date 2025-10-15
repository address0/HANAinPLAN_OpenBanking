package com.hanainplan.hana.fund.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "hana_fund_subscriptions",
       indexes = {
           @Index(name = "idx_customer_ci", columnList = "customer_ci"),
           @Index(name = "idx_fund_code", columnList = "fund_code"),
           @Index(name = "idx_status", columnList = "status")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long subscriptionId;

    @Column(name = "customer_ci", nullable = false, length = 64)
    private String customerCi;

    @Column(name = "irp_account_number", length = 50)
    private String irpAccountNumber;

    @Column(name = "fund_code", nullable = false, length = 20)
    private String fundCode;

    @Column(name = "child_fund_cd", nullable = false, length = 16)
    private String childFundCd;

    @Column(name = "fund_name", nullable = false, length = 100)
    private String fundName;

    @Column(name = "class_code", length = 8)
    private String classCode;

    @Column(name = "fund_type", length = 50)
    private String fundType;

    @Column(name = "risk_level", length = 20)
    private String riskLevel;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "purchase_nav", nullable = false, precision = 15, scale = 4)
    private BigDecimal purchaseNav;

    @Column(name = "purchase_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal purchaseAmount;

    @Column(name = "purchase_fee", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal purchaseFee = BigDecimal.ZERO;

    @Column(name = "purchase_units", nullable = false, precision = 15, scale = 6)
    private BigDecimal purchaseUnits;

    @Column(name = "current_units", nullable = false, precision = 15, scale = 6)
    private BigDecimal currentUnits;

    @Column(name = "current_nav", precision = 15, scale = 4)
    private BigDecimal currentNav;

    @Column(name = "current_value", precision = 15, scale = 2)
    private BigDecimal currentValue;

    @Column(name = "total_return", precision = 15, scale = 2)
    private BigDecimal totalReturn;

    @Column(name = "return_rate", precision = 10, scale = 4)
    private BigDecimal returnRate;

    @Column(name = "accumulated_fees", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal accumulatedFees = BigDecimal.ZERO;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    @Column(name = "bank_name", length = 50)
    @Builder.Default
    private String bankName = "하나은행";

    @Column(name = "bank_code", length = 10)
    @Builder.Default
    private String bankCode = "HANA";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateValuation(BigDecimal newNav) {
        this.currentNav = newNav;

        this.currentValue = this.currentUnits
            .multiply(newNav)
            .setScale(2, RoundingMode.DOWN);

        this.totalReturn = this.currentValue.subtract(this.purchaseAmount);

        if (this.purchaseAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.returnRate = this.totalReturn
                .divide(this.purchaseAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }
    }

    public void addPurchase(BigDecimal amount, BigDecimal nav, BigDecimal fee, BigDecimal units) {
        this.purchaseAmount = this.purchaseAmount.add(amount);
        this.purchaseFee = this.purchaseFee.add(fee);
        this.purchaseUnits = this.purchaseUnits.add(units);
        this.currentUnits = this.currentUnits.add(units);
        this.accumulatedFees = this.accumulatedFees.add(fee);

        BigDecimal netAmount = this.purchaseAmount.subtract(this.purchaseFee);
        if (this.purchaseUnits.compareTo(BigDecimal.ZERO) > 0) {
            this.purchaseNav = netAmount.divide(this.purchaseUnits, 4, RoundingMode.HALF_UP);
        }
    }

    public void sellUnits(BigDecimal soldUnits) {
        this.currentUnits = this.currentUnits.subtract(soldUnits);

        if (this.currentUnits.compareTo(BigDecimal.ZERO) <= 0) {
            this.status = "SOLD";
            this.currentUnits = BigDecimal.ZERO;
        } else {
            this.status = "PARTIAL_SOLD";
        }
    }

    public boolean isActive() {
        return "ACTIVE".equals(status) || "PARTIAL_SOLD".equals(status);
    }

    public boolean isProfitable() {
        return totalReturn != null && totalReturn.compareTo(BigDecimal.ZERO) > 0;
    }
}