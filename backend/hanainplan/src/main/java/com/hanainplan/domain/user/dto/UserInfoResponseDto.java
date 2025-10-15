package com.hanainplan.domain.user.dto;

import com.hanainplan.domain.user.entity.User;
import com.hanainplan.domain.user.entity.Customer;
import com.hanainplan.domain.user.entity.CustomerDiseaseDetail;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "사용자 정보 응답 DTO")
public class UserInfoResponseDto {

    @Schema(description = "사용자 기본 정보")
    private UserBasicInfo userBasicInfo;

    @Schema(description = "고객 상세 정보 (일반 고객인 경우)")
    private CustomerDetailInfo customerDetailInfo;

    public UserInfoResponseDto() {}

    public UserInfoResponseDto(User user, Customer customer, List<CustomerDiseaseDetail> diseaseDetails) {
        this.userBasicInfo = new UserBasicInfo(user);
        if (customer != null) {
            this.customerDetailInfo = new CustomerDetailInfo(customer, diseaseDetails);
        }
    }

    public UserBasicInfo getUserBasicInfo() {
        return userBasicInfo;
    }

    public void setUserBasicInfo(UserBasicInfo userBasicInfo) {
        this.userBasicInfo = userBasicInfo;
    }

    public CustomerDetailInfo getCustomerDetailInfo() {
        return customerDetailInfo;
    }

    public void setCustomerDetailInfo(CustomerDetailInfo customerDetailInfo) {
        this.customerDetailInfo = customerDetailInfo;
    }

    @Schema(description = "사용자 기본 정보")
    public static class UserBasicInfo {
        @Schema(description = "사용자 ID")
        private Long userId;

        @Schema(description = "사용자 이름")
        private String userName;

        @Schema(description = "전화번호")
        private String phoneNumber;

        @Schema(description = "이메일")
        private String email;

        @Schema(description = "사용자 타입")
        private String userType;

        @Schema(description = "성별")
        private String gender;

        @Schema(description = "생년월일")
        private String birthDate;

        @Schema(description = "로그인 타입")
        private String loginType;

        @Schema(description = "전화번호 인증 여부")
        private Boolean isPhoneVerified;

        @Schema(description = "계정 활성화 여부")
        private Boolean isActive;

        @Schema(description = "가입일")
        private LocalDateTime createdDate;

        @Schema(description = "마지막 로그인")
        private LocalDateTime lastLoginDate;

        public UserBasicInfo() {}

