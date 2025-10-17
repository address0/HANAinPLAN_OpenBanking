package com.hanainplan.domain.user.dto;

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
public class RiskProfileRequest {
    private Long customerId;
    private String sessionId;
    private List<QuestionAnswer> answers;

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
}
