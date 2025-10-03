package com.hanainplan.hana.fund.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 하나은행 펀드 상품 엔티티
 */
@Entity
@Table(name = "hana_fund_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundProduct {

    @Id
    @Column(name = "fund_code", length = 20)
    private String fundCode; // 펀드 코드

    @Column(name = "fund_name", nullable = false, length = 100)
    private String fundName; // 펀드명

    @Column(name = "fund_type", nullable = false, length = 50)
    private String fundType; // 펀드 유형 (주식형, 채권형, 혼합형, MMF 등)

    @Column(name = "investment_region", length = 50)
    private String investmentRegion; // 투자 지역 (국내, 해외, 글로벌)

    @Column(name = "risk_level", nullable = false, length = 20)
    private String riskLevel; // 위험등급 (1:매우높음, 2:높음, 3:보통, 4:낮음, 5:매우낮음)

    // 수수료 정보
    @Column(name = "sales_fee_rate", precision = 5, scale = 4)
    private BigDecimal salesFeeRate; // 선취판매수수료율 (%)

    @Column(name = "management_fee_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal managementFeeRate; // 운용보수율 (연 %)

    @Column(name = "trust_fee_rate", precision = 5, scale = 4)
    private BigDecimal trustFeeRate; // 수탁보수율 (연 %)

    @Column(name = "total_expense_ratio", precision = 5, scale = 4)
    private BigDecimal totalExpenseRatio; // 총보수율 (연 %)

    @Column(name = "redemption_fee_rate", precision = 5, scale = 4)
    private BigDecimal redemptionFeeRate; // 환매수수료율 (%, 90일 이내 환매 시)

    // 수익률 정보
    @Column(name = "return_1month", precision = 10, scale = 4)
    private BigDecimal return1month; // 1개월 수익률 (%)

    @Column(name = "return_3month", precision = 10, scale = 4)
    private BigDecimal return3month; // 3개월 수익률 (%)

    @Column(name = "return_6month", precision = 10, scale = 4)
    private BigDecimal return6month; // 6개월 수익률 (%)

    @Column(name = "return_1year", precision = 10, scale = 4)
    private BigDecimal return1year; // 1년 수익률 (%)

    @Column(name = "return_3year", precision = 10, scale = 4)
    private BigDecimal return3year; // 3년 수익률 (연평균, %)

    // 기타 정보
    @Column(name = "management_company", nullable = false, length = 100)
    @Builder.Default
    private String managementCompany = "하나자산운용"; // 운용사

    @Column(name = "trust_company", length = 100)
    @Builder.Default
    private String trustCompany = "하나은행"; // 수탁사

    @Column(name = "min_investment_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal minInvestmentAmount = BigDecimal.valueOf(10000); // 최소 가입금액

    @Column(name = "is_irp_eligible")
    @Builder.Default
    private Boolean isIrpEligible = true; // IRP 편입 가능 여부

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // 상품 설명

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

    /**
     * 총 보수율 계산
     */
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

    /**
     * 위험등급 숫자 반환
     */
    public int getRiskLevelNumber() {
        try {
            return Integer.parseInt(riskLevel);
        } catch (NumberFormatException e) {
            return 3; // 기본값: 보통
        }
    }

    /**
     * 위험도 높은 상품 여부
     */
    public boolean isHighRisk() {
        return getRiskLevelNumber() <= 2;
    }
}

