package com.hanainplan.kookmin.product.service;

import com.hanainplan.kookmin.product.dto.InterestRateResponseDto;
import com.hanainplan.kookmin.product.repository.InterestRateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InterestRateService {

    @Autowired
    private InterestRateRepository interestRateRepository;

    /**
     * 모든 금리 정보 조회
     */
    @Transactional(readOnly = true)
    public List<InterestRateResponseDto> getAllInterestRates() {
        return interestRateRepository.findAll().stream()
            .map(InterestRateResponseDto::from)
            .collect(Collectors.toList());
    }
}


