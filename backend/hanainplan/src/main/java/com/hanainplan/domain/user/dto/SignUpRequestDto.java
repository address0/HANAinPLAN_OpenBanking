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

/**
 * 회원가입 요청 DTO
 */
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
    private String ci; // 실명인증 CI값

    private String password; // 카카오 로그인시 null 가능

    private String confirmPassword;

    private String kakaoId; // 카카오 OAuth ID

    private String email; // 카카오 로그인시 이메일

    // 일반고객 전용 필드들
    private HealthInfoDto healthInfo;
    private JobInfoDto jobInfo;

    // 상담원 전용 필드들 (현재는 기본 정보만)
    // 추후 상담원 가입 폼 구현시 추가 예정

    /**
     * 건강 정보 DTO
     */
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

    /**
     * 질병 상세 정보 DTO
     */
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

    /**
     * 직업 정보 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobInfoDto {
        private String industryCode;
        private String industryName;
        private Integer careerYears;
        private String assetLevel;
    }

    /**
     * 일반고객 여부 확인
     */
    public boolean isGeneralCustomer() {
        return UserType.GENERAL.equals(userType);
    }

    /**
     * 상담원 여부 확인
     */
    public boolean isCounselor() {
        return UserType.COUNSELOR.equals(userType);
    }

    /**
     * 카카오 로그인 여부 확인
     */
    public boolean isKakaoLogin() {
        return kakaoId != null && !kakaoId.trim().isEmpty();
    }
}
