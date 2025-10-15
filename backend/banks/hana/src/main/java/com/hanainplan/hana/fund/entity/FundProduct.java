package com.hanainplan.hana.fund.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "hana_fund_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundProduct {

    @Id
    @Column(name = "fund_code", length = 20)
    private String fundCode;

    @Column(name = "fund_name", nullable = false, length = 100)
    private String fundName;

    @Column(name = "fund_type", nullable = false, length = 50)
    private String fundType;

    @Column(name = "investment_region", length = 50)
    private String investmentRegion;

    @Column(name = "risk_level", nullable = false, length = 20)
    private String riskLevel;

    @Column(name = "sales_fee_rate", precision = 5, scale = 4)
    private BigDecimal salesFeeRate;

    @Column(name = "management_fee_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal managementFeeRate;

    @Column(name = "trust_fee_rate", precision = 5, scale = 4)
    private BigDecimal trustFeeRate;

    @Column(name = "total_expense_ratio", precision = 5, scale = 4)
    private BigDecimal totalExpenseRatio;

    @Column(name = "redemption_fee_rate", precision = 5, scale = 4)
    private BigDecimal redemptionFeeRate;

    @Column(name = "return_1month", precision = 10, scale = 4)
    private BigDecimal return1month;

    @Column(name = "return_3month", precision = 10, scale = 4)
    private BigDecimal return3month;

    @Column(name = "return_6month", precision = 10, scale = 4)
    private BigDecimal return6month;

    @Column(name = "return_1year", precision = 10, scale = 4)
    private BigDecimal return1year;

    @Column(name = "return_3year", precision = 10, scale = 4)
    private BigDecimal return3year;

    @Column(name = "management_company", nullable = false, length = 100)
    @Builder.Default
    private String managementCompany = "하나자산운용";

    @Column(name = "trust_company", length = 100)
    @Builder.Default
    private String trustCompany = "하나은행";

    @Column(name = "min_investment_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal minInvestmentAmount = BigDecimal.valueOf(10000);

    @Column(name = "is_irp_eligible")
    @Builder.Default
    private Boolean isIrpEligible = true;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

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

    public BigDecimal calculateTotalExpenseRatio() {
        BigDecimal total = BigDecimal.ZERO;
        if (managementFeeRate != null) {
            total = total.add(managementFeeRate);
        }
        if (trustFeeRate != null) {
            total = total.add(trustFeeRate);
        }
        return total;
    }

    public int getRiskLevelNumber() {
        try {
            return Integer.parseInt(riskLevel);
        } catch (NumberFormatException e) {
            return 3;
        }
    }

    public boolean isHighRisk() {
        return getRiskLevelNumber() <= 2;
    }
}