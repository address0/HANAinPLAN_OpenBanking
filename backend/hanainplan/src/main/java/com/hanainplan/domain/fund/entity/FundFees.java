package com.hanainplan.domain.fund.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 펀드 수수료 정보 (하나인플랜)
 * - 하나은행 FundFees와 동일한 구조
 */
@Entity
@Table(name = "fund_fees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundFees {

    @Id
    @Column(name = "child_fund_cd", length = 16)
    private String childFundCd;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_fund_cd", insertable = false, updatable = false)
    private FundClass fundClass;

    @Column(name = "mgmt_fee_bps")
    private Integer mgmtFeeBps; // 운용보수 (bps, 1bps = 0.01%)

    @Column(name = "sales_fee_bps")
    private Integer salesFeeBps; // 판매보수 (bps)

    @Column(name = "trustee_fee_bps")
    private Integer trusteeFeeBps; // 수탁보수 (bps)

    @Column(name = "admin_fee_bps")
    private Integer adminFeeBps; // 사무관리보수 (bps)

    @Column(name = "front_load_pct", precision = 5, scale = 2)
    private BigDecimal frontLoadPct; // 선취수수료율 (%)

    @Column(name = "total_fee_bps")
    private Integer totalFeeBps; // 총보수 (bps)

    /**
     * 운용보수 % 반환
     */
    public BigDecimal getMgmtFeePercent() {
        return mgmtFeeBps != null 
            ? BigDecimal.valueOf(mgmtFeeBps).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
    }

    /**
     * 판매보수 % 반환
     */
    public BigDecimal getSalesFeePercent() {
        return salesFeeBps != null
            ? BigDecimal.valueOf(salesFeeBps).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
    }

    /**
     * 수탁보수 % 반환
     */
    public BigDecimal getTrusteeFeePercent() {
        return trusteeFeeBps != null
            ? BigDecimal.valueOf(trusteeFeeBps).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
    }

    /**
     * 사무관리보수 % 반환
     */
    public BigDecimal getAdminFeePercent() {
        return adminFeeBps != null
            ? BigDecimal.valueOf(adminFeeBps).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
    }

    /**
     * 총보수 % 반환
     */
    public BigDecimal getTotalFeePercent() {
        return totalFeeBps != null
            ? BigDecimal.valueOf(totalFeeBps).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
    }
}

