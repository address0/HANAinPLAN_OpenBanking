package com.hanainplan.hana.fund.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * 펀드 거래/운영 규칙
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

    // 연관관계: 규칙 1 : 1 클래스
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "child_fund_cd")
    private FundClass fundClass;

    @Column(name = "cutoff_time")
    private LocalTime cutoffTime; // 주문 컷오프 (영업일 기준)

    @Column(name = "nav_publish_time")
    private LocalTime navPublishTime; // 기준가 반영 시각

    @Column(name = "buy_settle_days")
    private Integer buySettleDays; // 매수 결제 T+N

    @Column(name = "redeem_settle_days")
    private Integer redeemSettleDays; // 환매 대금 지급 T+N

    @Column(name = "unit_type", length = 8, nullable = false)
    @Builder.Default
    private String unitType = "KRW"; // KRW/UNITS

    @Column(name = "min_initial_amount", precision = 18, scale = 2)
    private BigDecimal minInitialAmount; // 최소 최초 투자금액

    @Column(name = "min_additional", precision = 18, scale = 2)
    private BigDecimal minAdditional; // 최소 추가 투자금액

    @Column(name = "increment_amount", precision = 18, scale = 2)
    private BigDecimal incrementAmount; // 투자 증가 단위

    @Column(name = "allow_sip", nullable = false)
    @Builder.Default
    private Boolean allowSip = false; // 정기투자 허용 여부

    @Column(name = "allow_switch", nullable = false)
    @Builder.Default
    private Boolean allowSwitch = false; // 펀드 전환 허용 여부

    @Column(name = "redemption_fee_rate", precision = 6, scale = 4)
    private BigDecimal redemptionFeeRate; // 환매수수료율 (후취)

    @Column(name = "redemption_fee_days")
    private Integer redemptionFeeDays; // 환매수수료 적용기간 (일)

    /**
     * 컷오프 시간 문자열 반환 (예: "15:30")
     */
    public String getCutoffTimeString() {
        return cutoffTime != null ? cutoffTime.toString() : "미정";
    }

    /**
     * 환매수수료 적용 여부 확인
     */
    public boolean hasRedemptionFee() {
        return redemptionFeeRate != null && 
               redemptionFeeRate.compareTo(BigDecimal.ZERO) > 0 &&
               redemptionFeeDays != null && 
               redemptionFeeDays > 0;
    }

    /**
     * 결제일 설명 반환
     */
    public String getSettlementDescription() {
        String buy = buySettleDays != null ? "T+" + buySettleDays : "미정";
        String redeem = redeemSettleDays != null ? "T+" + redeemSettleDays : "미정";
        return String.format("매수: %s, 환매: %s", buy, redeem);
    }
}

