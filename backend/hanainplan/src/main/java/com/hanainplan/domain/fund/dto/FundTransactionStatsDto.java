package com.hanainplan.domain.fund.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 펀드 거래 통계 DTO (하나인플랜)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundTransactionStatsDto {

    private String customerCi;
    
    // 매수 통계
    private int totalPurchaseCount;           // 총 매수 건수
    private BigDecimal totalPurchaseAmount;   // 총 매수 금액
    private BigDecimal totalPurchaseFee;      // 총 매수 수수료
    
    // 매도 통계
    private int totalRedemptionCount;         // 총 매도 건수
    private BigDecimal totalRedemptionAmount; // 총 매도 금액
    private BigDecimal totalRedemptionFee;    // 총 환매 수수료
    
    // 손익 통계
    private BigDecimal totalRealizedProfit;   // 총 실현 손익
    private BigDecimal totalFees;             // 총 수수료
    
    // 거래 통계
    private int totalTransactionCount;        // 총 거래 건수
    private BigDecimal netCashFlow;           // 순현금흐름 (매수 - 매도)
}

