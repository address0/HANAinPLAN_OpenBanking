package com.hanainplan.hana.product.controller;

import com.hanainplan.hana.product.dto.GeneralInsuranceRequestDto;
import com.hanainplan.hana.product.dto.GeneralInsuranceResponseDto;
import com.hanainplan.hana.product.service.GeneralInsuranceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hana/general-insurance")
@RequiredArgsConstructor
@Tag(name = "HANA 일반보험 상품 관리", description = "HANA 보험사의 일반보험 상품 CRUD API")
public class GeneralInsuranceController {

    private final GeneralInsuranceService generalInsuranceService;

    @PostMapping
    @Operation(summary = "일반보험 상품 생성", description = "새로운 일반보험 상품을 생성합니다.")
    public ResponseEntity<GeneralInsuranceResponseDto> createGeneralInsurance(@Valid @RequestBody GeneralInsuranceRequestDto requestDto) {
        try {
            GeneralInsuranceResponseDto response = generalInsuranceService.createGeneralInsurance(requestDto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{productCode}")
    @Operation(summary = "일반보험 상품 조회", description = "상품코드로 일반보험 상품을 조회합니다.")
    public ResponseEntity<GeneralInsuranceResponseDto> getGeneralInsurance(@PathVariable String productCode) {
        try {
            GeneralInsuranceResponseDto response = generalInsuranceService.getGeneralInsuranceByProductCode(productCode);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @Operation(summary = "전체 일반보험 상품 목록 조회", description = "모든 일반보험 상품 목록을 조회합니다.")
    public ResponseEntity<List<GeneralInsuranceResponseDto>> getAllGeneralInsurances() {
        List<GeneralInsuranceResponseDto> responses = generalInsuranceService.getAllGeneralInsurances();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/active")
    @Operation(summary = "활성화된 일반보험 상품 목록 조회", description = "활성화된 일반보험 상품 목록을 조회합니다.")
    public ResponseEntity<List<GeneralInsuranceResponseDto>> getActiveGeneralInsurances() {
        List<GeneralInsuranceResponseDto> responses = generalInsuranceService.getActiveGeneralInsurances();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "카테고리별 일반보험 상품 조회", description = "특정 카테고리의 일반보험 상품 목록을 조회합니다.")
    public ResponseEntity<List<GeneralInsuranceResponseDto>> getGeneralInsurancesByCategory(@PathVariable String category) {
        List<GeneralInsuranceResponseDto> responses = generalInsuranceService.getGeneralInsurancesByCategory(category);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search")
    @Operation(summary = "일반보험 상품 검색", description = "상품명 키워드로 일반보험 상품을 검색합니다.")
    public ResponseEntity<List<GeneralInsuranceResponseDto>> searchGeneralInsurances(@RequestParam String keyword) {
        List<GeneralInsuranceResponseDto> responses = generalInsuranceService.searchGeneralInsurancesByName(keyword);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{productCode}")
    @Operation(summary = "일반보험 상품 수정", description = "기존 일반보험 상품 정보를 수정합니다.")
    public ResponseEntity<GeneralInsuranceResponseDto> updateGeneralInsurance(
            @PathVariable String productCode,
            @Valid @RequestBody GeneralInsuranceRequestDto requestDto) {
        try {
            GeneralInsuranceResponseDto response = generalInsuranceService.updateGeneralInsurance(productCode, requestDto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{productCode}/toggle-status")
    @Operation(summary = "일반보험 상품 상태 변경", description = "일반보험 상품의 활성화/비활성화 상태를 변경합니다.")
    public ResponseEntity<GeneralInsuranceResponseDto> toggleGeneralInsuranceStatus(@PathVariable String productCode) {
        try {
            GeneralInsuranceResponseDto response = generalInsuranceService.toggleGeneralInsuranceStatus(productCode);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{productCode}")
    @Operation(summary = "일반보험 상품 삭제", description = "일반보험 상품을 삭제합니다.")
    public ResponseEntity<Void> deleteGeneralInsurance(@PathVariable String productCode) {
        try {
            generalInsuranceService.deleteGeneralInsurance(productCode);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
