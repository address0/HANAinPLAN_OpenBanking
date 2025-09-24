package com.hanainplan.hana.product.controller;

import com.hanainplan.hana.product.dto.InterestRateRequestDto;
import com.hanainplan.hana.product.dto.InterestRateResponseDto;
import com.hanainplan.hana.product.entity.InterestRate;
import com.hanainplan.hana.product.service.InterestRateService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/hana/interest-rates")
@CrossOrigin(origins = "*")
public class InterestRateController {

    @Autowired
    private InterestRateService interestRateService;

    /**
     * 금리 정보 생성
     */
    @PostMapping
    public ResponseEntity<InterestRateResponseDto> createInterestRate(@Valid @RequestBody InterestRateRequestDto request) {
        InterestRateResponseDto response = interestRateService.createInterestRate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 금리 정보 조회 (ID)
     */
    @GetMapping("/{interestRateId}")
    public ResponseEntity<InterestRateResponseDto> getInterestRateById(@PathVariable Long interestRateId) {
        Optional<InterestRateResponseDto> interestRate = interestRateService.getInterestRateById(interestRateId);
        return interestRate.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 상품코드로 금리 목록 조회
     */
    @GetMapping("/product/{productCode}")
    public ResponseEntity<List<InterestRateResponseDto>> getInterestRatesByProductCode(@PathVariable String productCode) {
        List<InterestRateResponseDto> interestRates = interestRateService.getInterestRatesByProductCode(productCode);
        return ResponseEntity.ok(interestRates);
    }

    /**
     * 상품코드와 금리종류로 금리 조회
     */
    @GetMapping("/product/{productCode}/type/{interestType}")
    public ResponseEntity<List<InterestRateResponseDto>> getInterestRatesByProductCodeAndType(
            @PathVariable String productCode, 
            @PathVariable InterestRate.InterestType interestType) {
        List<InterestRateResponseDto> interestRates = interestRateService.getInterestRatesByProductCodeAndType(productCode, interestType);
        return ResponseEntity.ok(interestRates);
    }

    /**
     * 상품코드와 만기기간으로 금리 조회
     */
    @GetMapping("/product/{productCode}/maturity")
    public ResponseEntity<List<InterestRateResponseDto>> getInterestRatesByProductCodeAndMaturityPeriod(
            @PathVariable String productCode, 
            @RequestParam(required = false) String maturityPeriod) {
        List<InterestRateResponseDto> interestRates = interestRateService.getInterestRatesByProductCodeAndMaturityPeriod(productCode, maturityPeriod);
        return ResponseEntity.ok(interestRates);
    }

    /**
     * 상품코드, 금리종류, 만기기간으로 금리 조회
     */
    @GetMapping("/product/{productCode}/type/{interestType}/maturity/{maturityPeriod}")
    public ResponseEntity<InterestRateResponseDto> getInterestRateByProductCodeAndTypeAndMaturityPeriod(
            @PathVariable String productCode, 
            @PathVariable InterestRate.InterestType interestType, 
            @PathVariable String maturityPeriod) {
        Optional<InterestRateResponseDto> interestRate = interestRateService.getInterestRateByProductCodeAndTypeAndMaturityPeriod(productCode, interestType, maturityPeriod);
        return interestRate.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 상품코드로 최신 기본금리 조회
     */
    @GetMapping("/product/{productCode}/latest-basic")
    public ResponseEntity<InterestRateResponseDto> getLatestBasicRateByProductCodeAndMaturityPeriod(
            @PathVariable String productCode, 
            @RequestParam String maturityPeriod) {
        Optional<InterestRateResponseDto> interestRate = interestRateService.getLatestBasicRateByProductCodeAndMaturityPeriod(productCode, maturityPeriod);
        return interestRate.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 상품코드로 최신 우대금리 조회
     */
    @GetMapping("/product/{productCode}/latest-preferential")
    public ResponseEntity<InterestRateResponseDto> getLatestPreferentialRateByProductCodeAndMaturityPeriod(
            @PathVariable String productCode, 
            @RequestParam String maturityPeriod) {
        Optional<InterestRateResponseDto> interestRate = interestRateService.getLatestPreferentialRateByProductCodeAndMaturityPeriod(productCode, maturityPeriod);
        return interestRate.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * IRP 상품 여부로 금리 조회
     */
    @GetMapping("/irp/{isIrp}")
    public ResponseEntity<List<InterestRateResponseDto>> getInterestRatesByIrpStatus(@PathVariable Boolean isIrp) {
        List<InterestRateResponseDto> interestRates = interestRateService.getInterestRatesByIrpStatus(isIrp);
        return ResponseEntity.ok(interestRates);
    }

    /**
     * 상품코드와 IRP 여부로 금리 조회
     */
    @GetMapping("/product/{productCode}/irp/{isIrp}")
    public ResponseEntity<List<InterestRateResponseDto>> getInterestRatesByProductCodeAndIrpStatus(
            @PathVariable String productCode, 
            @PathVariable Boolean isIrp) {
        List<InterestRateResponseDto> interestRates = interestRateService.getInterestRatesByProductCodeAndIrpStatus(productCode, isIrp);
        return ResponseEntity.ok(interestRates);
    }

    /**
     * 모든 금리 정보 조회
     */
    @GetMapping
    public ResponseEntity<List<InterestRateResponseDto>> getAllInterestRates() {
        List<InterestRateResponseDto> interestRates = interestRateService.getAllInterestRates();
        return ResponseEntity.ok(interestRates);
    }

    /**
     * 금리 정보 수정
     */
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

    /**
     * 금리 정보 삭제
     */
    @DeleteMapping("/{interestRateId}")
    public ResponseEntity<Void> deleteInterestRate(@PathVariable Long interestRateId) {
        try {
            interestRateService.deleteInterestRate(interestRateId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 상품코드로 모든 금리 정보 삭제
     */
    @DeleteMapping("/product/{productCode}")
    public ResponseEntity<Void> deleteInterestRatesByProductCode(@PathVariable String productCode) {
        interestRateService.deleteInterestRatesByProductCode(productCode);
        return ResponseEntity.noContent().build();
    }
}

