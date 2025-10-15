package com.hanainplan.kookmin.product.controller;

import com.hanainplan.kookmin.product.dto.InterestRateResponseDto;
import com.hanainplan.kookmin.product.service.InterestRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kookmin/interest-rates")
@CrossOrigin(origins = "*")
public class InterestRateController {

    @Autowired
    private InterestRateService interestRateService;

    @GetMapping
    public ResponseEntity<List<InterestRateResponseDto>> getAllInterestRates() {
        List<InterestRateResponseDto> interestRates = interestRateService.getAllInterestRates();
        return ResponseEntity.ok(interestRates);
    }

    @GetMapping("/all")
    public ResponseEntity<List<java.util.Map<String, Object>>> getAllInterestRatesForIntegration() {
        List<InterestRateResponseDto> interestRates = interestRateService.getAllInterestRates();
        List<java.util.Map<String, Object>> result = interestRates.stream()
            .map(rate -> {
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("bankCode", "KOOKMIN");
                map.put("bankName", "국민은행");
                map.put("productCode", rate.getProductCode());
                map.put("productName", "국민은행 정기예금");
                map.put("maturityPeriod", rate.getMaturityPeriod());
                map.put("interestRate", rate.getInterestRate());
                map.put("interestType", rate.getInterestType().name());
                map.put("isIrp", rate.getIsIrp());
                return map;
            })
            .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(result);
    }
}