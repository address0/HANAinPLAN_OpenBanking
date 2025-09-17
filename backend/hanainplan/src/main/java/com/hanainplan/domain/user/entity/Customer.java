package com.hanainplan.domain.user.entity;

import com.hanainplan.domain.common.entity.IndustryCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 일반고객 엔터티
 * - 하나인플랜 서비스 일반고객 정보 관리
 * - 사용자와 1:1 관계로 확장 정보 저장
 */
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

    // 건강 정보
    @Column(name = "recent_medical_advice")
    private Boolean recentMedicalAdvice; // 최근 3개월 내 의사 권유사항

    @Column(name = "recent_hospitalization")
    private Boolean recentHospitalization; // 최근 2년 내 입원/수술

    @Column(name = "major_disease")
    private Boolean majorDisease; // 최근 5년 내 중증질환

    @Column(name = "long_term_medication")
    private Boolean longTermMedication; // 장기복용 약물

    @Column(name = "disability_registered")
    private Boolean disabilityRegistered; // 장애등록 여부

    @Column(name = "insurance_rejection")
    private Boolean insuranceRejection; // 보험 거절 이력

    // 직업 정보
    @Column(name = "industry_code", length = 10)
    private String industryCode; // 직종 코드

    @Column(name = "industry_name", length = 100)
    private String industryName; // 직종명

    @Column(name = "career_years")
    private Integer careerYears; // 재직 기간 (0: 1년미만, 1: 1-3년, 3: 3-5년, 5: 5-10년, 10: 10-20년, 20: 20-30년, 30: 30년이상)

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_level")
    private AssetLevel assetLevel; // 자산 수준

    @Column(name = "created_date", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "updated_date")
    @Builder.Default
    private LocalDateTime updatedDate = LocalDateTime.now();

    // 외래 키 제약조건 제거 - 단순히 ID로만 연결
    // @OneToOne
    // @JoinColumn(name = "customer_id", referencedColumnName = "user_id")
    // private User user;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "industry_code", referencedColumnName = "industry_code", insertable = false, updatable = false)
    // private IndustryCode industry;

    /**
     * 자산 수준 enum 정의
     */
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
                default: return FROM_1_TO_5; // 기본값
            }
        }
    }

    /**
     * 질병 보유 여부 확인
     */
    public boolean hasDiseaseHistory() {
        return Boolean.TRUE.equals(majorDisease);
    }

    /**
     * 건강상 위험 요소 확인
     */
    public boolean hasHealthRisk() {
        return Boolean.TRUE.equals(recentMedicalAdvice) || 
               Boolean.TRUE.equals(recentHospitalization) ||
               Boolean.TRUE.equals(longTermMedication) ||
               Boolean.TRUE.equals(disabilityRegistered);
    }

    /**
     * 업데이트 시간 갱신
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}
