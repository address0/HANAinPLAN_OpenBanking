package com.hanainplan.hana.product.service;

import com.hanainplan.hana.product.dto.PensionInsuranceRequestDto;
import com.hanainplan.hana.product.dto.PensionInsuranceResponseDto;
import com.hanainplan.hana.product.entity.PensionInsurance;
import com.hanainplan.hana.product.repository.PensionInsuranceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PensionInsuranceService {

    private final PensionInsuranceRepository pensionInsuranceRepository;

    @Transactional
    public PensionInsuranceResponseDto createPensionInsurance(PensionInsuranceRequestDto requestDto) {
        // 상품코드 중복 확인
        if (pensionInsuranceRepository.findByProductCode(requestDto.getProductCode()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 상품코드입니다: " + requestDto.getProductCode());
        }

        PensionInsurance pensionInsurance = PensionInsurance.builder()
                .productCode(requestDto.getProductCode())
                .productName(requestDto.getProductName())
                .maintenancePeriod(requestDto.getMaintenancePeriod())
                .premiumPayment(requestDto.getPremiumPayment())
                .contractorAccumulationMale(requestDto.getContractorAccumulationMale())
                .accumulationRateMale(requestDto.getAccumulationRateMale())
                .surrenderValueMale(requestDto.getSurrenderValueMale())
                .contractorAccumulationFemale(requestDto.getContractorAccumulationFemale())
                .accumulationRateFemale(requestDto.getAccumulationRateFemale())
                .surrenderValueFemale(requestDto.getSurrenderValueFemale())
                .expectedReturnRateMinimum(requestDto.getExpectedReturnRateMinimum())
                .expectedReturnRateCurrent(requestDto.getExpectedReturnRateCurrent())
                .expectedReturnRateAverage(requestDto.getExpectedReturnRateAverage())
                .businessExpenseRatio(requestDto.getBusinessExpenseRatio())
                .riskCoverage(requestDto.getRiskCoverage())
                .currentAnnouncedRate(requestDto.getCurrentAnnouncedRate())
                .minimumGuaranteedRate(requestDto.getMinimumGuaranteedRate())
                .subscriptionType(requestDto.getSubscriptionType())
                .isUniversal(requestDto.getIsUniversal())
                .paymentMethod(requestDto.getPaymentMethod())
                .salesChannel(requestDto.getSalesChannel())
                .specialNotes(requestDto.getSpecialNotes())
                .representativeNumber(requestDto.getRepresentativeNumber())
                .isActive(requestDto.getIsActive() != null ? requestDto.getIsActive() : true)
                .build();

        PensionInsurance savedInsurance = pensionInsuranceRepository.save(pensionInsurance);
        return PensionInsuranceResponseDto.fromEntity(savedInsurance);
    }

    @Transactional(readOnly = true)
    public PensionInsuranceResponseDto getPensionInsuranceByProductCode(String productCode) {
        PensionInsurance pensionInsurance = pensionInsuranceRepository.findByProductCode(productCode)
                .orElseThrow(() -> new IllegalArgumentException("연금보험 상품을 찾을 수 없습니다. 상품코드: " + productCode));
        return PensionInsuranceResponseDto.fromEntity(pensionInsurance);
    }

    @Transactional(readOnly = true)
    public List<PensionInsuranceResponseDto> getAllPensionInsurances() {
        return pensionInsuranceRepository.findAll().stream()
                .map(PensionInsuranceResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PensionInsuranceResponseDto> getActivePensionInsurances() {
        return pensionInsuranceRepository.findByIsActiveTrue().stream()
                .map(PensionInsuranceResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PensionInsuranceResponseDto> getPensionInsurancesBySubscriptionType(String subscriptionType) {
        return pensionInsuranceRepository.findBySubscriptionTypeAndActive(subscriptionType).stream()
                .map(PensionInsuranceResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PensionInsuranceResponseDto> searchPensionInsurancesByName(String keyword) {
        return pensionInsuranceRepository.findByProductNameContaining(keyword).stream()
                .map(PensionInsuranceResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public PensionInsuranceResponseDto updatePensionInsurance(String productCode, PensionInsuranceRequestDto requestDto) {
        PensionInsurance pensionInsurance = pensionInsuranceRepository.findByProductCode(productCode)
                .orElseThrow(() -> new IllegalArgumentException("연금보험 상품을 찾을 수 없습니다. 상품코드: " + productCode));

        // 상품코드는 변경 불가
        pensionInsurance.setProductName(requestDto.getProductName());
        pensionInsurance.setMaintenancePeriod(requestDto.getMaintenancePeriod());
        pensionInsurance.setPremiumPayment(requestDto.getPremiumPayment());
        pensionInsurance.setContractorAccumulationMale(requestDto.getContractorAccumulationMale());
        pensionInsurance.setAccumulationRateMale(requestDto.getAccumulationRateMale());
        pensionInsurance.setSurrenderValueMale(requestDto.getSurrenderValueMale());
        pensionInsurance.setContractorAccumulationFemale(requestDto.getContractorAccumulationFemale());
        pensionInsurance.setAccumulationRateFemale(requestDto.getAccumulationRateFemale());
        pensionInsurance.setSurrenderValueFemale(requestDto.getSurrenderValueFemale());
        pensionInsurance.setExpectedReturnRateMinimum(requestDto.getExpectedReturnRateMinimum());
        pensionInsurance.setExpectedReturnRateCurrent(requestDto.getExpectedReturnRateCurrent());
        pensionInsurance.setExpectedReturnRateAverage(requestDto.getExpectedReturnRateAverage());
        pensionInsurance.setBusinessExpenseRatio(requestDto.getBusinessExpenseRatio());
        pensionInsurance.setRiskCoverage(requestDto.getRiskCoverage());
        pensionInsurance.setCurrentAnnouncedRate(requestDto.getCurrentAnnouncedRate());
        pensionInsurance.setMinimumGuaranteedRate(requestDto.getMinimumGuaranteedRate());
        pensionInsurance.setSubscriptionType(requestDto.getSubscriptionType());
        pensionInsurance.setIsUniversal(requestDto.getIsUniversal());
        pensionInsurance.setPaymentMethod(requestDto.getPaymentMethod());
        pensionInsurance.setSalesChannel(requestDto.getSalesChannel());
        pensionInsurance.setSpecialNotes(requestDto.getSpecialNotes());
        pensionInsurance.setRepresentativeNumber(requestDto.getRepresentativeNumber());
        if (requestDto.getIsActive() != null) {
            pensionInsurance.setIsActive(requestDto.getIsActive());
        }

        PensionInsurance updatedInsurance = pensionInsuranceRepository.save(pensionInsurance);
        return PensionInsuranceResponseDto.fromEntity(updatedInsurance);
    }

    @Transactional
    public void deletePensionInsurance(String productCode) {
        PensionInsurance pensionInsurance = pensionInsuranceRepository.findByProductCode(productCode)
                .orElseThrow(() -> new IllegalArgumentException("연금보험 상품을 찾을 수 없습니다. 상품코드: " + productCode));
        pensionInsuranceRepository.delete(pensionInsurance);
    }

    @Transactional
    public PensionInsuranceResponseDto togglePensionInsuranceStatus(String productCode) {
        PensionInsurance pensionInsurance = pensionInsuranceRepository.findByProductCode(productCode)
                .orElseThrow(() -> new IllegalArgumentException("연금보험 상품을 찾을 수 없습니다. 상품코드: " + productCode));
        
        pensionInsurance.setIsActive(!pensionInsurance.getIsActive());
        PensionInsurance updatedInsurance = pensionInsuranceRepository.save(pensionInsurance);
        return PensionInsuranceResponseDto.fromEntity(updatedInsurance);
    }
}
