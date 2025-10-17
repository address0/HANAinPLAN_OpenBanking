package com.hanainplan.domain.user.dto;

import com.hanainplan.domain.user.entity.Customer;
import com.hanainplan.domain.user.entity.RiskProfileAnswer;
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
public class RiskProfileResponse {
    private Long customerId;
    private BigDecimal riskProfileScore;
    private String riskProfileType;
    private String riskProfileDescription;
    private LocalDateTime evaluatedAt;
    private List<QuestionAnswer> answers;
    private PortfolioRecommendation recommendation;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionAnswer {
        private Integer questionNumber;
        private Integer answerScore;
        private String questionText;
        private String answerText;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortfolioRecommendation {
        private BigDecimal cashWeight;
        private BigDecimal depositWeight;
        private BigDecimal fundWeight;
        private String description;
    }

    public static RiskProfileResponse from(Customer customer, List<RiskProfileAnswer> answers) {
        RiskProfileResponseBuilder builder = RiskProfileResponse.builder()
                .customerId(customer.getCustomerId())
                .riskProfileScore(customer.getRiskProfileScore())
                .riskProfileType(customer.getRiskProfileType() != null ? customer.getRiskProfileType().name() : null)
                .riskProfileDescription(customer.getRiskProfileType() != null ? 
                    customer.getRiskProfileType().getDescription() : null)
                .evaluatedAt(customer.getRiskProfileUpdatedAt());

        if (answers != null && !answers.isEmpty()) {
            builder.answers(answers.stream()
                    .map(answer -> QuestionAnswer.builder()
                            .questionNumber(answer.getQuestionNumber())
                            .answerScore(answer.getAnswerScore())
                            .questionText(answer.getQuestionText())
                            .answerText(answer.getAnswerText())
                            .build())
                    .toList());
        }

        PortfolioRecommendation recommendation = createPortfolioRecommendation(customer.getRiskProfileType());
        builder.recommendation(recommendation);

        return builder.build();
    }

    private static PortfolioRecommendation createPortfolioRecommendation(Customer.RiskProfileType riskType) {
        if (riskType == null) {
            return PortfolioRecommendation.builder()
                    .cashWeight(BigDecimal.valueOf(5.0))
                    .depositWeight(BigDecimal.valueOf(40.0))
                    .fundWeight(BigDecimal.valueOf(55.0))
                    .description("기본 포트폴리오")
                    .build();
        }

        return switch (riskType) {
            case STABLE -> PortfolioRecommendation.builder()
                    .cashWeight(BigDecimal.valueOf(5.0))
                    .depositWeight(BigDecimal.valueOf(80.0))
                    .fundWeight(BigDecimal.valueOf(15.0))
                    .description("안정형: 원금보전 중심")
                    .build();
            case STABLE_PLUS -> PortfolioRecommendation.builder()
                    .cashWeight(BigDecimal.valueOf(5.0))
                    .depositWeight(BigDecimal.valueOf(60.0))
                    .fundWeight(BigDecimal.valueOf(35.0))
                    .description("안정추구형: 저위험·저수익")
                    .build();
            case NEUTRAL -> PortfolioRecommendation.builder()
                    .cashWeight(BigDecimal.valueOf(5.0))
                    .depositWeight(BigDecimal.valueOf(40.0))
                    .fundWeight(BigDecimal.valueOf(55.0))
                    .description("중립형: 균형투자")
                    .build();
            case AGGRESSIVE -> PortfolioRecommendation.builder()
                    .cashWeight(BigDecimal.valueOf(5.0))
                    .depositWeight(BigDecimal.valueOf(25.0))
                    .fundWeight(BigDecimal.valueOf(70.0))
                    .description("적극형: 성장지향")
                    .build();
        };
    }
}
