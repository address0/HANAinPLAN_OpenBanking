package com.hanainplan.domain.fund.dto;

import com.hanainplan.domain.banking.dto.DepositPortfolioDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntegratedPortfolioDto {

    private Long userId;

    @Builder.Default
    private List<DepositPortfolioDto> deposits = new ArrayList<>();

    @Builder.Default
    private List<FundPortfolioDto> funds = new ArrayList<>();

    private PortfolioSummary summary;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PortfolioSummary {

        private BigDecimal totalDepositPrincipal;
        private BigDecimal totalDepositValue;
        private BigDecimal totalDepositInterest;

        private BigDecimal totalFundInvestment;
        private BigDecimal totalFundValue;
        private BigDecimal totalFundReturn;
        private BigDecimal totalFundReturnRate;

        private BigDecimal totalInvestment;
        private BigDecimal totalValue;
        private BigDecimal totalReturn;
        private BigDecimal totalReturnRate;

        private int depositCount;
        private int fundCount;
        private int totalProductCount;

        public void calculateReturnRates() {
            if (totalFundInvestment != null && totalFundInvestment.compareTo(BigDecimal.ZERO) > 0) {
                totalFundReturnRate = totalFundReturn
                    .divide(totalFundInvestment, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            } else {
                totalFundReturnRate = BigDecimal.ZERO;
            }

            if (totalInvestment != null && totalInvestment.compareTo(BigDecimal.ZERO) > 0) {
                totalReturnRate = totalReturn
                    .divide(totalInvestment, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            } else {
                totalReturnRate = BigDecimal.ZERO;
            }
        }

        public String getTotalReturnRateString() {
            if (totalReturnRate == null) {
                return "0.00%";
            }
            String sign = totalReturnRate.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
            return String.format("%s%.2f%%", sign, totalReturnRate);
        }

        public String getFundReturnRateString() {
            if (totalFundReturnRate == null) {
                return "0.00%";
            }
            String sign = totalFundReturnRate.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
            return String.format("%s%.2f%%", sign, totalFundReturnRate);
        }
    }

    public void calculateSummary() {
        PortfolioSummary newSummary = new PortfolioSummary();

        newSummary.totalDepositPrincipal = deposits.stream()
            .map(DepositPortfolioDto::getPrincipalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        newSummary.totalDepositValue = deposits.stream()
            .map(DepositPortfolioDto::getMaturityAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        newSummary.totalDepositInterest = deposits.stream()
            .map(DepositPortfolioDto::getExpectedInterest)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        newSummary.totalFundInvestment = funds.stream()
            .filter(f -> "ACTIVE".equals(f.getStatus()) || "PARTIAL_SOLD".equals(f.getStatus()))
            .map(FundPortfolioDto::getPurchaseAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        newSummary.totalFundValue = funds.stream()
            .filter(f -> "ACTIVE".equals(f.getStatus()) || "PARTIAL_SOLD".equals(f.getStatus()))
            .map(f -> f.getCurrentValue() != null ? f.getCurrentValue() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        newSummary.totalFundReturn = funds.stream()
            .filter(f -> "ACTIVE".equals(f.getStatus()) || "PARTIAL_SOLD".equals(f.getStatus()))
            .map(f -> f.getTotalReturn() != null ? f.getTotalReturn() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        newSummary.totalInvestment = newSummary.totalDepositPrincipal.add(newSummary.totalFundInvestment);
        newSummary.totalValue = newSummary.totalDepositValue.add(newSummary.totalFundValue);
        newSummary.totalReturn = newSummary.totalDepositInterest.add(newSummary.totalFundReturn);

        newSummary.depositCount = deposits.size();
        newSummary.fundCount = (int) funds.stream()
            .filter(f -> "ACTIVE".equals(f.getStatus()) || "PARTIAL_SOLD".equals(f.getStatus()))
            .count();
        newSummary.totalProductCount = newSummary.depositCount + newSummary.fundCount;

        newSummary.calculateReturnRates();

        this.summary = newSummary;
    }

    public ProductAllocation getProductAllocation() {
        BigDecimal total = summary.getTotalInvestment();

        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            return new ProductAllocation(BigDecimal.ZERO, BigDecimal.ZERO);
        }

        BigDecimal depositRatio = summary.getTotalDepositPrincipal()
            .divide(total, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));

        BigDecimal fundRatio = summary.getTotalFundInvestment()
            .divide(total, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));

        return new ProductAllocation(depositRatio, fundRatio);
    }

    @Data
    @AllArgsConstructor
    public static class ProductAllocation {
        private BigDecimal depositRatio;
        private BigDecimal fundRatio;
    }
}