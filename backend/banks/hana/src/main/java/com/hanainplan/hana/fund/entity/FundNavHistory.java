package com.hanainplan.hana.fund.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 하나은행 펀드 기준가 이력 엔티티
 * NAV = Net Asset Value (순자산가치, 기준가)
 */
@Entity
@Table(name = "hana_fund_nav_history", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"fund_code", "base_date"}),
       indexes = {
           @Index(name = "idx_fund_code", columnList = "fund_code"),
           @Index(name = "idx_base_date", columnList = "base_date")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundNavHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nav_id")
    private Long navId;

    @Column(name = "fund_code", nullable = false, length = 20)
    private String fundCode; // 펀드 코드

    @Column(name = "base_date", nullable = false)
    private LocalDate baseDate; // 기준일

    @Column(name = "nav", nullable = false, precision = 15, scale = 4)
    private BigDecimal nav; // 기준가 (원)

    @Column(name = "previous_nav", precision = 15, scale = 4)
    private BigDecimal previousNav; // 전일 기준가

    @Column(name = "change_amount", precision = 15, scale = 4)
    private BigDecimal changeAmount; // 전일 대비 변동액

    @Column(name = "change_rate", precision = 10, scale = 4)
    private BigDecimal changeRate; // 전일 대비 변동률 (%)

    @Column(name = "total_net_assets", precision = 20, scale = 2)
    private BigDecimal totalNetAssets; // 순자산총액 (억원)

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        
        // 변동액 및 변동률 자동 계산
        if (nav != null && previousNav != null && previousNav.compareTo(BigDecimal.ZERO) > 0) {
            // 변동액 = 현재 기준가 - 전일 기준가
            changeAmount = nav.subtract(previousNav);
            
            // 변동률 = (변동액 / 전일 기준가) * 100
            changeRate = changeAmount
                .divide(previousNav, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }
    }

    /**
     * 상승 여부 확인
     */
    public boolean isIncreased() {
        return changeAmount != null && changeAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 하락 여부 확인
     */
    public boolean isDecreased() {
        return changeAmount != null && changeAmount.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * 변동률 문자열 반환 (예: "+2.34%")
     */
    public String getChangeRateString() {
        if (changeRate == null) {
            return "0.00%";
        }
        String sign = changeRate.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
        return String.format("%s%.2f%%", sign, changeRate);
    }
}

