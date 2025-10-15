package com.hanainplan.hana.fund.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "fund_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundRules {

    @Id
    @Column(name = "child_fund_cd", length = 16)
    private String childFundCd;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "child_fund_cd")
    private FundClass fundClass;

    @Column(name = "cutoff_time")
    private LocalTime cutoffTime;

    @Column(name = "nav_publish_time")
    private LocalTime navPublishTime;

    @Column(name = "buy_settle_days")
    private Integer buySettleDays;

    @Column(name = "redeem_settle_days")
    private Integer redeemSettleDays;

    @Column(name = "unit_type", length = 8, nullable = false)
    @Builder.Default
    private String unitType = "KRW";

    @Column(name = "min_initial_amount", precision = 18, scale = 2)
    private BigDecimal minInitialAmount;

    @Column(name = "min_additional", precision = 18, scale = 2)
    private BigDecimal minAdditional;

    @Column(name = "increment_amount", precision = 18, scale = 2)
    private BigDecimal incrementAmount;

    @Column(name = "allow_sip", nullable = false)
    @Builder.Default
    private Boolean allowSip = false;

    @Column(name = "allow_switch", nullable = false)
    @Builder.Default
    private Boolean allowSwitch = false;

    @Column(name = "redemption_fee_rate", precision = 6, scale = 4)
    private BigDecimal redemptionFeeRate;

    @Column(name = "redemption_fee_days")
    private Integer redemptionFeeDays;

    public String getCutoffTimeString() {
        return cutoffTime != null ? cutoffTime.toString() : "미정";
    }

    public boolean hasRedemptionFee() {
        return redemptionFeeRate != null && 
               redemptionFeeRate.compareTo(BigDecimal.ZERO) > 0 &&
               redemptionFeeDays != null && 
               redemptionFeeDays > 0;
    }

    public String getSettlementDescription() {
        String buy = buySettleDays != null ? "T+" + buySettleDays : "미정";
        String redeem = redeemSettleDays != null ? "T+" + redeemSettleDays : "미정";
        return String.format("매수: %s, 환매: %s", buy, redeem);
    }
}