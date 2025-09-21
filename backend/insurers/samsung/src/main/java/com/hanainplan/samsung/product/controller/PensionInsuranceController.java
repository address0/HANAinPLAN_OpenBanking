package com.hanainplan.samsung.product.controller;

import com.hanainplan.samsung.product.dto.PensionInsuranceRequestDto;
import com.hanainplan.samsung.product.dto.PensionInsuranceResponseDto;
import com.hanainplan.samsung.product.service.PensionInsuranceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/samsung/pension-insurance")
@RequiredArgsConstructor
@Tag(name = "Samsung 연금보험 상품 관리", description = "Samsung 보험사의 연금보험 상품 CRUD API")
public class PensionInsuranceController {

    private final PensionInsuranceService pensionInsuranceService;

    @PostMapping
    @Operation(summary = "연금보험 상품 생성", description = "새로운 연금보험 상품을 생성합니다.")
    public ResponseEntity<PensionInsuranceResponseDto> createPensionInsurance(@Valid @RequestBody PensionInsuranceRequestDto requestDto) {
        try {
            PensionInsuranceResponseDto response = pensionInsuranceService.createPensionInsurance(requestDto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{productCode}")
    @Operation(summary = "연금보험 상품 조회", description = "상품코드로 연금보험 상품을 조회합니다.")
    public ResponseEntity<PensionInsuranceResponseDto> getPensionInsurance(@PathVariable String productCode) {
        try {
            PensionInsuranceResponseDto response = pensionInsuranceService.getPensionInsuranceByProductCode(productCode);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @Operation(summary = "전체 연금보험 상품 목록 조회", description = "모든 연금보험 상품 목록을 조회합니다.")
    public ResponseEntity<List<PensionInsuranceResponseDto>> getAllPensionInsurances() {
        List<PensionInsuranceResponseDto> responses = pensionInsuranceService.getAllPensionInsurances();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/active")
    @Operation(summary = "활성화된 연금보험 상품 목록 조회", description = "활성화된 연금보험 상품 목록을 조회합니다.")
    public ResponseEntity<List<PensionInsuranceResponseDto>> getActivePensionInsurances() {
        List<PensionInsuranceResponseDto> responses = pensionInsuranceService.getActivePensionInsurances();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/subscription-type/{subscriptionType}")
    @Operation(summary = "가입유형별 연금보험 상품 조회", description = "특정 가입유형의 연금보험 상품 목록을 조회합니다.")
    public ResponseEntity<List<PensionInsuranceResponseDto>> getPensionInsurancesBySubscriptionType(@PathVariable String subscriptionType) {
        List<PensionInsuranceResponseDto> responses = pensionInsuranceService.getPensionInsurancesBySubscriptionType(subscriptionType);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search")
    @Operation(summary = "연금보험 상품 검색", description = "상품명 키워드로 연금보험 상품을 검색합니다.")
    public ResponseEntity<List<PensionInsuranceResponseDto>> searchPensionInsurances(@RequestParam String keyword) {
        List<PensionInsuranceResponseDto> responses = pensionInsuranceService.searchPensionInsurancesByName(keyword);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{productCode}")
    @Operation(summary = "연금보험 상품 수정", description = "기존 연금보험 상품 정보를 수정합니다.")
    public ResponseEntity<PensionInsuranceResponseDto> updatePensionInsurance(
            @PathVariable String productCode,
            @Valid @RequestBody PensionInsuranceRequestDto requestDto) {
        try {
            PensionInsuranceResponseDto response = pensionInsuranceService.updatePensionInsurance(productCode, requestDto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{productCode}/toggle-status")
    @Operation(summary = "연금보험 상품 상태 변경", description = "연금보험 상품의 활성화/비활성화 상태를 변경합니다.")
    public ResponseEntity<PensionInsuranceResponseDto> togglePensionInsuranceStatus(@PathVariable String productCode) {
        try {
            PensionInsuranceResponseDto response = pensionInsuranceService.togglePensionInsuranceStatus(productCode);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{productCode}")
    @Operation(summary = "연금보험 상품 삭제", description = "연금보험 상품을 삭제합니다.")
    public ResponseEntity<Void> deletePensionInsurance(@PathVariable String productCode) {
        try {
            pensionInsuranceService.deletePensionInsurance(productCode);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
