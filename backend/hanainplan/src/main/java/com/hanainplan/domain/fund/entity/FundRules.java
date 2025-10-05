package com.hanainplan.domain.fund.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * 펀드 거래 규칙 (하나인플랜)
 * - 하나은행 FundRules와 동일한 구조
 */
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
    private LocalTime cutoffTime; // 당일 매수/환매 마감 시각

    @Column(name = "nav_publish_time")
    private LocalTime navPublishTime; // 기준가 공시 시각

    @Column(name = "buy_settle_days")
    private Integer buySettleDays; // 매수 결제일 (T+N)

    @Column(name = "redeem_settle_days")
    private Integer redeemSettleDays; // 환매 결제일 (T+N)

    @Column(name = "unit_type", length = 16)
    private String unitType; // KRW/USD 등

    @Column(name = "min_initial_amount", precision = 15, scale = 2)
    private BigDecimal minInitialAmount; // 최초 최소 매수금액

    @Column(name = "min_additional", precision = 15, scale = 2)
    private BigDecimal minAdditional; // 추가 최소 매수금액

    @Column(name = "increment_amount", precision = 15, scale = 2)
    private BigDecimal incrementAmount; // 매수단위 (이 금액의 배수로만 매수 가능)

    @Column(name = "allow_sip")
    @Builder.Default
    private Boolean allowSip = false; // 적립식 가능 여부

    @Column(name = "allow_switch")
    @Builder.Default
    private Boolean allowSwitch = false; // 펀드 전환 가능 여부

    @Column(name = "redemption_fee_rate", precision = 7, scale = 4)
    private BigDecimal redemptionFeeRate; // 환매수수료율

    @Column(name = "redemption_fee_days")
    private Integer redemptionFeeDays; // 환매수수료 면제일 (N일 이상 보유 시 면제)

    /**
     * 환매수수료 적용 여부
     */
    public boolean hasRedemptionFee() {
        return redemptionFeeRate != null && redemptionFeeRate.compareTo(BigDecimal.ZERO) > 0;
    }
}

