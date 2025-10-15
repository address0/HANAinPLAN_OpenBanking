package com.hanainplan.domain.user.entity;

import com.hanainplan.domain.common.entity.IndustryCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_customer")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "recent_medical_advice")
    private Boolean recentMedicalAdvice;

    @Column(name = "recent_hospitalization")
    private Boolean recentHospitalization;

    @Column(name = "major_disease")
    private Boolean majorDisease;

    @Column(name = "long_term_medication")
    private Boolean longTermMedication;

    @Column(name = "disability_registered")
    private Boolean disabilityRegistered;

    @Column(name = "insurance_rejection")
    private Boolean insuranceRejection;

    @Column(name = "industry_code", length = 10)
    private String industryCode;

    @Column(name = "industry_name", length = 100)
    private String industryName;

    @Column(name = "career_years")
    private Integer careerYears;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_level")
    private AssetLevel assetLevel;

    @Column(name = "has_irp_account")
    private Boolean hasIrpAccount;

    @Column(name = "irp_bank_code", length = 10)
    private String irpBankCode;

    @Column(name = "irp_account_number", length = 50)
    private String irpAccountNumber;

    @Column(name = "created_date", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();

    public boolean hasIrpAccount() {
        return Boolean.TRUE.equals(hasIrpAccount);
    }

    public enum AssetLevel {
        UNDER_1("1천만원 미만"),
        FROM_1_TO_5("1천만원 ~ 5천만원"),
        FROM_5_TO_10("5천만원 ~ 1억원"),
        FROM_10_TO_30("1억원 ~ 3억원"),
        FROM_30_TO_50("3억원 ~ 5억원"),
        OVER_50("5억원 이상");

        private final String description;

        AssetLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public static AssetLevel fromValue(String value) {
            switch (value) {
                case "under_1": return UNDER_1;
                case "1_to_5": return FROM_1_TO_5;
                case "5_to_10": return FROM_5_TO_10;
                case "10_to_30": return FROM_10_TO_30;
                case "30_to_50": return FROM_30_TO_50;
                case "over_50": return OVER_50;
                default: return FROM_1_TO_5;
            }
        }
    }

    public boolean hasDiseaseHistory() {
        return Boolean.TRUE.equals(majorDisease);
    }

    public boolean hasHealthRisk() {
        return Boolean.TRUE.equals(recentMedicalAdvice) || 
               Boolean.TRUE.equals(recentHospitalization) ||
               Boolean.TRUE.equals(longTermMedication) ||
               Boolean.TRUE.equals(disabilityRegistered);
    }

}