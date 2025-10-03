package com.hanainplan.domain.fund.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 펀드 상품 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundProductDto {

    private String fundCode;
    private String fundName;
    private String fundType;
    private String investmentRegion;
    private String riskLevel;

    // 수수료 정보
    private BigDecimal salesFeeRate;
    private BigDecimal managementFeeRate;
    private BigDecimal trustFeeRate;
    private BigDecimal totalExpenseRatio;
    private BigDecimal redemptionFeeRate;

    // 수익률 정보
    private BigDecimal return1month;
    private BigDecimal return3month;
    private BigDecimal return6month;
    private BigDecimal return1year;
    private BigDecimal return3year;

    // 기타 정보
    private String managementCompany;
    private String trustCompany;
    private BigDecimal minInvestmentAmount;
    private Boolean isIrpEligible;
    private String description;

    private Boolean isActive;

    // 추가 계산 필드
    private String riskLevelDescription;
    private String fundTypeDescription;

    /**
     * 위험등급 설명 반환
     */
    public String getRiskLevelDescription() {
        return switch (riskLevel) {
            case "1" -> "매우 높음";
            case "2" -> "높음";
            case "3" -> "보통";
            case "4" -> "낮음";
            case "5" -> "매우 낮음";
            default -> "알 수 없음";
        };
    }

    /**
     * 총 보수율 문자열 반환
     */
    public String getTotalExpenseRatioString() {
        if (totalExpenseRatio == null) {
            return "0.00%";
        }
        return String.format("%.2f%%", totalExpenseRatio);
    }
}

