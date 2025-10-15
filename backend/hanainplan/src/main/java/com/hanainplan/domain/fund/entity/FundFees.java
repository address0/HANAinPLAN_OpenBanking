package com.hanainplan.domain.fund.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
    private Integer mgmtFeeBps;

    @Column(name = "sales_fee_bps")
    private Integer salesFeeBps;

    @Column(name = "trustee_fee_bps")
    private Integer trusteeFeeBps;

    @Column(name = "admin_fee_bps")
    private Integer adminFeeBps;

    @Column(name = "front_load_pct", precision = 5, scale = 2)
    private BigDecimal frontLoadPct;

    @Column(name = "total_fee_bps")
    private Integer totalFeeBps;

    public BigDecimal getMgmtFeePercent() {
        return mgmtFeeBps != null 
            ? BigDecimal.valueOf(mgmtFeeBps).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
    }

    public BigDecimal getSalesFeePercent() {
        return salesFeeBps != null
            ? BigDecimal.valueOf(salesFeeBps).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
    }

    public BigDecimal getTrusteeFeePercent() {
        return trusteeFeeBps != null
            ? BigDecimal.valueOf(trusteeFeeBps).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
    }

    public BigDecimal getAdminFeePercent() {
        return adminFeeBps != null
            ? BigDecimal.valueOf(adminFeeBps).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
    }

    public BigDecimal getTotalFeePercent() {
        return totalFeeBps != null
            ? BigDecimal.valueOf(totalFeeBps).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
    }
}