package com.hanainplan.hana.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "hana_irp_products")
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
    private String productCode; // IRP 상품코드

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName; // IRP 상품명

    @Column(name = "product_type", length = 50)
    private String productType; // 상품유형 (DC형, DB형, 혼합형)

    @Column(name = "management_company", length = 100)
    private String managementCompany; // 운용회사

    @Column(name = "trust_company", length = 100)
    private String trustCompany; // 신탁회사

    @Column(name = "minimum_contribution", precision = 15, scale = 2)
    private BigDecimal minimumContribution; // 최소 납입금액

    @Column(name = "maximum_contribution", precision = 15, scale = 2)
    private BigDecimal maximumContribution; // 최대 납입금액

    @Column(name = "annual_contribution_limit", precision = 15, scale = 2)
    private BigDecimal annualContributionLimit; // 연간 납입한도

    @Column(name = "management_fee_rate", precision = 5, scale = 4)
    private BigDecimal managementFeeRate; // 운용수수료율

    @Column(name = "trust_fee_rate", precision = 5, scale = 4)
    private BigDecimal trustFeeRate; // 신탁수수료율

    @Column(name = "sales_fee_rate", precision = 5, scale = 4)
    private BigDecimal salesFeeRate; // 판매수수료율

    @Column(name = "total_fee_rate", precision = 5, scale = 4)
    private BigDecimal totalFeeRate; // 총 수수료율

    @Column(name = "investment_options", length = 500)
    private String investmentOptions; // 투자옵션 (안정형, 성장형, 균형형 등)

    @Column(name = "risk_level", length = 20)
    private String riskLevel; // 위험등급 (1~5단계)

    @Column(name = "expected_return_rate", precision = 5, scale = 4)
    private BigDecimal expectedReturnRate; // 예상수익률

    @Column(name = "guarantee_type", length = 50)
    private String guaranteeType; // 보장유형 (원금보장, 수익보장, 무보장)

    @Column(name = "guarantee_rate", precision = 5, scale = 4)
    private BigDecimal guaranteeRate; // 보장수익률

    @Column(name = "maturity_age", length = 20)
    private String maturityAge; // 만기연령 (55세, 60세, 65세 등)

    @Column(name = "early_withdrawal_penalty", length = 200)
    private String earlyWithdrawalPenalty; // 조기인출시 불이익

    @Column(name = "tax_benefit", length = 300)
    private String taxBenefit; // 세제혜택 (소득공제, 비과세 등)

    @Column(name = "contribution_frequency", length = 50)
    private String contributionFrequency; // 납입주기 (월납, 분기납, 연납)

    @Column(name = "contribution_method", length = 200)
    private String contributionMethod; // 납입방법 (자동이체, 수동납입)

    @Column(name = "minimum_holding_period")
    private Integer minimumHoldingPeriod; // 최소 보유기간 (개월)

    @Column(name = "auto_rebalancing", length = 10)
    private String autoRebalancing; // 자동 리밸런싱 여부 (Y/N)

    @Column(name = "rebalancing_frequency", length = 50)
    private String rebalancingFrequency; // 리밸런싱 주기

    @Column(name = "performance_fee", precision = 5, scale = 4)
    private BigDecimal performanceFee; // 성과수수료율

    @Column(name = "performance_fee_threshold", precision = 5, scale = 4)
    private BigDecimal performanceFeeThreshold; // 성과수수료 기준수익률

    @Column(name = "fund_allocation", length = 1000)
    private String fundAllocation; // 펀드 배분 비율 (JSON 형태)

    @Column(name = "benchmark_index", length = 100)
    private String benchmarkIndex; // 벤치마크 지수

    @Column(name = "description", length = 1000)
    private String description; // 상품설명

    @Column(name = "precautions", length = 1000)
    private String precautions; // 유의사항

    @Column(name = "is_active")
    private Boolean isActive; // 활성화여부

    @Column(name = "start_date")
    private LocalDate startDate; // 판매시작일

    @Column(name = "end_date")
    private LocalDate endDate; // 판매종료일

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
