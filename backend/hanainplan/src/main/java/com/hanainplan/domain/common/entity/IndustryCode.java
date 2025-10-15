package com.hanainplan.domain.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Column(name = "risk_level", length = 10, nullable = false)
    private String riskLevel;

    @Column(name = "special_conditions", length = 20)
    private String specialConditions;

    @Column(name = "premium_surcharge_rate", length = 10)
    private Double premiumSurchargeRate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    public boolean isHighRiskIndustry() {
        return "상".equals(this.riskLevel);
    }

    public double getRecommendationWeight() {
        double weight = 1.0;

        switch (this.riskLevel) {
            case "하" -> weight += 0.3;
            case "중" -> weight += 0.1;
            case "상" -> weight -= 0.2;
        }

        return Math.max(0.1, weight);
    }

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
            return MEDIUM;
        }
    }
}