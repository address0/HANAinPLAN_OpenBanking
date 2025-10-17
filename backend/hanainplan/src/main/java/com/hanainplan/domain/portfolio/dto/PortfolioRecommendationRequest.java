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
public class PortfolioRecommendationRequest {
    private Long customerId;
    private String irpAccountNumber;
    private Integer birthYear;
    private String industryCode;
    private String assetLevel;
    private BigDecimal riskProfileScore;
    private Boolean hasDisease;
}
