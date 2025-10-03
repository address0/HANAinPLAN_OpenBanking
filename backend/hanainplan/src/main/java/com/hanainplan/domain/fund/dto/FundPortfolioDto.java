package com.hanainplan.domain.fund.dto;

import com.hanainplan.domain.fund.entity.FundPortfolio;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 펀드 포트폴리오 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundPortfolioDto {

    private Long portfolioId;
    private Long userId;
    private String customerCi;

    // 은행 정보
    private String bankCode;
    private String bankName;

    // 펀드 정보
    private String fundCode;
    private String fundName;
    private String fundType;
    private String riskLevel;

    // 매수 정보
    private LocalDate purchaseDate;
    private BigDecimal purchaseNav;
    private BigDecimal purchaseAmount;
    private BigDecimal purchaseFee;
    private BigDecimal purchaseUnits;

    // 현재 보유 정보
    private BigDecimal currentUnits;
    private BigDecimal currentNav;
    private BigDecimal currentValue;

    // 수익 정보
    private BigDecimal totalReturn;
    private BigDecimal returnRate;
    private BigDecimal accumulatedFees;

    // IRP 연계
    private String irpAccountNumber;

    // 외부 참조
    private Long subscriptionId;

    // 상태
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Entity -> DTO 변환
     */
    public static FundPortfolioDto fromEntity(FundPortfolio entity) {
        return FundPortfolioDto.builder()
                .portfolioId(entity.getPortfolioId())
                .userId(entity.getUserId())
                .customerCi(entity.getCustomerCi())
                .bankCode(entity.getBankCode())
                .bankName(entity.getBankName())
                .fundCode(entity.getFundCode())
                .fundName(entity.getFundName())
                .fundType(entity.getFundType())
                .riskLevel(entity.getRiskLevel())
                .purchaseDate(entity.getPurchaseDate())
                .purchaseNav(entity.getPurchaseNav())
                .purchaseAmount(entity.getPurchaseAmount())
                .purchaseFee(entity.getPurchaseFee())
                .purchaseUnits(entity.getPurchaseUnits())
                .currentUnits(entity.getCurrentUnits())
                .currentNav(entity.getCurrentNav())
                .currentValue(entity.getCurrentValue())
                .totalReturn(entity.getTotalReturn())
                .returnRate(entity.getReturnRate())
                .accumulatedFees(entity.getAccumulatedFees())
                .irpAccountNumber(entity.getIrpAccountNumber())
                .subscriptionId(entity.getSubscriptionId())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * 수익률 문자열 반환
     */
    public String getReturnRateString() {
        if (returnRate == null) {
            return "0.00%";
        }
        String sign = returnRate.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
        return String.format("%s%.2f%%", sign, returnRate);
    }

    /**
     * 수익 여부
     */
    public boolean isProfitable() {
        return totalReturn != null && totalReturn.compareTo(BigDecimal.ZERO) > 0;
    }
}

