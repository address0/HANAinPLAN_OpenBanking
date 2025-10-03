package com.hanainplan.hana.fund.dto;

import com.hanainplan.hana.fund.entity.FundProduct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 하나은행 펀드 상품 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundProductResponseDto {

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

    /**
     * Entity -> DTO 변환
     */
    public static FundProductResponseDto fromEntity(FundProduct entity) {
        return FundProductResponseDto.builder()
                .fundCode(entity.getFundCode())
                .fundName(entity.getFundName())
                .fundType(entity.getFundType())
                .investmentRegion(entity.getInvestmentRegion())
                .riskLevel(entity.getRiskLevel())
                .salesFeeRate(entity.getSalesFeeRate())
                .managementFeeRate(entity.getManagementFeeRate())
                .trustFeeRate(entity.getTrustFeeRate())
                .totalExpenseRatio(entity.getTotalExpenseRatio())
                .redemptionFeeRate(entity.getRedemptionFeeRate())
                .return1month(entity.getReturn1month())
                .return3month(entity.getReturn3month())
                .return6month(entity.getReturn6month())
                .return1year(entity.getReturn1year())
                .return3year(entity.getReturn3year())
                .managementCompany(entity.getManagementCompany())
                .trustCompany(entity.getTrustCompany())
                .minInvestmentAmount(entity.getMinInvestmentAmount())
                .isIrpEligible(entity.getIsIrpEligible())
                .description(entity.getDescription())
                .isActive(entity.getIsActive())
                .build();
    }

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
}

