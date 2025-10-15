package com.hanainplan.domain.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "disease_code")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiseaseCode {

    @Id
    @Column(name = "disease_code", length = 20)
    private String diseaseCode;

    @Column(name = "disease_name", length = 50, nullable = false)
    private String diseaseName;

    @Column(name = "disease_category", length = 50, nullable = false)
    private String diseaseCategory;

    @Column(name = "risk_level", length = 10)
    private String riskLevel;

    @Column(name = "is_insurable", length = 1, nullable = false)
    private String isInsurable;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "min_age", length = 10)
    private Integer minAge;

    @Column(name = "max_age", length = 10)
    private Integer maxAge;

    @Column(name = "waiting_period_days", length = 10)
    private Integer waitingPeriodDays;

    @Column(name = "renewal_cycle_years", length = 10)
    private Integer renewalCycleYears;

    @Column(name = "applicable_ins_count", length = 10)
    private Integer applicableInsCount;

    @Column(name = "applicable_ins_type", length = 50)
    private String applicableInsType;

    @Column(name = "insurance_comp_count", length = 10)
    private Integer insuranceCompCount;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    public boolean canSubscribeInsurance() {
        return "Y".equals(this.isInsurable);
    }

    public boolean isHighRisk() {
        return "상".equals(this.riskLevel);
    }

    public enum RiskLevel {
        HIGH("상"), MEDIUM("중"), LOW("하");

        private final String value;

        RiskLevel(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static RiskLevel fromValue(String value) {
            for (RiskLevel level : values()) {
                if (level.value.equals(value)) {
                    return level;
                }
            }
            return null;
        }
    }
}