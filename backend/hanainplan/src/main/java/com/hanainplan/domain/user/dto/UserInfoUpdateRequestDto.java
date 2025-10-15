package com.hanainplan.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

import java.util.List;

@Schema(description = "사용자 정보 업데이트 요청 DTO")
public class UserInfoUpdateRequestDto {

    @Schema(description = "사용자 기본 정보 업데이트")
    private UserBasicInfoUpdate userBasicInfo;

    @Schema(description = "고객 상세 정보 업데이트 (일반 고객인 경우)")
    private CustomerDetailInfoUpdate customerDetailInfo;

    public UserInfoUpdateRequestDto() {}

    public UserBasicInfoUpdate getUserBasicInfo() {
        return userBasicInfo;
    }

    public void setUserBasicInfo(UserBasicInfoUpdate userBasicInfo) {
        this.userBasicInfo = userBasicInfo;
    }

    public CustomerDetailInfoUpdate getCustomerDetailInfo() {
        return customerDetailInfo;
    }

    public void setCustomerDetailInfo(CustomerDetailInfoUpdate customerDetailInfo) {
        this.customerDetailInfo = customerDetailInfo;
    }

    @Schema(description = "사용자 기본 정보 업데이트")
    public static class UserBasicInfoUpdate {
        @Schema(description = "사용자 이름")
        private String userName;

        @Schema(description = "전화번호")
        @Pattern(regexp = "^01[016789]\\d{7,8}$|^01[016789]-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다")
        private String phoneNumber;

        @Schema(description = "이메일")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        private String email;

        public UserBasicInfoUpdate() {}

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }

        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    @Schema(description = "고객 상세 정보 업데이트")
    public static class CustomerDetailInfoUpdate {
        @Schema(description = "건강 정보 업데이트")
        private HealthInfoUpdate healthInfo;

        @Schema(description = "직업 정보 업데이트")
        private JobInfoUpdate jobInfo;

        public CustomerDetailInfoUpdate() {}

        public HealthInfoUpdate getHealthInfo() { return healthInfo; }
        public void setHealthInfo(HealthInfoUpdate healthInfo) { this.healthInfo = healthInfo; }

        public JobInfoUpdate getJobInfo() { return jobInfo; }
        public void setJobInfo(JobInfoUpdate jobInfo) { this.jobInfo = jobInfo; }
    }

    @Schema(description = "건강 정보 업데이트")
    public static class HealthInfoUpdate {
        @Schema(description = "최근 의료진 상담 여부")
        private Boolean recentMedicalAdvice;

        @Schema(description = "최근 입원 여부")
        private Boolean recentHospitalization;

        @Schema(description = "주요 질병 여부")
        private Boolean majorDisease;

        @Schema(description = "질병 상세 정보")
        private List<DiseaseDetailUpdate> diseaseDetails;

        @Schema(description = "장기 복용 약물 여부")
        private Boolean longTermMedication;

        @Schema(description = "장애 등록 여부")
        private Boolean disabilityRegistered;

        @Schema(description = "보험 거절 이력 여부")
        private Boolean insuranceRejection;

        public HealthInfoUpdate() {}

        public Boolean getRecentMedicalAdvice() { return recentMedicalAdvice; }
        public void setRecentMedicalAdvice(Boolean recentMedicalAdvice) { this.recentMedicalAdvice = recentMedicalAdvice; }

        public Boolean getRecentHospitalization() { return recentHospitalization; }
        public void setRecentHospitalization(Boolean recentHospitalization) { this.recentHospitalization = recentHospitalization; }

        public Boolean getMajorDisease() { return majorDisease; }
        public void setMajorDisease(Boolean majorDisease) { this.majorDisease = majorDisease; }

        public List<DiseaseDetailUpdate> getDiseaseDetails() { return diseaseDetails; }
        public void setDiseaseDetails(List<DiseaseDetailUpdate> diseaseDetails) { this.diseaseDetails = diseaseDetails; }

        public Boolean getLongTermMedication() { return longTermMedication; }
        public void setLongTermMedication(Boolean longTermMedication) { this.longTermMedication = longTermMedication; }

        public Boolean getDisabilityRegistered() { return disabilityRegistered; }
        public void setDisabilityRegistered(Boolean disabilityRegistered) { this.disabilityRegistered = disabilityRegistered; }

        public Boolean getInsuranceRejection() { return insuranceRejection; }
        public void setInsuranceRejection(Boolean insuranceRejection) { this.insuranceRejection = insuranceRejection; }
    }

    @Schema(description = "질병 상세 정보 업데이트")
    public static class DiseaseDetailUpdate {
        @Schema(description = "질병 코드")
        private String diseaseCode;

        @Schema(description = "질병명")
        private String diseaseName;

        @Schema(description = "질병 카테고리")
        private String diseaseCategory;

        @Schema(description = "위험도")
        private String riskLevel;

        @Schema(description = "심각도")
        private String severity;

        @Schema(description = "진행 기간")
        private String progressPeriod;

        @Schema(description = "만성 여부")
        private Boolean isChronic;

        @Schema(description = "설명")
        private String description;

        public DiseaseDetailUpdate() {}

        public String getDiseaseCode() { return diseaseCode; }
        public void setDiseaseCode(String diseaseCode) { this.diseaseCode = diseaseCode; }

        public String getDiseaseName() { return diseaseName; }
        public void setDiseaseName(String diseaseName) { this.diseaseName = diseaseName; }

        public String getDiseaseCategory() { return diseaseCategory; }
        public void setDiseaseCategory(String diseaseCategory) { this.diseaseCategory = diseaseCategory; }

        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }

        public String getProgressPeriod() { return progressPeriod; }
        public void setProgressPeriod(String progressPeriod) { this.progressPeriod = progressPeriod; }

        public Boolean getIsChronic() { return isChronic; }
        public void setIsChronic(Boolean isChronic) { this.isChronic = isChronic; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    @Schema(description = "직업 정보 업데이트")
    public static class JobInfoUpdate {
        @Schema(description = "산업 코드")
        private String industryCode;

        @Schema(description = "산업명")
        private String industryName;

        @Schema(description = "경력 연수")
        private Integer careerYears;

        @Schema(description = "자산 수준", allowableValues = {"LOW", "MEDIUM", "HIGH", "VERY_HIGH"})
        private String assetLevel;

        public JobInfoUpdate() {}

        public String getIndustryCode() { return industryCode; }
        public void setIndustryCode(String industryCode) { this.industryCode = industryCode; }

        public String getIndustryName() { return industryName; }
        public void setIndustryName(String industryName) { this.industryName = industryName; }

        public Integer getCareerYears() { return careerYears; }
        public void setCareerYears(Integer careerYears) { this.careerYears = careerYears; }

        public String getAssetLevel() { return assetLevel; }
        public void setAssetLevel(String assetLevel) { this.assetLevel = assetLevel; }
    }
}