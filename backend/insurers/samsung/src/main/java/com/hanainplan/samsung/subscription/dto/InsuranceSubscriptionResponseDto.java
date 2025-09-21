package com.hanainplan.samsung.subscription.dto;

import com.hanainplan.samsung.subscription.entity.InsuranceSubscription;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class InsuranceSubscriptionResponseDto {
    private Long subscriptionId;
    private String customerCi;
    private String productCode;
    private String productType;
    private String policyNumber;
    private LocalDate subscriptionDate;
    private LocalDate maturityDate;
    private BigDecimal premiumAmount;
    private String paymentFrequency;
    private BigDecimal coverageAmount;
    private String beneficiaryName;
    private String beneficiaryRelation;
    private String paymentMethod;
    private String paymentAccount;
    private String subscriptionStatus;
    private String riskAssessment;
    private Boolean medicalExamRequired;
    private LocalDate medicalExamDate;
    private String medicalExamResult;
    private String agentCode;
    private String branchCode;
    private String specialConditions;
    private String exclusions;
    private Integer waitingPeriod;
    private Integer gracePeriod;
    private Boolean renewalOption;
    private Boolean automaticRenewal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static InsuranceSubscriptionResponseDto fromEntity(InsuranceSubscription entity) {
        InsuranceSubscriptionResponseDto dto = new InsuranceSubscriptionResponseDto();
        dto.setSubscriptionId(entity.getSubscriptionId());
        dto.setCustomerCi(entity.getCustomerCi());
        dto.setProductCode(entity.getProductCode());
        dto.setProductType(entity.getProductType());
        dto.setPolicyNumber(entity.getPolicyNumber());
        dto.setSubscriptionDate(entity.getSubscriptionDate());
        dto.setMaturityDate(entity.getMaturityDate());
        dto.setPremiumAmount(entity.getPremiumAmount());
        dto.setPaymentFrequency(entity.getPaymentFrequency());
        dto.setCoverageAmount(entity.getCoverageAmount());
        dto.setBeneficiaryName(entity.getBeneficiaryName());
        dto.setBeneficiaryRelation(entity.getBeneficiaryRelation());
        dto.setPaymentMethod(entity.getPaymentMethod());
        dto.setPaymentAccount(entity.getPaymentAccount());
        dto.setSubscriptionStatus(entity.getSubscriptionStatus());
        dto.setRiskAssessment(entity.getRiskAssessment());
        dto.setMedicalExamRequired(entity.getMedicalExamRequired());
        dto.setMedicalExamDate(entity.getMedicalExamDate());
        dto.setMedicalExamResult(entity.getMedicalExamResult());
        dto.setAgentCode(entity.getAgentCode());
        dto.setBranchCode(entity.getBranchCode());
        dto.setSpecialConditions(entity.getSpecialConditions());
        dto.setExclusions(entity.getExclusions());
        dto.setWaitingPeriod(entity.getWaitingPeriod());
        dto.setGracePeriod(entity.getGracePeriod());
        dto.setRenewalOption(entity.getRenewalOption());
        dto.setAutomaticRenewal(entity.getAutomaticRenewal());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
