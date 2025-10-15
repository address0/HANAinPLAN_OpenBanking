package com.hanainplan.domain.fund.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundClassDetailDto {

    private String childFundCd;
    private String classCode;
    private String loadType;
    private String taxCategory;
    private String saleStatus;
    private String sourceUrl;

    private FundMasterDto fundMaster;

    private FundRulesDto rules;

    private FundFeesDto fees;

    private BigDecimal latestNav;
    private LocalDate latestNavDate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FundMasterDto {
        private String fundCd;
        private String fundName;
        private Integer fundGb;
        private String assetType;
        private String riskGrade;
        private String currency;
        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FundRulesDto {
        private LocalTime cutoffTime;
        private LocalTime navPublishTime;
        private Integer buySettleDays;
        private Integer redeemSettleDays;
        private String unitType;
        private BigDecimal minInitialAmount;
        private BigDecimal minAdditional;
        private BigDecimal incrementAmount;
        private Boolean allowSip;
        private Boolean allowSwitch;
        private BigDecimal redemptionFeeRate;
        private Integer redemptionFeeDays;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FundFeesDto {
        private Integer mgmtFeeBps;
        private Integer salesFeeBps;
        private Integer trusteeFeeBps;
        private Integer adminFeeBps;
        private BigDecimal frontLoadPct;
        private Integer totalFeeBps;

        private BigDecimal mgmtFeePercent;
        private BigDecimal salesFeePercent;
        private BigDecimal trusteeFeePercent;
        private BigDecimal adminFeePercent;
        private BigDecimal totalFeePercent;
    }

    public String getDisplayName() {
        if (fundMaster == null) {
            return childFundCd;
        }
        String className = classCode != null ? " " + classCode + "클래스" : "";
        return fundMaster.getFundName() + className;
    }

    public String getTotalFeeString() {
        if (fees == null || fees.getTotalFeePercent() == null) {
            return "0.00%";
        }
        return String.format("%.2f%%", fees.getTotalFeePercent());
    }

    public String getMinInvestmentString() {
        if (rules == null || rules.getMinInitialAmount() == null) {
            return "미정";
        }
        return String.format("%,d원", rules.getMinInitialAmount().intValue());
    }
}