package com.hanainplan.domain.user.dto;

import com.hanainplan.domain.user.entity.User.UserType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequestDto {

    @NotNull(message = "사용자 타입은 필수입니다.")
    private UserType userType;

    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 2, max = 50, message = "이름은 2-50자 사이여야 합니다.")
    private String name;

    @NotBlank(message = "주민번호는 필수입니다.")
    @Pattern(regexp = "^\\d{13}$", message = "주민번호 형식이 올바르지 않습니다. (13자리 숫자)")
    private String socialNumber;

    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다. (010-0000-0000)")
    private String phoneNumber;

    @NotBlank(message = "인증번호는 필수입니다.")
    private String verificationCode;

    @NotBlank(message = "CI값은 필수입니다.")
    private String ci;

    private String password;

    private String confirmPassword;

    private String kakaoId;

    private String email;

    private HealthInfoDto healthInfo;
    private JobInfoDto jobInfo;

    private CounselorInfoDto counselorInfo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HealthInfoDto {
        private Boolean recentMedicalAdvice;
        private Boolean recentHospitalization;
        private Boolean majorDisease;
        private List<DiseaseDetailDto> diseaseDetails;
        private Boolean longTermMedication;
        private Boolean disabilityRegistered;
        private Boolean insuranceRejection;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiseaseDetailDto {
        private String diseaseCode;
        private String diseaseName;
        private String diseaseCategory;
        private String riskLevel;
        private String severity;
        private String progressPeriod;
        private Boolean isChronic;
        private String description;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobInfoDto {
        private String industryCode;
        private String industryName;
        private Integer careerYears;
        private String assetLevel;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CounselorInfoDto {
        private String employeeId;

        private String specialty;
        private String position;
        private String workPhoneNumber;
        private String workEmail;

        private String branchCode;
        private String branchName;
        private String branchAddress;
        private Double branchLatitude;
        private Double branchLongitude;

        private String additionalNotes;
    }

    public boolean isGeneralCustomer() {
        return UserType.GENERAL.equals(userType);
    }

    public boolean isCounselor() {
        return UserType.COUNSELOR.equals(userType);
    }

    public boolean isKakaoLogin() {
        return kakaoId != null && !kakaoId.trim().isEmpty();
    }
}