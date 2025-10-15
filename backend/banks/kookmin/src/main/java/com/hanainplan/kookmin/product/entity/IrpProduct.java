package com.hanainplan.kookmin.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "kookmin_irp_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IrpProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "irp_product_id")
    private Long irpProductId;

    @Column(name = "product_code", unique = true, nullable = false, length = 20)
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "product_type", length = 50)
    private String productType;

    @Column(name = "management_company", length = 100)
    private String managementCompany;

    @Column(name = "trust_company", length = 100)
    private String trustCompany;

    @Column(name = "minimum_contribution", precision = 15, scale = 2)
    private BigDecimal minimumContribution;

    @Column(name = "maximum_contribution", precision = 15, scale = 2)
    private BigDecimal maximumContribution;

    @Column(name = "annual_contribution_limit", precision = 15, scale = 2)
    private BigDecimal annualContributionLimit;

    @Column(name = "management_fee_rate", precision = 5, scale = 4)
    private BigDecimal managementFeeRate;

    @Column(name = "trust_fee_rate", precision = 5, scale = 4)
    private BigDecimal trustFeeRate;

    @Column(name = "sales_fee_rate", precision = 5, scale = 4)
    private BigDecimal salesFeeRate;

    @Column(name = "total_fee_rate", precision = 5, scale = 4)
    private BigDecimal totalFeeRate;

    @Column(name = "investment_options", length = 500)
    private String investmentOptions;

    @Column(name = "risk_level", length = 20)
    private String riskLevel;

    @Column(name = "expected_return_rate", precision = 5, scale = 4)
    private BigDecimal expectedReturnRate;

    @Column(name = "guarantee_type", length = 50)
    private String guaranteeType;

    @Column(name = "guarantee_rate", precision = 5, scale = 4)
    private BigDecimal guaranteeRate;

    @Column(name = "maturity_age", length = 20)
    private String maturityAge;

    @Column(name = "early_withdrawal_penalty", length = 200)
    private String earlyWithdrawalPenalty;

    @Column(name = "tax_benefit", length = 300)
    private String taxBenefit;

    @Column(name = "contribution_frequency", length = 50)
    private String contributionFrequency;

    @Column(name = "contribution_method", length = 200)
    private String contributionMethod;

    @Column(name = "minimum_holding_period")
    private Integer minimumHoldingPeriod;

    @Column(name = "auto_rebalancing", length = 10)
    private String autoRebalancing;

    @Column(name = "rebalancing_frequency", length = 50)
    private String rebalancingFrequency;

    @Column(name = "performance_fee", precision = 5, scale = 4)
    private BigDecimal performanceFee;

    @Column(name = "performance_fee_threshold", precision = 5, scale = 4)
    private BigDecimal performanceFeeThreshold;

    @Column(name = "fund_allocation", length = 1000)
    private String fundAllocation;

    @Column(name = "benchmark_index", length = 100)
    private String benchmarkIndex;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "precautions", length = 1000)
    private String precautions;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

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
}