package com.hanainplan.hana.fund.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 펀드 보수/수수료 (클래스 기준)
 * - bp (basis point) 단위: 1 bp = 0.01%
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

    // 연관관계: 수수료 1 : 1 클래스
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "child_fund_cd")
    private FundClass fundClass;

    @Column(name = "mgmt_fee_bps")
    private Integer mgmtFeeBps; // 운용보수 (bp) 예: 45 -> 0.45%

    @Column(name = "sales_fee_bps")
    private Integer salesFeeBps; // 판매보수 (bp)

    @Column(name = "trustee_fee_bps")
    private Integer trusteeFeeBps; // 수탁보수 (bp)

    @Column(name = "admin_fee_bps")
    private Integer adminFeeBps; // 사무관리보수 (bp)

    @Column(name = "front_load_pct", precision = 6, scale = 4)
    private BigDecimal frontLoadPct; // 선취판매 수수료율 (%)

    @Column(name = "total_fee_bps")
    private Integer totalFeeBps; // 총보수 (bp)

    /**
     * bp를 % 비율로 변환
     */
    public BigDecimal bpsToPercent(Integer bps) {
        if (bps == null) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(bps)
                .divide(BigDecimal.valueOf(10000), 4, RoundingMode.HALF_UP);
    }

    /**
     * 운용보수 % 반환
     */
    public BigDecimal getMgmtFeePercent() {
        return bpsToPercent(mgmtFeeBps);
    }

    /**
     * 판매보수 % 반환
     */
    public BigDecimal getSalesFeePercent() {
        return bpsToPercent(salesFeeBps);
    }

    /**
     * 수탁보수 % 반환
     */
    public BigDecimal getTrusteeFeePercent() {
        return bpsToPercent(trusteeFeeBps);
    }

    /**
     * 사무관리보수 % 반환
     */
    public BigDecimal getAdminFeePercent() {
        return bpsToPercent(adminFeeBps);
    }

    /**
     * 총보수 % 반환
     */
    public BigDecimal getTotalFeePercent() {
        return bpsToPercent(totalFeeBps);
    }

    /**
     * 총보수 계산 (자동)
     */
    public void calculateTotalFee() {
        int total = 0;
        if (mgmtFeeBps != null) total += mgmtFeeBps;
        if (salesFeeBps != null) total += salesFeeBps;
        if (trusteeFeeBps != null) total += trusteeFeeBps;
        if (adminFeeBps != null) total += adminFeeBps;
        this.totalFeeBps = total;
    }

    /**
     * 보수 요약 문자열 반환
     */
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

