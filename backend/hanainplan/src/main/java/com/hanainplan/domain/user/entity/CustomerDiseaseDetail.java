package com.hanainplan.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_customer_disease_detail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDiseaseDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Long detailId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "disease_code", length = 10, nullable = false)
    private String diseaseCode;

    @Column(name = "disease_name", length = 200, nullable = false)
    private String diseaseName;

    @Column(name = "disease_category", length = 100, nullable = false)
    private String diseaseCategory;

    @Column(name = "risk_level", nullable = false)
    private String riskLevel;

    @Column(name = "severity", nullable = false)
    private String severity;

    @Column(name = "progress_period", nullable = false)
    private String progressPeriod;

    @Column(name = "is_chronic", nullable = false)
    private Boolean isChronic;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_date", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();

    public boolean isHighRiskDisease() {
        return "HIGH".equals(riskLevel) || "SEVERE".equals(severity);
    }

    public boolean isChronicSevereDisease() {
        return Boolean.TRUE.equals(isChronic) && "SEVERE".equals(severity);
    }
}