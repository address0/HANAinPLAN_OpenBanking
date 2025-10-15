package com.hanainplan.hana.fund.entity;

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
    @MapsId
    @JoinColumn(name = "child_fund_cd")
    private FundClass fundClass;

    @Column(name = "mgmt_fee_bps")
    private Integer mgmtFeeBps;

    @Column(name = "sales_fee_bps")
    private Integer salesFeeBps;

    @Column(name = "trustee_fee_bps")
    private Integer trusteeFeeBps;

    @Column(name = "admin_fee_bps")
    private Integer adminFeeBps;

    @Column(name = "front_load_pct", precision = 6, scale = 4)
    private BigDecimal frontLoadPct;

    @Column(name = "total_fee_bps")
    private Integer totalFeeBps;

    public BigDecimal bpsToPercent(Integer bps) {
        if (bps == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(bps)
                .divide(BigDecimal.valueOf(10000), 4, RoundingMode.HALF_UP);
    }

    public BigDecimal getMgmtFeePercent() {
        return bpsToPercent(mgmtFeeBps);
    }

    public BigDecimal getSalesFeePercent() {
        return bpsToPercent(salesFeeBps);
    }

    public BigDecimal getTrusteeFeePercent() {
        return bpsToPercent(trusteeFeeBps);
    }

    public BigDecimal getAdminFeePercent() {
        return bpsToPercent(adminFeeBps);
    }

    public BigDecimal getTotalFeePercent() {
        return bpsToPercent(totalFeeBps);
    }

    public void calculateTotalFee() {
        int total = 0;
        if (mgmtFeeBps != null) total += mgmtFeeBps;
        if (salesFeeBps != null) total += salesFeeBps;
        if (trusteeFeeBps != null) total += trusteeFeeBps;
        if (adminFeeBps != null) total += adminFeeBps;
        this.totalFeeBps = total;
    }

    public String getFeeSummary() {
        return String.format("총보수: %.2f%% (운용 %.2f%% + 판매 %.2f%% + 수탁 %.2f%% + 사무 %.2f%%)",
                getTotalFeePercent(),
                getMgmtFeePercent(),
                getSalesFeePercent(),
                getTrusteeFeePercent(),
                getAdminFeePercent());
    }

    @PrePersist
    @PreUpdate
    protected void onSave() {
        calculateTotalFee();
    }
}