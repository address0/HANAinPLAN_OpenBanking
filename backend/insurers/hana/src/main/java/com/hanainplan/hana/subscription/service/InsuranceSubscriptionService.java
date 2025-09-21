package com.hanainplan.hana.subscription.service;

import com.hanainplan.hana.subscription.dto.InsuranceSubscriptionRequestDto;
import com.hanainplan.hana.subscription.dto.InsuranceSubscriptionResponseDto;
import com.hanainplan.hana.subscription.entity.InsuranceSubscription;
import com.hanainplan.hana.subscription.repository.InsuranceSubscriptionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InsuranceSubscriptionService {

    private final InsuranceSubscriptionRepository subscriptionRepository;

    @Transactional
    public InsuranceSubscriptionResponseDto createSubscription(InsuranceSubscriptionRequestDto requestDto) {
        // 증권번호 중복 확인
        if (subscriptionRepository.existsByPolicyNumber(requestDto.getPolicyNumber())) {
            throw new IllegalArgumentException("이미 존재하는 증권번호입니다: " + requestDto.getPolicyNumber());
        }

        InsuranceSubscription subscription = InsuranceSubscription.builder()
                .customerCi(requestDto.getCustomerCi())
                .productCode(requestDto.getProductCode())
                .productType(requestDto.getProductType())
                .policyNumber(requestDto.getPolicyNumber())
                .subscriptionDate(requestDto.getSubscriptionDate())
                .maturityDate(requestDto.getMaturityDate())
                .premiumAmount(requestDto.getPremiumAmount())
                .paymentFrequency(requestDto.getPaymentFrequency())
                .coverageAmount(requestDto.getCoverageAmount())
                .beneficiaryName(requestDto.getBeneficiaryName())
                .beneficiaryRelation(requestDto.getBeneficiaryRelation())
                .paymentMethod(requestDto.getPaymentMethod())
                .paymentAccount(requestDto.getPaymentAccount())
                .subscriptionStatus(requestDto.getSubscriptionStatus() != null ? requestDto.getSubscriptionStatus() : "ACTIVE")
                .riskAssessment(requestDto.getRiskAssessment())
                .medicalExamRequired(requestDto.getMedicalExamRequired())
                .medicalExamDate(requestDto.getMedicalExamDate())
                .medicalExamResult(requestDto.getMedicalExamResult())
                .agentCode(requestDto.getAgentCode())
                .branchCode(requestDto.getBranchCode())
                .specialConditions(requestDto.getSpecialConditions())
                .exclusions(requestDto.getExclusions())
                .waitingPeriod(requestDto.getWaitingPeriod())
                .gracePeriod(requestDto.getGracePeriod())
                .renewalOption(requestDto.getRenewalOption())
                .automaticRenewal(requestDto.getAutomaticRenewal())
                .build();

        InsuranceSubscription savedSubscription = subscriptionRepository.save(subscription);
        return InsuranceSubscriptionResponseDto.fromEntity(savedSubscription);
    }

    @Transactional(readOnly = true)
    public InsuranceSubscriptionResponseDto getSubscriptionByPolicyNumber(String policyNumber) {
        InsuranceSubscription subscription = subscriptionRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new IllegalArgumentException("보험 가입을 찾을 수 없습니다. 증권번호: " + policyNumber));
        return InsuranceSubscriptionResponseDto.fromEntity(subscription);
    }

    @Transactional(readOnly = true)
    public InsuranceSubscriptionResponseDto getSubscriptionById(Long subscriptionId) {
        InsuranceSubscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("보험 가입을 찾을 수 없습니다. ID: " + subscriptionId));
        return InsuranceSubscriptionResponseDto.fromEntity(subscription);
    }

    @Transactional(readOnly = true)
    public List<InsuranceSubscriptionResponseDto> getAllSubscriptions() {
        return subscriptionRepository.findAll().stream()
                .map(InsuranceSubscriptionResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InsuranceSubscriptionResponseDto> getSubscriptionsByCustomer(String customerCi) {
        return subscriptionRepository.findByCustomerCi(customerCi).stream()
                .map(InsuranceSubscriptionResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InsuranceSubscriptionResponseDto> getActiveSubscriptionsByCustomer(String customerCi) {
        return subscriptionRepository.findByCustomerCiAndStatus(customerCi, "ACTIVE").stream()
                .map(InsuranceSubscriptionResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InsuranceSubscriptionResponseDto> getSubscriptionsByProduct(String productCode) {
        return subscriptionRepository.findByProductCode(productCode).stream()
                .map(InsuranceSubscriptionResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InsuranceSubscriptionResponseDto> getSubscriptionsByStatus(String status) {
        return subscriptionRepository.findBySubscriptionStatus(status).stream()
                .map(InsuranceSubscriptionResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InsuranceSubscriptionResponseDto> getSubscriptionsByAgent(String agentCode) {
        return subscriptionRepository.findByAgentCode(agentCode).stream()
                .map(InsuranceSubscriptionResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InsuranceSubscriptionResponseDto> getSubscriptionsByBranch(String branchCode) {
        return subscriptionRepository.findByBranchCode(branchCode).stream()
                .map(InsuranceSubscriptionResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InsuranceSubscriptionResponseDto> getSubscriptionsByDateRange(LocalDate startDate, LocalDate endDate) {
        return subscriptionRepository.findBySubscriptionDateBetween(startDate, endDate).stream()
                .map(InsuranceSubscriptionResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InsuranceSubscriptionResponseDto> getExpiredSubscriptions() {
        return subscriptionRepository.findExpiredSubscriptions(LocalDate.now()).stream()
                .map(InsuranceSubscriptionResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InsuranceSubscriptionResponseDto> getPendingMedicalExams() {
        return subscriptionRepository.findPendingMedicalExams().stream()
                .map(InsuranceSubscriptionResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InsuranceSubscriptionResponseDto> getAutoRenewalSubscriptions() {
        return subscriptionRepository.findAutoRenewalSubscriptions().stream()
                .map(InsuranceSubscriptionResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Long getActiveSubscriptionCountByCustomer(String customerCi) {
        return subscriptionRepository.countActiveSubscriptionsByCustomer(customerCi);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalPremiumByCustomer(String customerCi) {
        BigDecimal total = subscriptionRepository.getTotalPremiumByCustomer(customerCi);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional
    public InsuranceSubscriptionResponseDto updateSubscription(String policyNumber, InsuranceSubscriptionRequestDto requestDto) {
        InsuranceSubscription subscription = subscriptionRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new IllegalArgumentException("보험 가입을 찾을 수 없습니다. 증권번호: " + policyNumber));

        // 증권번호는 변경 불가
        subscription.setCustomerCi(requestDto.getCustomerCi());
        subscription.setProductCode(requestDto.getProductCode());
        subscription.setProductType(requestDto.getProductType());
        subscription.setSubscriptionDate(requestDto.getSubscriptionDate());
        subscription.setMaturityDate(requestDto.getMaturityDate());
        subscription.setPremiumAmount(requestDto.getPremiumAmount());
        subscription.setPaymentFrequency(requestDto.getPaymentFrequency());
        subscription.setCoverageAmount(requestDto.getCoverageAmount());
        subscription.setBeneficiaryName(requestDto.getBeneficiaryName());
        subscription.setBeneficiaryRelation(requestDto.getBeneficiaryRelation());
        subscription.setPaymentMethod(requestDto.getPaymentMethod());
        subscription.setPaymentAccount(requestDto.getPaymentAccount());
        subscription.setSubscriptionStatus(requestDto.getSubscriptionStatus());
        subscription.setRiskAssessment(requestDto.getRiskAssessment());
        subscription.setMedicalExamRequired(requestDto.getMedicalExamRequired());
        subscription.setMedicalExamDate(requestDto.getMedicalExamDate());
        subscription.setMedicalExamResult(requestDto.getMedicalExamResult());
        subscription.setAgentCode(requestDto.getAgentCode());
        subscription.setBranchCode(requestDto.getBranchCode());
        subscription.setSpecialConditions(requestDto.getSpecialConditions());
        subscription.setExclusions(requestDto.getExclusions());
        subscription.setWaitingPeriod(requestDto.getWaitingPeriod());
        subscription.setGracePeriod(requestDto.getGracePeriod());
        subscription.setRenewalOption(requestDto.getRenewalOption());
        subscription.setAutomaticRenewal(requestDto.getAutomaticRenewal());

        InsuranceSubscription updatedSubscription = subscriptionRepository.save(subscription);
        return InsuranceSubscriptionResponseDto.fromEntity(updatedSubscription);
    }

    @Transactional
    public InsuranceSubscriptionResponseDto updateSubscriptionStatus(String policyNumber, String status) {
        InsuranceSubscription subscription = subscriptionRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new IllegalArgumentException("보험 가입을 찾을 수 없습니다. 증권번호: " + policyNumber));
        
        subscription.setSubscriptionStatus(status);
        InsuranceSubscription updatedSubscription = subscriptionRepository.save(subscription);
        return InsuranceSubscriptionResponseDto.fromEntity(updatedSubscription);
    }

    @Transactional
    public InsuranceSubscriptionResponseDto updateMedicalExamResult(String policyNumber, String result, LocalDate examDate) {
        InsuranceSubscription subscription = subscriptionRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new IllegalArgumentException("보험 가입을 찾을 수 없습니다. 증권번호: " + policyNumber));
        
        subscription.setMedicalExamResult(result);
        subscription.setMedicalExamDate(examDate);
        InsuranceSubscription updatedSubscription = subscriptionRepository.save(subscription);
        return InsuranceSubscriptionResponseDto.fromEntity(updatedSubscription);
    }

    @Transactional
    public void deleteSubscription(String policyNumber) {
        InsuranceSubscription subscription = subscriptionRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new IllegalArgumentException("보험 가입을 찾을 수 없습니다. 증권번호: " + policyNumber));
        subscriptionRepository.delete(subscription);
    }
}