        public UserBasicInfo(User user) {
            this.userId = user.getUserId();
            this.userName = user.getUserName();
            this.phoneNumber = user.getPhoneNumber();
            this.email = user.getEmail();
            this.userType = user.getUserType() != null ? user.getUserType().toString() : null;
            this.gender = user.getGender() != null ? user.getGender().toString() : null;
            this.birthDate = user.getBirthDate() != null ? user.getBirthDate().toString() : null;
            this.loginType = user.getLoginType() != null ? user.getLoginType().toString() : null;
            this.isPhoneVerified = user.getIsPhoneVerified();
            this.isActive = user.getIsActive();
            this.createdDate = user.getCreatedDate();
            this.lastLoginDate = user.getLastLoginDate();
        }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }

        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getUserType() { return userType; }
        public void setUserType(String userType) { this.userType = userType; }

        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }

        public String getBirthDate() { return birthDate; }
        public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

        public String getLoginType() { return loginType; }
        public void setLoginType(String loginType) { this.loginType = loginType; }

        public Boolean getIsPhoneVerified() { return isPhoneVerified; }
        public void setIsPhoneVerified(Boolean isPhoneVerified) { this.isPhoneVerified = isPhoneVerified; }

        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }

        public LocalDateTime getCreatedDate() { return createdDate; }
        public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

        public LocalDateTime getLastLoginDate() { return lastLoginDate; }
        public void setLastLoginDate(LocalDateTime lastLoginDate) { this.lastLoginDate = lastLoginDate; }
    }

    @Schema(description = "고객 상세 정보")
    public static class CustomerDetailInfo {
        @Schema(description = "고객 ID")
        private Long customerId;

        @Schema(description = "건강 정보")
        private HealthInfo healthInfo;

        @Schema(description = "직업 정보")
        private JobInfo jobInfo;

        public CustomerDetailInfo() {}

        public CustomerDetailInfo(Customer customer, List<CustomerDiseaseDetail> diseaseDetails) {
            this.customerId = customer.getCustomerId();
            this.healthInfo = new HealthInfo(customer, diseaseDetails);
            this.jobInfo = new JobInfo(customer);
        }

        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }

        public HealthInfo getHealthInfo() { return healthInfo; }
        public void setHealthInfo(HealthInfo healthInfo) { this.healthInfo = healthInfo; }

        public JobInfo getJobInfo() { return jobInfo; }
        public void setJobInfo(JobInfo jobInfo) { this.jobInfo = jobInfo; }
    }

    @Schema(description = "건강 정보")
    public static class HealthInfo {
        @Schema(description = "최근 의료진 상담 여부")
        private Boolean recentMedicalAdvice;

        @Schema(description = "최근 입원 여부")
        private Boolean recentHospitalization;

        @Schema(description = "주요 질병 여부")
        private Boolean majorDisease;

        @Schema(description = "질병 상세 정보")
        private List<DiseaseDetail> diseaseDetails;

        @Schema(description = "장기 복용 약물 여부")
        private Boolean longTermMedication;

        @Schema(description = "장애 등록 여부")
        private Boolean disabilityRegistered;

        @Schema(description = "보험 거절 이력 여부")
        private Boolean insuranceRejection;

        public HealthInfo() {}

        public HealthInfo(Customer customer, List<CustomerDiseaseDetail> diseaseDetails) {
            this.recentMedicalAdvice = customer.getRecentMedicalAdvice();
            this.recentHospitalization = customer.getRecentHospitalization();
            this.majorDisease = customer.getMajorDisease();
            this.longTermMedication = customer.getLongTermMedication();
            this.disabilityRegistered = customer.getDisabilityRegistered();
            this.insuranceRejection = customer.getInsuranceRejection();

            if (diseaseDetails != null) {
                this.diseaseDetails = diseaseDetails.stream()
                    .map(DiseaseDetail::new)
                    .collect(Collectors.toList());
            }
        }

        public Boolean getRecentMedicalAdvice() { return recentMedicalAdvice; }
        public void setRecentMedicalAdvice(Boolean recentMedicalAdvice) { this.recentMedicalAdvice = recentMedicalAdvice; }

        public Boolean getRecentHospitalization() { return recentHospitalization; }
        public void setRecentHospitalization(Boolean recentHospitalization) { this.recentHospitalization = recentHospitalization; }

        public Boolean getMajorDisease() { return majorDisease; }
        public void setMajorDisease(Boolean majorDisease) { this.majorDisease = majorDisease; }

        public List<DiseaseDetail> getDiseaseDetails() { return diseaseDetails; }
        public void setDiseaseDetails(List<DiseaseDetail> diseaseDetails) { this.diseaseDetails = diseaseDetails; }

        public Boolean getLongTermMedication() { return longTermMedication; }
        public void setLongTermMedication(Boolean longTermMedication) { this.longTermMedication = longTermMedication; }

        public Boolean getDisabilityRegistered() { return disabilityRegistered; }
        public void setDisabilityRegistered(Boolean disabilityRegistered) { this.disabilityRegistered = disabilityRegistered; }

        public Boolean getInsuranceRejection() { return insuranceRejection; }
        public void setInsuranceRejection(Boolean insuranceRejection) { this.insuranceRejection = insuranceRejection; }
    }

    @Schema(description = "질병 상세 정보")
    public static class DiseaseDetail {
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

        public DiseaseDetail() {}

        public DiseaseDetail(CustomerDiseaseDetail detail) {
            this.diseaseCode = detail.getDiseaseCode();
            this.diseaseName = detail.getDiseaseName();
            this.diseaseCategory = detail.getDiseaseCategory();
            this.riskLevel = detail.getRiskLevel();
            this.severity = detail.getSeverity();
            this.progressPeriod = detail.getProgressPeriod();
            this.isChronic = detail.getIsChronic();
            this.description = detail.getDescription();
        }

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

    @Schema(description = "직업 정보")
    public static class JobInfo {
        @Schema(description = "산업 코드")
        private String industryCode;

        @Schema(description = "산업명")
        private String industryName;

        @Schema(description = "경력 연수")
        private Integer careerYears;

        @Schema(description = "자산 수준")
        private String assetLevel;

        public JobInfo() {}

        public JobInfo(Customer customer) {
            this.industryCode = customer.getIndustryCode();
            this.industryName = customer.getIndustryName();
            this.careerYears = customer.getCareerYears();
            this.assetLevel = customer.getAssetLevel() != null ? customer.getAssetLevel().toString() : null;
        }

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