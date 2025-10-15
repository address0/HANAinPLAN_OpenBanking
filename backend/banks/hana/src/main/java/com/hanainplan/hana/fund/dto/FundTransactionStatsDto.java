package com.hanainplan.hana.fund.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundTransactionStatsDto {

    private String customerCi;

    private int totalPurchaseCount;
    private BigDecimal totalPurchaseAmount;
    private BigDecimal totalPurchaseFee;

    private int totalRedemptionCount;
    private BigDecimal totalRedemptionAmount;
    private BigDecimal totalRedemptionFee;

    private BigDecimal totalRealizedProfit;
    private BigDecimal totalFees;

    private int totalTransactionCount;
    private BigDecimal netCashFlow;
}