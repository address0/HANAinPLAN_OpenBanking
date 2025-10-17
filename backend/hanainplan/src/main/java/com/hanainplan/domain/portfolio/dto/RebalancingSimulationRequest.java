package com.hanainplan.domain.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RebalancingSimulationRequest {
    private Long customerId;
    private String triggerType; // MANUAL, PERIODIC, THRESHOLD
    private BigDecimal cashWeight;
    private BigDecimal depositWeight;
    private BigDecimal fundWeight;
}
