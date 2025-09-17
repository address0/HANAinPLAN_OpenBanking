package com.hanainplan.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 고객 질병 상세 정보 엔터티
 * - 고객의 질병 이력 및 상세 정보 관리
 */
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
    private String diseaseCode; // 질병 코드

    @Column(name = "disease_name", length = 200, nullable = false)
    private String diseaseName; // 질병명

    @Column(name = "disease_category", length = 100, nullable = false)
    private String diseaseCategory; // 질병 분류

    @Column(name = "risk_level", nullable = false)
    private String riskLevel; // 위험도

    @Column(name = "severity", nullable = false)
    private String severity; // 중증도

    @Column(name = "progress_period", nullable = false)
    private String progressPeriod; // 경과 기간

    @Column(name = "is_chronic", nullable = false)
    private Boolean isChronic; // 만성 여부

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // 질병 설명

    @Column(name = "created_date", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();

    // 외래 키 제약조건 제거 - 단순히 ID로만 연결
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "customer_id", referencedColumnName = "customer_id", insertable = false, updatable = false)
    // private Customer customer;

    /**
     * 고위험 질병 여부 확인
     */
    public boolean isHighRiskDisease() {
        return "HIGH".equals(riskLevel) || "SEVERE".equals(severity);
    }

    /**
     * 만성 중증 질병 여부 확인
     */
    public boolean isChronicSevereDisease() {
        return Boolean.TRUE.equals(isChronic) && "SEVERE".equals(severity);
    }
}
