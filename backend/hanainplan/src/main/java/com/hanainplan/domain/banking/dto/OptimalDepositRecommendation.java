package com.hanainplan.domain.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimalDepositRecommendation {

    private String depositCode;
    private String depositName;
    private String bankCode;
    private String bankName;

    private Integer productType;
    private String productTypeName;
    private Integer contractPeriod;
    private String contractPeriodUnit;
    private String maturityPeriod;
    private BigDecimal depositAmount;

    private BigDecimal appliedRate;
    private BigDecimal expectedInterest;
    private BigDecimal expectedMaturityAmount;
    private LocalDate expectedMaturityDate;

    private String recommendationReason;
    private Integer yearsToRetirement;
    private BigDecimal currentIrpBalance;

    private List<AlternativeDepositOption> alternativeOptions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlternativeDepositOption {
        private String bankCode;
        private String bankName;
        private String maturityPeriod;
        private BigDecimal interestRate;
        private BigDecimal expectedInterest;
        private BigDecimal expectedMaturityAmount;
        private String reason;
    }
}