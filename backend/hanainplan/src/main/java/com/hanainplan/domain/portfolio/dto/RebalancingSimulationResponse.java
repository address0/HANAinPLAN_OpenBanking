package com.hanainplan.domain.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RebalancingSimulationResponse {
    private Long jobId;
    private Long customerId;
    private String irpAccountNumber;
    private String triggerType;
    private String status;
    
    private PortfolioSnapshot currentPortfolio;
    private PortfolioSnapshot targetPortfolio;
    private List<RebalancingOrder> orders;
    private PortfolioSnapshot expectedPortfolio;
    
    private BigDecimal totalFee;
    private BigDecimal totalOrderAmount;
    private String message;
    private LocalDateTime createdAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortfolioSnapshot {
        private BigDecimal totalValue;
        private BigDecimal cashWeight;
        private BigDecimal depositWeight;
        private BigDecimal fundWeight;
        private BigDecimal cashAmount;
        private BigDecimal depositAmount;
        private BigDecimal fundAmount;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RebalancingOrder {
        private String orderType; // BUY, SELL
        private String assetType; // FUND
        private String fundCode;
        private String fundName;
        private BigDecimal orderAmount;
        private BigDecimal expectedNav;
        private BigDecimal orderUnits;
        private BigDecimal fee;
        private String reason; // 리밸런싱 사유
    }
}
