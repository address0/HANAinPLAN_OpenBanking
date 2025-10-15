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

    @Transactional(readOnly = true)
    public List<InterestRateResponseDto> getAllInterestRates() {
        return interestRateRepository.findAll().stream()
            .map(InterestRateResponseDto::from)
            .collect(Collectors.toList());
    }
}