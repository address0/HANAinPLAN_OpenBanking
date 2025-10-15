package com.hanainplan.domain.fund.entity;

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
    @JoinColumn(name = "child_fund_cd", insertable = false, updatable = false)
    private FundClass fundClass;

    @Column(name = "cutoff_time")
    private LocalTime cutoffTime;

    @Column(name = "nav_publish_time")
    private LocalTime navPublishTime;

    @Column(name = "buy_settle_days")
    private Integer buySettleDays;

    @Column(name = "redeem_settle_days")
    private Integer redeemSettleDays;

    @Column(name = "unit_type", length = 16)
    private String unitType;

    @Column(name = "min_initial_amount", precision = 15, scale = 2)
    private BigDecimal minInitialAmount;

    @Column(name = "min_additional", precision = 15, scale = 2)
    private BigDecimal minAdditional;

    @Column(name = "increment_amount", precision = 15, scale = 2)
    private BigDecimal incrementAmount;

    @Column(name = "allow_sip")
    @Builder.Default
    private Boolean allowSip = false;

    @Column(name = "allow_switch")
    @Builder.Default
    private Boolean allowSwitch = false;

    @Column(name = "redemption_fee_rate", precision = 7, scale = 4)
    private BigDecimal redemptionFeeRate;

    @Column(name = "redemption_fee_days")
    private Integer redemptionFeeDays;

    public boolean hasRedemptionFee() {
        return redemptionFeeRate != null && redemptionFeeRate.compareTo(BigDecimal.ZERO) > 0;
    }
}