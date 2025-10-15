package com.hanainplan.hana.fund.dto;

import com.hanainplan.hana.fund.entity.*;
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

    public static FundClassDetailDto fromEntity(FundClass entity, FundNav latestNav) {
        FundClassDetailDtoBuilder builder = FundClassDetailDto.builder()
                .childFundCd(entity.getChildFundCd())
                .classCode(entity.getClassCode())
                .loadType(entity.getLoadType())
                .taxCategory(entity.getTaxCategory())
                .saleStatus(entity.getSaleStatus())
                .sourceUrl(entity.getSourceUrl());

        if (entity.getFundMaster() != null) {
            builder.fundMaster(FundMasterDto.fromEntity(entity.getFundMaster()));
        }

        if (entity.getFundRules() != null) {
            builder.rules(FundRulesDto.fromEntity(entity.getFundRules()));
        }

        if (entity.getFundFees() != null) {
            builder.fees(FundFeesDto.fromEntity(entity.getFundFees()));
        }

        if (latestNav != null) {
            builder.latestNav(latestNav.getNav())
                   .latestNavDate(latestNav.getNavDate());
        }

        return builder.build();
    }

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

        public static FundMasterDto fromEntity(FundMaster entity) {
            return FundMasterDto.builder()
                    .fundCd(entity.getFundCd())
                    .fundName(entity.getFundName())
                    .fundGb(entity.getFundGb())
                    .assetType(entity.getAssetType())
                    .riskGrade(entity.getRiskGrade())
                    .currency(entity.getCurrency())
                    .isActive(entity.getIsActive())
                    .build();
        }
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

        public static FundRulesDto fromEntity(FundRules entity) {
            return FundRulesDto.builder()
                    .cutoffTime(entity.getCutoffTime())
                    .navPublishTime(entity.getNavPublishTime())
                    .buySettleDays(entity.getBuySettleDays())
                    .redeemSettleDays(entity.getRedeemSettleDays())
                    .unitType(entity.getUnitType())
                    .minInitialAmount(entity.getMinInitialAmount())
                    .minAdditional(entity.getMinAdditional())
                    .incrementAmount(entity.getIncrementAmount())
                    .allowSip(entity.getAllowSip())
                    .allowSwitch(entity.getAllowSwitch())
                    .redemptionFeeRate(entity.getRedemptionFeeRate())
                    .redemptionFeeDays(entity.getRedemptionFeeDays())
                    .build();
        }
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

        public static FundFeesDto fromEntity(FundFees entity) {
            return FundFeesDto.builder()
                    .mgmtFeeBps(entity.getMgmtFeeBps())
                    .salesFeeBps(entity.getSalesFeeBps())
                    .trusteeFeeBps(entity.getTrusteeFeeBps())
                    .adminFeeBps(entity.getAdminFeeBps())
                    .frontLoadPct(entity.getFrontLoadPct())
                    .totalFeeBps(entity.getTotalFeeBps())
                    .mgmtFeePercent(entity.getMgmtFeePercent())
                    .salesFeePercent(entity.getSalesFeePercent())
                    .trusteeFeePercent(entity.getTrusteeFeePercent())
                    .adminFeePercent(entity.getAdminFeePercent())
                    .totalFeePercent(entity.getTotalFeePercent())
                    .build();
        }
    }
}