package com.hanainplan.hana.product.service;

import com.hanainplan.hana.product.dto.InterestRateRequestDto;
import com.hanainplan.hana.product.dto.InterestRateResponseDto;
import com.hanainplan.hana.product.entity.InterestRate;
import com.hanainplan.hana.product.repository.InterestRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class InterestRateService {

    @Autowired
    private InterestRateRepository interestRateRepository;

    public InterestRateResponseDto createInterestRate(InterestRateRequestDto request) {
        InterestRate interestRate = InterestRate.builder()
            .productCode(request.getProductCode())
            .interestType(request.getInterestType())
            .maturityPeriod(request.getMaturityPeriod())
            .interestRate(request.getInterestRate())
            .isIrp(request.getIsIrp() != null ? request.getIsIrp() : false)
            .effectiveDate(request.getEffectiveDate())
            .build();

        InterestRate savedInterestRate = interestRateRepository.save(interestRate);
        return InterestRateResponseDto.from(savedInterestRate);
    }

    @Transactional(readOnly = true)
    public Optional<InterestRateResponseDto> getInterestRateById(Long interestRateId) {
        return interestRateRepository.findById(interestRateId)
            .map(InterestRateResponseDto::from);
    }

    @Transactional(readOnly = true)
    public List<InterestRateResponseDto> getInterestRatesByProductCode(String productCode) {
        List<InterestRate> interestRates = interestRateRepository.findByProductCode(productCode);
        return interestRates.stream()
            .map(InterestRateResponseDto::from)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InterestRateResponseDto> getInterestRatesByProductCodeAndType(String productCode, InterestRate.InterestType interestType) {
        List<InterestRate> interestRates = interestRateRepository.findByProductCodeAndInterestType(productCode, interestType);
        return interestRates.stream()
            .map(InterestRateResponseDto::from)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InterestRateResponseDto> getInterestRatesByProductCodeAndMaturityPeriod(String productCode, String maturityPeriod) {
        List<InterestRate> interestRates = interestRateRepository.findAllByProductCodeOrderByEffectiveDateDesc(productCode);
        return interestRates.stream()
            .filter(ir -> maturityPeriod == null || maturityPeriod.equals(ir.getMaturityPeriod()))
            .map(InterestRateResponseDto::from)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<InterestRateResponseDto> getInterestRateByProductCodeAndTypeAndMaturityPeriod(
            String productCode, 
            InterestRate.InterestType interestType, 
            String maturityPeriod) {
        return interestRateRepository.findByProductCodeAndInterestTypeAndMaturityPeriod(productCode, interestType, maturityPeriod)
            .map(InterestRateResponseDto::from);
    }

    @Transactional(readOnly = true)
    public Optional<InterestRateResponseDto> getLatestBasicRateByProductCodeAndMaturityPeriod(String productCode, String maturityPeriod) {
        return interestRateRepository.findLatestBasicRateByProductCodeAndMaturityPeriod(productCode, maturityPeriod)
            .map(InterestRateResponseDto::from);
    }

    @Transactional(readOnly = true)
    public Optional<InterestRateResponseDto> getLatestPreferentialRateByProductCodeAndMaturityPeriod(String productCode, String maturityPeriod) {
        return interestRateRepository.findLatestPreferentialRateByProductCodeAndMaturityPeriod(productCode, maturityPeriod)
            .map(InterestRateResponseDto::from);
    }

    @Transactional(readOnly = true)
    public List<InterestRateResponseDto> getInterestRatesByIrpStatus(Boolean isIrp) {
        List<InterestRate> interestRates = interestRateRepository.findByIsIrp(isIrp);
        return interestRates.stream()
            .map(InterestRateResponseDto::from)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InterestRateResponseDto> getInterestRatesByProductCodeAndIrpStatus(String productCode, Boolean isIrp) {
        List<InterestRate> interestRates = interestRateRepository.findByProductCodeAndIsIrp(productCode, isIrp);
        return interestRates.stream()
            .map(InterestRateResponseDto::from)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InterestRateResponseDto> getAllInterestRates() {
        List<InterestRate> interestRates = interestRateRepository.findAll();
        return interestRates.stream()
            .map(InterestRateResponseDto::from)
            .collect(Collectors.toList());
    }

    public InterestRateResponseDto updateInterestRate(Long interestRateId, InterestRateRequestDto request) {
        InterestRate interestRate = interestRateRepository.findById(interestRateId)
            .orElseThrow(() -> new IllegalArgumentException("금리 정보를 찾을 수 없습니다: " + interestRateId));

        interestRate.setProductCode(request.getProductCode());
        interestRate.setInterestType(request.getInterestType());
        interestRate.setMaturityPeriod(request.getMaturityPeriod());
        interestRate.setInterestRate(request.getInterestRate());
        interestRate.setIsIrp(request.getIsIrp());
        interestRate.setEffectiveDate(request.getEffectiveDate());

        InterestRate updatedInterestRate = interestRateRepository.save(interestRate);
        return InterestRateResponseDto.from(updatedInterestRate);
    }

    public void deleteInterestRate(Long interestRateId) {
        if (!interestRateRepository.existsById(interestRateId)) {
            throw new IllegalArgumentException("금리 정보를 찾을 수 없습니다: " + interestRateId);
        }
        interestRateRepository.deleteById(interestRateId);
    }

    public void deleteInterestRatesByProductCode(String productCode) {
        List<InterestRate> interestRates = interestRateRepository.findByProductCode(productCode);
        interestRateRepository.deleteAll(interestRates);
    }
}