package com.hanainplan.domain.user.entity;

import com.hanainplan.domain.common.entity.BranchCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 상담원 엔터티
 * - 하나인플랜 서비스 상담원 정보 관리
 * - 상담 배정 및 평가에 사용
 */
@Entity
@Table(name = "tb_consultant")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consultant {

    @Id
    @Column(name = "consultant_id")
    private Long consultantId;

    // 직원 정보
    @Column(name = "employee_id", length = 20, unique = true)
    private String employeeId; // 직원번호

    @Column(name = "department", length = 50)
    private String department; // 소속 부서

    @Column(name = "position", length = 50)
    private String position; // 직급

    @Column(name = "branch_code", length = 10)
    private String branchCode; // 지점 코드

    @Column(name = "branch_name", length = 100)
    private String branchName; // 지점명

    // 자격증 정보
    @Column(name = "license_type", length = 50)
    private String licenseType; // 자격증 종류

    @Column(name = "license_number", length = 50)
    private String licenseNumber; // 자격증 번호

    @Column(name = "license_issue_date")
    private LocalDate licenseIssueDate; // 자격증 발급일

    @Column(name = "license_expiry_date")
    private LocalDate licenseExpiryDate; // 자격증 만료일

    // 전문 분야 (JSON으로 저장)
    @Column(name = "specialization", columnDefinition = "JSON")
    private String specialization; // 전문 분야 배열

    // 근무 정보
    @Column(name = "hire_date")
    private LocalDate hireDate; // 입사일

    @Enumerated(EnumType.STRING)
    @Column(name = "work_status")
    @Builder.Default
    private WorkStatus workStatus = WorkStatus.ACTIVE; // 근무 상태

    // 상담 관련 정보
    @Column(name = "max_daily_consultations")
    @Builder.Default
    private Integer maxDailyConsultations = 10; // 일일 최대 상담 가능 건수

    @Column(name = "consultation_rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal consultationRating = BigDecimal.ZERO; // 상담 평점

    @Column(name = "total_consultations")
    @Builder.Default
    private Integer totalConsultations = 0; // 총 상담 건수

    // 연락처 정보
    @Column(name = "office_phone", length = 15)
    private String officePhone; // 사무실 전화번호

    @Column(name = "extension", length = 5)
    private String extension; // 내선번호

    @Column(name = "work_email", length = 100)
    private String workEmail; // 업무용 이메일

    @Column(name = "created_date", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "updated_date")
    @Builder.Default
    private LocalDateTime updatedDate = LocalDateTime.now();

    // 외래 키 제약조건 제거 - 단순히 ID로만 연결
    // @OneToOne
    // @JoinColumn(name = "consultant_id", referencedColumnName = "user_id")
    // private User user;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "branch_code", referencedColumnName = "branch_code", insertable = false, updatable = false)
    // private BranchCode branch;

    /**
     * 근무 상태 enum 정의
     */
    public enum WorkStatus {
        ACTIVE("활성"), INACTIVE("비활성"), ON_LEAVE("휴직");

        private final String description;

        WorkStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 전문 분야 enum 정의
     */
    public enum SpecializationType {
        LIFE_INSURANCE("생명보험"), 
        NON_LIFE_INSURANCE("손해보험"), 
        PENSION_INSURANCE("연금보험"),
        SAVINGS_INSURANCE("저축보험"),
        INVESTMENT("투자상품"),
        LOAN("대출상품");

        private final String description;

        SpecializationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 자격증 유효성 확인
     */
    public boolean isLicenseValid() {
        return licenseExpiryDate != null && licenseExpiryDate.isAfter(LocalDate.now());
    }

    /**
     * 높은 평점 상담원 여부 확인 (4.0 이상)
     */
    public boolean isHighRatedConsultant() {
        return consultationRating != null && consultationRating.compareTo(new BigDecimal("4.0")) >= 0;
    }

    /**
     * 경력 년수 계산
     */
    public int calculateExperienceYears() {
        if (hireDate == null) return 0;
        return LocalDate.now().getYear() - hireDate.getYear();
    }

    /**
     * 시니어 상담원 여부 확인 (5년 이상 경력)
     */
    public boolean isSeniorConsultant() {
        return calculateExperienceYears() >= 5;
    }

    /**
     * 평점 업데이트
     */
    public void updateRating(BigDecimal newRating) {
        if (newRating != null && newRating.compareTo(BigDecimal.ZERO) >= 0 && newRating.compareTo(new BigDecimal("5.0")) <= 0) {
            this.consultationRating = newRating;
            this.updatedDate = LocalDateTime.now();
        }
    }

    /**
     * 상담 건수 증가
     */
    public void incrementConsultationCount() {
        this.totalConsultations = (this.totalConsultations != null) ? this.totalConsultations + 1 : 1;
        this.updatedDate = LocalDateTime.now();
    }

    /**
     * 업데이트 시간 갱신
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}
