package com.hanainplan.hanhwa.product.service;

import com.hanainplan.hanhwa.product.dto.GeneralInsuranceRequestDto;
import com.hanainplan.hanhwa.product.dto.GeneralInsuranceResponseDto;
import com.hanainplan.hanhwa.product.entity.GeneralInsurance;
import com.hanainplan.hanhwa.product.repository.GeneralInsuranceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GeneralInsuranceService {

    private final GeneralInsuranceRepository generalInsuranceRepository;

    @Transactional
    public GeneralInsuranceResponseDto createGeneralInsurance(GeneralInsuranceRequestDto requestDto) {
        if (generalInsuranceRepository.findByProductCode(requestDto.getProductCode()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 상품코드입니다: " + requestDto.getProductCode());
        }

        GeneralInsurance generalInsurance = GeneralInsurance.builder()
                .productCode(requestDto.getProductCode())
                .productName(requestDto.getProductName())
                .category(requestDto.getCategory())
                .benefitName(requestDto.getBenefitName())
                .paymentReason(requestDto.getPaymentReason())
                .paymentAmount(requestDto.getPaymentAmount())
                .subscriptionAmountBasic(requestDto.getSubscriptionAmountBasic())
                .subscriptionAmountMale(requestDto.getSubscriptionAmountMale())
                .subscriptionAmountFemale(requestDto.getSubscriptionAmountFemale())
                .interestRate(requestDto.getInterestRate())
                .insurancePriceIndexMale(requestDto.getInsurancePriceIndexMale())
                .insurancePriceIndexFemale(requestDto.getInsurancePriceIndexFemale())
                .productFeatures(requestDto.getProductFeatures())
                .surrenderValue(requestDto.getSurrenderValue())
                .renewalCycle(requestDto.getRenewalCycle())
                .isUniversal(requestDto.getIsUniversal())
                .salesChannel(requestDto.getSalesChannel())
                .specialNotes(requestDto.getSpecialNotes())
                .representativeNumber(requestDto.getRepresentativeNumber())
                .isActive(requestDto.getIsActive() != null ? requestDto.getIsActive() : true)
                .build();

        GeneralInsurance savedInsurance = generalInsuranceRepository.save(generalInsurance);
        return GeneralInsuranceResponseDto.fromEntity(savedInsurance);
    }

    @Transactional(readOnly = true)
    public GeneralInsuranceResponseDto getGeneralInsuranceByProductCode(String productCode) {
        GeneralInsurance generalInsurance = generalInsuranceRepository.findByProductCode(productCode)
                .orElseThrow(() -> new IllegalArgumentException("일반보험 상품을 찾을 수 없습니다. 상품코드: " + productCode));
        return GeneralInsuranceResponseDto.fromEntity(generalInsurance);
    }

    @Transactional(readOnly = true)
    public List<GeneralInsuranceResponseDto> getAllGeneralInsurances() {
        return generalInsuranceRepository.findAll().stream()
                .map(GeneralInsuranceResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GeneralInsuranceResponseDto> getActiveGeneralInsurances() {
        return generalInsuranceRepository.findByIsActiveTrue().stream()
                .map(GeneralInsuranceResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GeneralInsuranceResponseDto> getGeneralInsurancesByCategory(String category) {
        return generalInsuranceRepository.findByCategoryAndActive(category).stream()
                .map(GeneralInsuranceResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GeneralInsuranceResponseDto> searchGeneralInsurancesByName(String keyword) {
        return generalInsuranceRepository.findByProductNameContaining(keyword).stream()
                .map(GeneralInsuranceResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public GeneralInsuranceResponseDto updateGeneralInsurance(String productCode, GeneralInsuranceRequestDto requestDto) {
        GeneralInsurance generalInsurance = generalInsuranceRepository.findByProductCode(productCode)
                .orElseThrow(() -> new IllegalArgumentException("일반보험 상품을 찾을 수 없습니다. 상품코드: " + productCode));

        generalInsurance.setProductName(requestDto.getProductName());
        generalInsurance.setCategory(requestDto.getCategory());
        generalInsurance.setBenefitName(requestDto.getBenefitName());
        generalInsurance.setPaymentReason(requestDto.getPaymentReason());
        generalInsurance.setPaymentAmount(requestDto.getPaymentAmount());
        generalInsurance.setSubscriptionAmountBasic(requestDto.getSubscriptionAmountBasic());
        generalInsurance.setSubscriptionAmountMale(requestDto.getSubscriptionAmountMale());
        generalInsurance.setSubscriptionAmountFemale(requestDto.getSubscriptionAmountFemale());
        generalInsurance.setInterestRate(requestDto.getInterestRate());
        generalInsurance.setInsurancePriceIndexMale(requestDto.getInsurancePriceIndexMale());
        generalInsurance.setInsurancePriceIndexFemale(requestDto.getInsurancePriceIndexFemale());
        generalInsurance.setProductFeatures(requestDto.getProductFeatures());
        generalInsurance.setSurrenderValue(requestDto.getSurrenderValue());
        generalInsurance.setRenewalCycle(requestDto.getRenewalCycle());
        generalInsurance.setIsUniversal(requestDto.getIsUniversal());
        generalInsurance.setSalesChannel(requestDto.getSalesChannel());
        generalInsurance.setSpecialNotes(requestDto.getSpecialNotes());
        generalInsurance.setRepresentativeNumber(requestDto.getRepresentativeNumber());
        if (requestDto.getIsActive() != null) {
            generalInsurance.setIsActive(requestDto.getIsActive());
        }

        GeneralInsurance updatedInsurance = generalInsuranceRepository.save(generalInsurance);
        return GeneralInsuranceResponseDto.fromEntity(updatedInsurance);
    }

    @Transactional
    public void deleteGeneralInsurance(String productCode) {
        GeneralInsurance generalInsurance = generalInsuranceRepository.findByProductCode(productCode)
                .orElseThrow(() -> new IllegalArgumentException("일반보험 상품을 찾을 수 없습니다. 상품코드: " + productCode));
        generalInsuranceRepository.delete(generalInsurance);
    }

    @Transactional
    public GeneralInsuranceResponseDto toggleGeneralInsuranceStatus(String productCode) {
        GeneralInsurance generalInsurance = generalInsuranceRepository.findByProductCode(productCode)
                .orElseThrow(() -> new IllegalArgumentException("일반보험 상품을 찾을 수 없습니다. 상품코드: " + productCode));
        
        generalInsurance.setIsActive(!generalInsurance.getIsActive());
        GeneralInsurance updatedInsurance = generalInsuranceRepository.save(generalInsurance);
        return GeneralInsuranceResponseDto.fromEntity(updatedInsurance);
    }
}
