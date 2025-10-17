package com.hanainplan.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_risk_profile_answer",
       indexes = {
           @Index(name = "idx_customer_id", columnList = "customer_id"),
           @Index(name = "idx_answered_at", columnList = "answered_at")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RiskProfileAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long answerId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "question_number", nullable = false)
    private Integer questionNumber; // 1~7

    @Column(name = "answer_score", nullable = false)
    private Integer answerScore; // 1~5

    @Column(name = "question_text", length = 500)
    private String questionText;

    @Column(name = "answer_text", length = 200)
    private String answerText;

    @CreatedDate
    @Column(name = "answered_at", nullable = false, updatable = false)
    private LocalDateTime answeredAt;

    // 질문별 가중치 (필요시 사용)
    @Column(name = "weight")
    @Builder.Default
    private Double weight = 1.0;

    // 평가 세션 ID (한 번의 평가를 그룹핑)
    @Column(name = "session_id", length = 50)
    private String sessionId;

    public static RiskProfileAnswer create(Long customerId, Integer questionNumber, 
                                         Integer answerScore, String questionText, 
                                         String answerText, String sessionId) {
        return RiskProfileAnswer.builder()
                .customerId(customerId)
                .questionNumber(questionNumber)
                .answerScore(answerScore)
                .questionText(questionText)
                .answerText(answerText)
                .sessionId(sessionId)
                .build();
    }

    public boolean isValidScore() {
        return answerScore != null && answerScore >= 1 && answerScore <= 5;
    }

    public boolean isValidQuestionNumber() {
        return questionNumber != null && questionNumber >= 1 && questionNumber <= 7;
    }
}
