package com.hanainplan.domain.fund.dto;

import com.hanainplan.domain.fund.entity.FundPortfolio;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundPortfolioDto {

    private Long portfolioId;
    private Long userId;
    private String customerCi;

    private String bankCode;
    private String bankName;

    private String fundCode;
    private String childFundCd;
    private String fundName;
    private String classCode;
    private String fundType;
    private String riskLevel;

    private LocalDate purchaseDate;
    private BigDecimal purchaseNav;
    private BigDecimal purchaseAmount;
    private BigDecimal purchaseFee;
    private BigDecimal purchaseUnits;

    private BigDecimal currentUnits;
    private BigDecimal currentNav;
    private BigDecimal currentValue;

    private BigDecimal totalReturn;
    private BigDecimal returnRate;
    private BigDecimal accumulatedFees;

    private String irpAccountNumber;

    private Long subscriptionId;

    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FundPortfolioDto fromEntity(FundPortfolio entity) {
        return FundPortfolioDto.builder()
                .portfolioId(entity.getPortfolioId())
                .userId(entity.getUserId())
                .customerCi(entity.getCustomerCi())
                .bankCode(entity.getBankCode())
                .bankName(entity.getBankName())
                .fundCode(entity.getFundCode())
                .childFundCd(entity.getChildFundCd())
                .fundName(entity.getFundName())
                .classCode(entity.getClassCode())
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

    public String getReturnRateString() {
        if (returnRate == null) {
            return "0.00%";
        }
        String sign = returnRate.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
        return String.format("%s%.2f%%", sign, returnRate);
    }

    public boolean isProfitable() {
        return totalReturn != null && totalReturn.compareTo(BigDecimal.ZERO) > 0;
    }
}