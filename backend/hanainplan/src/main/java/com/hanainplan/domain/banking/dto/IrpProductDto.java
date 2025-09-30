package com.hanainplan.domain.banking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * IRP 상품 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IrpProductDto {

    private Long irpProductId;
    private String bankCode;
    private String productCode;
    private String productName;
    private String productType;
    private String managementCompany;
    private String trustCompany;
    private BigDecimal minimumContribution;
    private BigDecimal maximumContribution;
    private BigDecimal annualContributionLimit;
    private BigDecimal managementFeeRate;
    private BigDecimal trustFeeRate;
    private BigDecimal salesFeeRate;
    private BigDecimal totalFeeRate;
    private String investmentOptions;
    private String riskLevel;
    private BigDecimal expectedReturnRate;
    private String guaranteeType;
    private BigDecimal guaranteeRate;
    private String maturityAge;
    private String earlyWithdrawalPenalty;
    private String taxBenefit;
    private String contributionFrequency;
    private String contributionMethod;
    private Integer minimumHoldingPeriod;
    private String autoRebalancing;
    private String rebalancingFrequency;
    private BigDecimal performanceFee;
    private BigDecimal performanceFeeThreshold;
    private String fundAllocation;
    private String benchmarkIndex;
    private String description;
    private String precautions;
    private Boolean isActive;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastSyncDate;

    private String syncStatus;
    private String externalProductId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;

    // 편의 메소드들
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public boolean isOnSale() {
        LocalDate now = LocalDate.now();
        return (startDate == null || !now.isBefore(startDate)) &&
               (endDate == null || !now.isAfter(endDate));
    }

    public String getBankName() {
        switch (bankCode) {
            case "HANA": return "하나은행";
            case "KOOKMIN": return "국민은행";
            case "SHINHAN": return "신한은행";
            default: return bankCode;
        }
    }

    public String getProductTypeDisplay() {
        if (productType == null) return "-";

        switch (productType) {
            case "DC": return "확정기여형";
            case "DB": return "확정급여형";
            case "MIXED": return "혼합형";
            default: return productType;
        }
    }

    public String getRiskLevelDisplay() {
        if (riskLevel == null) return "-";

        switch (riskLevel) {
            case "1": return "매우낮음";
            case "2": return "낮음";
            case "3": return "중간";
            case "4": return "높음";
            case "5": return "매우높음";
            default: return riskLevel;
        }
    }

    public String getGuaranteeTypeDisplay() {
        if (guaranteeType == null) return "-";

        switch (guaranteeType) {
            case "PRINCIPAL": return "원금보장";
            case "RETURN": return "수익보장";
            case "NONE": return "무보장";
            default: return guaranteeType;
        }
    }

    public String getSyncStatusDisplay() {
        if (syncStatus == null) return "-";

        switch (syncStatus) {
            case "PENDING": return "대기중";
            case "RUNNING": return "동기화중";
            case "SUCCESS": return "성공";
            case "FAILED": return "실패";
            case "PARTIAL": return "부분성공";
            default: return syncStatus;
        }
    }

    public String getAutoRebalancingDisplay() {
        if (autoRebalancing == null) return "-";
        return "Y".equals(autoRebalancing) ? "자동" : "수동";
    }

    public String getFormattedMinimumContribution() {
        return minimumContribution != null ? String.format("%,.0f원", minimumContribution) : "-";
    }

    public String getFormattedMaximumContribution() {
        return maximumContribution != null ? String.format("%,.0f원", maximumContribution) : "-";
    }

    public String getFormattedAnnualContributionLimit() {
        return annualContributionLimit != null ? String.format("%,.0f원", annualContributionLimit) : "-";
    }

    public String getFormattedExpectedReturnRate() {
        return expectedReturnRate != null ? String.format("%.2f%%", expectedReturnRate) : "-";
    }

    public String getFormattedTotalFeeRate() {
        return totalFeeRate != null ? String.format("%.4f%%", totalFeeRate) : "-";
    }
}
