package com.hanainplan.domain.portfolio.dto;

import com.hanainplan.domain.user.entity.Customer;
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
public class PortfolioRecommendationResponse {
    private Long customerId;
    private String irpAccountNumber;
    private Customer.RiskProfileType riskProfileType;
    private String riskProfileDescription;
    
    private ModelPortfolio modelPortfolio;
    private SimilarUserPortfolio similarUserPortfolio;
    private RecommendedPortfolio recommendedPortfolio;
    
    private RecommendationMetadata metadata;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelPortfolio {
        private BigDecimal cashWeight;
        private BigDecimal depositWeight;
        private BigDecimal fundWeight;
        private String description;
        private String basis; // "RISK_PROFILE"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimilarUserPortfolio {
        private BigDecimal cashWeight;
        private BigDecimal depositWeight;
        private BigDecimal fundWeight;
        private Integer similarUserCount;
        private Double averageSimilarity;
        private String description;
        private String basis; // "SIMILAR_USERS"
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedPortfolio {
        private BigDecimal cashWeight;
        private BigDecimal depositWeight;
        private BigDecimal fundWeight;
        private String description;
        private String basis; // "COMBINED"
        private BigDecimal modelWeight; // 모델 포트폴리오 가중치 (60%)
        private BigDecimal similarUserWeight; // 유사 사용자 가중치 (40%)
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationMetadata {
        private LocalDateTime generatedAt;
        private String algorithm;
        private Integer totalUsersAnalyzed;
        private Integer similarUsersFound;
        private Double minSimilarityThreshold;
        private String constraints;
        private String notes;
    }

    public static PortfolioRecommendationResponse create(
            Long customerId,
            String irpAccountNumber,
            Customer.RiskProfileType riskProfileType,
            ModelPortfolio modelPortfolio,
            SimilarUserPortfolio similarUserPortfolio,
            RecommendedPortfolio recommendedPortfolio,
            RecommendationMetadata metadata) {
        
        return PortfolioRecommendationResponse.builder()
                .customerId(customerId)
                .irpAccountNumber(irpAccountNumber)
                .riskProfileType(riskProfileType)
                .riskProfileDescription(riskProfileType != null ? riskProfileType.getDescription() : null)
                .modelPortfolio(modelPortfolio)
                .similarUserPortfolio(similarUserPortfolio)
                .recommendedPortfolio(recommendedPortfolio)
                .metadata(metadata)
                .build();
    }
}
