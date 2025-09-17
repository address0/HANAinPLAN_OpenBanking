package com.hanainplan.domain.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 업종코드 엔터티
 * - 고객의 직업/업종 분류
 * - 보험료 산정 및 위험도 평가에 활용
 * - 상품 추천 알고리즘의 기준 데이터
 */
@Entity
@Table(name = "industry_code")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndustryCode {

    @Id
    @Column(name = "industry_code", length = 20)
    private String industryCode;

    @Column(name = "industry_name", length = 50, nullable = false)
    private String industryName;

    @Column(name = "industry_classification", length = 50, nullable = false)
    private String industryClassification;

    /**
     * 보험가입 위험등급: 상, 중, 하
     */
    @Column(name = "risk_level", length = 10, nullable = false)
    private String riskLevel;

    @Column(name = "special_conditions", length = 20)
    private String specialConditions;

    @Column(name = "premium_surcharge_rate", length = 10)
    private Double premiumSurchargeRate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 고위험 업종 여부 확인
     */
    public boolean isHighRiskIndustry() {
        return "상".equals(this.riskLevel);
    }

    /**
     * 업종별 추천 가중치 계산
     * 위험도가 낮고 소득이 높을수록 높은 가중치
     */
    public double getRecommendationWeight() {
        double weight = 1.0;

        // 위험도에 따른 가중치 조정
        switch (this.riskLevel) {
            case "하" -> weight += 0.3;
            case "중" -> weight += 0.1;
            case "상" -> weight -= 0.2;
        }

        return Math.max(0.1, weight); // 최소 0.1 보장
    }

    /**
     * 업종 위험등급 enum
     */
    public enum IndustryRiskLevel {
        HIGH("상"), MEDIUM("중"), LOW("하");

        private final String value;

        IndustryRiskLevel(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static IndustryRiskLevel fromValue(String value) {
            for (IndustryRiskLevel level : values()) {
                if (level.value.equals(value)) {
                    return level;
                }
            }
            return MEDIUM; // 기본값
        }
    }
}