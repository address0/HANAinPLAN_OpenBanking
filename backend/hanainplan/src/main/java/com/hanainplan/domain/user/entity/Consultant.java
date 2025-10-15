package com.hanainplan.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Column(name = "employee_id", length = 20, unique = true)
    private String employeeId;

    @Column(name = "department", length = 50)
    private String department;

    @Column(name = "position", length = 50)
    private String position;

    @Column(name = "branch_code", length = 10)
    private String branchCode;

    @Column(name = "branch_name", length = 100)
    private String branchName;

    @Column(name = "branch_address", length = 300)
    private String branchAddress;

    @Column(name = "branch_latitude", precision = 10, scale = 6)
    private BigDecimal branchLatitude;

    @Column(name = "branch_longitude", precision = 10, scale = 6)
    private BigDecimal branchLongitude;

    @Column(name = "license_type", length = 50)
    private String licenseType;

    @Column(name = "license_number", length = 50)
    private String licenseNumber;

    @Column(name = "license_issue_date")
    private LocalDate licenseIssueDate;

    @Column(name = "license_expiry_date")
    private LocalDate licenseExpiryDate;

    @Column(name = "specialization", columnDefinition = "JSON")
    private String specialization;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_status")
    @Builder.Default
    private WorkStatus workStatus = WorkStatus.ACTIVE;

    @Column(name = "max_daily_consultations")
    @Builder.Default
    private Integer maxDailyConsultations = 10;

    @Column(name = "consultation_rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal consultationRating = BigDecimal.ZERO;

    @Column(name = "total_consultations")
    @Builder.Default
    private Integer totalConsultations = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "consultation_status")
    @Builder.Default
    private ConsultationStatus consultationStatus = ConsultationStatus.OFFLINE;

    @Column(name = "office_phone", length = 15)
    private String officePhone;

    @Column(name = "extension", length = 5)
    private String extension;

    @Column(name = "work_email", length = 100)
    private String workEmail;

    @Column(name = "created_date", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "updated_date")
    @Builder.Default
    private LocalDateTime updatedDate = LocalDateTime.now();

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

    public enum ConsultationStatus {
        AVAILABLE("상담 가능"), BUSY("상담 중"), OFFLINE("오프라인");

        private final String description;

        ConsultationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

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

    public boolean isLicenseValid() {
        return licenseExpiryDate != null && licenseExpiryDate.isAfter(LocalDate.now());
    }

    public boolean isHighRatedConsultant() {
        return consultationRating != null && consultationRating.compareTo(new BigDecimal("4.0")) >= 0;
    }

    public int calculateExperienceYears() {
        if (hireDate == null) return 0;
        return LocalDate.now().getYear() - hireDate.getYear();
    }

    public boolean isSeniorConsultant() {
        return calculateExperienceYears() >= 5;
    }

    public void updateRating(BigDecimal newRating) {
        if (newRating != null && newRating.compareTo(BigDecimal.ZERO) >= 0 && newRating.compareTo(new BigDecimal("5.0")) <= 0) {
            this.consultationRating = newRating;
            this.updatedDate = LocalDateTime.now();
        }
    }

    public void incrementConsultationCount() {
        this.totalConsultations = (this.totalConsultations != null) ? this.totalConsultations + 1 : 1;
        this.updatedDate = LocalDateTime.now();
    }

    public void updateConsultationStatus(ConsultationStatus status) {
        this.consultationStatus = status;
        this.updatedDate = LocalDateTime.now();
    }

    public boolean isAvailableForConsultation() {
        return this.workStatus == WorkStatus.ACTIVE 
                && this.consultationStatus == ConsultationStatus.AVAILABLE;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}