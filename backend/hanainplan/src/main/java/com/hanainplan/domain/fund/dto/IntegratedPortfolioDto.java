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

/**
 * 통합 포트폴리오 DTO
 * - 정기예금, 펀드, 보험, 채권 등 모든 금융상품을 한눈에 조회
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntegratedPortfolioDto {

    private Long userId;
    
    // 각 상품별 포트폴리오
    @Builder.Default
    private List<DepositPortfolioDto> deposits = new ArrayList<>();
    
    @Builder.Default
    private List<FundPortfolioDto> funds = new ArrayList<>();
    
    // TODO: 보험, 채권 추가 예정
    // private List<InsurancePortfolioDto> insurances = new ArrayList<>();
    // private List<BondPortfolioDto> bonds = new ArrayList<>();
    
    // 통합 통계
    private PortfolioSummary summary;

    /**
     * 포트폴리오 요약 정보
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PortfolioSummary {
        
        // 정기예금
        private BigDecimal totalDepositPrincipal;      // 총 예금 원금
        private BigDecimal totalDepositValue;          // 총 예금 평가액 (원금 + 이자)
        private BigDecimal totalDepositInterest;       // 총 예금 이자
        
        // 펀드
        private BigDecimal totalFundInvestment;        // 총 펀드 투자금
        private BigDecimal totalFundValue;             // 총 펀드 평가액
        private BigDecimal totalFundReturn;            // 총 펀드 수익
        private BigDecimal totalFundReturnRate;        // 총 펀드 수익률
        
        // 전체 통합
        private BigDecimal totalInvestment;            // 총 투자금 (예금원금 + 펀드투자금)
        private BigDecimal totalValue;                 // 총 평가액
        private BigDecimal totalReturn;                // 총 수익
        private BigDecimal totalReturnRate;            // 총 수익률
        
        // 상품별 개수
        private int depositCount;
        private int fundCount;
        private int totalProductCount;

        /**
         * 수익률 계산
         */
        public void calculateReturnRates() {
            // 펀드 수익률
            if (totalFundInvestment != null && totalFundInvestment.compareTo(BigDecimal.ZERO) > 0) {
                totalFundReturnRate = totalFundReturn
                    .divide(totalFundInvestment, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            } else {
                totalFundReturnRate = BigDecimal.ZERO;
            }
            
            // 전체 수익률
            if (totalInvestment != null && totalInvestment.compareTo(BigDecimal.ZERO) > 0) {
                totalReturnRate = totalReturn
                    .divide(totalInvestment, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            } else {
                totalReturnRate = BigDecimal.ZERO;
            }
        }

        /**
         * 수익률 문자열 반환
         */
        public String getTotalReturnRateString() {
            if (totalReturnRate == null) {
                return "0.00%";
            }
            String sign = totalReturnRate.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
            return String.format("%s%.2f%%", sign, totalReturnRate);
        }

        /**
         * 펀드 수익률 문자열 반환
         */
        public String getFundReturnRateString() {
            if (totalFundReturnRate == null) {
                return "0.00%";
            }
            String sign = totalFundReturnRate.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
            return String.format("%s%.2f%%", sign, totalFundReturnRate);
        }
    }

    /**
     * 요약 정보 계산
     */
    public void calculateSummary() {
        PortfolioSummary newSummary = new PortfolioSummary();
        
        // 정기예금 집계
        newSummary.totalDepositPrincipal = deposits.stream()
            .map(DepositPortfolioDto::getPrincipalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        newSummary.totalDepositValue = deposits.stream()
            .map(DepositPortfolioDto::getMaturityAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        newSummary.totalDepositInterest = deposits.stream()
            .map(DepositPortfolioDto::getExpectedInterest)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 펀드 집계
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
        
        // 전체 통합
        newSummary.totalInvestment = newSummary.totalDepositPrincipal.add(newSummary.totalFundInvestment);
        newSummary.totalValue = newSummary.totalDepositValue.add(newSummary.totalFundValue);
        newSummary.totalReturn = newSummary.totalDepositInterest.add(newSummary.totalFundReturn);
        
        // 개수
        newSummary.depositCount = deposits.size();
        newSummary.fundCount = (int) funds.stream()
            .filter(f -> "ACTIVE".equals(f.getStatus()) || "PARTIAL_SOLD".equals(f.getStatus()))
            .count();
        newSummary.totalProductCount = newSummary.depositCount + newSummary.fundCount;
        
        // 수익률 계산
        newSummary.calculateReturnRates();
        
        this.summary = newSummary;
    }

    /**
     * 상품 유형별 비중 계산
     */
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

    /**
     * 상품 비중 클래스
     */
    @Data
    @AllArgsConstructor
    public static class ProductAllocation {
        private BigDecimal depositRatio;    // 예금 비중 (%)
        private BigDecimal fundRatio;        // 펀드 비중 (%)
        // TODO: 보험, 채권 비중 추가
    }
}

