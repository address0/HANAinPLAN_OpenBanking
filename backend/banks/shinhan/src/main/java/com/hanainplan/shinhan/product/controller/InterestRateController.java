package com.hanainplan.shinhan.product.controller;

import com.hanainplan.shinhan.product.dto.InterestRateRequestDto;
import com.hanainplan.shinhan.product.dto.InterestRateResponseDto;
import com.hanainplan.shinhan.product.entity.InterestRate;
import com.hanainplan.shinhan.product.service.InterestRateService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/shinhan/interest-rates")
@CrossOrigin(origins = "*")
public class InterestRateController {

    @Autowired
    private InterestRateService interestRateService;

    @PostMapping
    public ResponseEntity<InterestRateResponseDto> createInterestRate(@Valid @RequestBody InterestRateRequestDto request) {
        InterestRateResponseDto response = interestRateService.createInterestRate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{interestRateId}")
    public ResponseEntity<InterestRateResponseDto> getInterestRateById(@PathVariable Long interestRateId) {
        Optional<InterestRateResponseDto> interestRate = interestRateService.getInterestRateById(interestRateId);
        return interestRate.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/product/{productCode}")
    public ResponseEntity<List<InterestRateResponseDto>> getInterestRatesByProductCode(@PathVariable String productCode) {
        List<InterestRateResponseDto> interestRates = interestRateService.getInterestRatesByProductCode(productCode);
        return ResponseEntity.ok(interestRates);
    }

    @GetMapping("/product/{productCode}/type/{interestType}")
    public ResponseEntity<List<InterestRateResponseDto>> getInterestRatesByProductCodeAndType(
            @PathVariable String productCode, 
            @PathVariable InterestRate.InterestType interestType) {
        List<InterestRateResponseDto> interestRates = interestRateService.getInterestRatesByProductCodeAndType(productCode, interestType);
        return ResponseEntity.ok(interestRates);
    }

    @GetMapping("/product/{productCode}/maturity")
    public ResponseEntity<List<InterestRateResponseDto>> getInterestRatesByProductCodeAndMaturityPeriod(
            @PathVariable String productCode, 
            @RequestParam(required = false) String maturityPeriod) {
        List<InterestRateResponseDto> interestRates = interestRateService.getInterestRatesByProductCodeAndMaturityPeriod(productCode, maturityPeriod);
        return ResponseEntity.ok(interestRates);
    }

    @GetMapping("/product/{productCode}/type/{interestType}/maturity/{maturityPeriod}")
    public ResponseEntity<InterestRateResponseDto> getInterestRateByProductCodeAndTypeAndMaturityPeriod(
            @PathVariable String productCode, 
            @PathVariable InterestRate.InterestType interestType, 
            @PathVariable String maturityPeriod) {
        Optional<InterestRateResponseDto> interestRate = interestRateService.getInterestRateByProductCodeAndTypeAndMaturityPeriod(productCode, interestType, maturityPeriod);
        return interestRate.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/product/{productCode}/latest-basic")
    public ResponseEntity<InterestRateResponseDto> getLatestBasicRateByProductCodeAndMaturityPeriod(
            @PathVariable String productCode, 
            @RequestParam String maturityPeriod) {
        Optional<InterestRateResponseDto> interestRate = interestRateService.getLatestBasicRateByProductCodeAndMaturityPeriod(productCode, maturityPeriod);
        return interestRate.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/product/{productCode}/latest-preferential")
    public ResponseEntity<InterestRateResponseDto> getLatestPreferentialRateByProductCodeAndMaturityPeriod(
            @PathVariable String productCode, 
            @RequestParam String maturityPeriod) {
        Optional<InterestRateResponseDto> interestRate = interestRateService.getLatestPreferentialRateByProductCodeAndMaturityPeriod(productCode, maturityPeriod);
        return interestRate.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/irp/{isIrp}")
    public ResponseEntity<List<InterestRateResponseDto>> getInterestRatesByIrpStatus(@PathVariable Boolean isIrp) {
        List<InterestRateResponseDto> interestRates = interestRateService.getInterestRatesByIrpStatus(isIrp);
        return ResponseEntity.ok(interestRates);
    }

    @GetMapping("/product/{productCode}/irp/{isIrp}")
    public ResponseEntity<List<InterestRateResponseDto>> getInterestRatesByProductCodeAndIrpStatus(
            @PathVariable String productCode, 
            @PathVariable Boolean isIrp) {
        List<InterestRateResponseDto> interestRates = interestRateService.getInterestRatesByProductCodeAndIrpStatus(productCode, isIrp);
        return ResponseEntity.ok(interestRates);
    }

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
                map.put("bankCode", "SHINHAN");
                map.put("bankName", "신한은행");
                map.put("productCode", rate.getProductCode());
                map.put("productName", "신한은행 정기예금");
                map.put("maturityPeriod", rate.getMaturityPeriod());
                map.put("interestRate", rate.getInterestRate());
                map.put("interestType", rate.getInterestType().name());
                map.put("isIrp", rate.getIsIrp());
                return map;
            })
            .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{interestRateId}")
    public ResponseEntity<InterestRateResponseDto> updateInterestRate(
            @PathVariable Long interestRateId, 
            @Valid @RequestBody InterestRateRequestDto request) {
        try {
            InterestRateResponseDto response = interestRateService.updateInterestRate(interestRateId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{interestRateId}")
    public ResponseEntity<Void> deleteInterestRate(@PathVariable Long interestRateId) {
        try {
            interestRateService.deleteInterestRate(interestRateId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/product/{productCode}")
    public ResponseEntity<Void> deleteInterestRatesByProductCode(@PathVariable String productCode) {
        interestRateService.deleteInterestRatesByProductCode(productCode);
        return ResponseEntity.noContent().build();
    }
}