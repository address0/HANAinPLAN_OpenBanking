package com.hanainplan.domain.banking.controller;

import com.hanainplan.domain.banking.dto.DepositProductResponse;
import com.hanainplan.domain.banking.dto.OptimalDepositRecommendation;
import com.hanainplan.domain.banking.service.DepositProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 예금 상품 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/banking/deposit-products")
@RequiredArgsConstructor
@Tag(name = "예금 상품 관리", description = "예금 상품 조회 및 추천 API")
public class DepositProductController {

    private final DepositProductService depositProductService;

    /**
     * 모든 예금 상품 조회
     */
    @GetMapping
    @Operation(summary = "모든 예금 상품 조회", description = "등록된 모든 예금 상품을 조회합니다")
    public ResponseEntity<?> getAllDepositProducts() {
        try {
            log.info("모든 예금 상품 조회 API 호출");

            List<DepositProductResponse> products = depositProductService.getAllDepositProducts();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", products.size());
            response.put("products", products);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("예금 상품 조회 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("조회 실패", e.getMessage()));
        }
    }

    /**
     * 은행별 예금 상품 조회
     */
    @GetMapping("/bank/{bankCode}")
    @Operation(summary = "은행별 예금 상품 조회", description = "특정 은행의 예금 상품을 조회합니다")
    public ResponseEntity<?> getDepositProductsByBank(
            @Parameter(description = "은행 코드 (HANA, SHINHAN, KOOKMIN)") @PathVariable String bankCode) {
        try {
            log.info("은행별 예금 상품 조회 API 호출: bankCode={}", bankCode);

            List<DepositProductResponse> products = depositProductService.getDepositProductsByBank(bankCode);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("bankCode", bankCode);
            response.put("count", products.size());
            response.put("products", products);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("은행별 예금 상품 조회 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("조회 실패", e.getMessage()));
        }
    }

    /**
     * 사용자 목표 기반 최적 예금 상품 추천
     */
    @PostMapping("/recommend")
    @Operation(summary = "최적 예금 상품 추천", 
               description = "사용자의 목표와 IRP 계좌 정보를 기반으로 최적의 예금 상품을 추천합니다")
    public ResponseEntity<?> recommendOptimalDeposit(
            @Valid @RequestBody com.hanainplan.domain.banking.dto.DepositRecommendationRequest request) {
        try {
            log.info("최적 예금 상품 추천 API 호출: userId={}, retirementDate={}, goalAmount={}", 
                    request.getUserId(), request.getRetirementDate(), request.getGoalAmount());

            OptimalDepositRecommendation recommendation = 
                    depositProductService.recommendOptimalDeposit(
                            request.getUserId(), 
                            request.getRetirementDate(), 
                            request.getGoalAmount());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "최적 예금 상품 추천이 완료되었습니다");
            response.put("recommendation", recommendation);

            log.info("최적 예금 상품 추천 완료: userId={}, productType={}, period={}{}", 
                    request.getUserId(), recommendation.getProductType(), 
                    recommendation.getContractPeriod(), recommendation.getContractPeriodUnit());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.warn("최적 예금 상품 추천 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("추천 실패", e.getMessage()));

        } catch (Exception e) {
            log.error("최적 예금 상품 추천 오류: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("추천 오류", e.getMessage()));
        }
    }

    /**
     * 에러 응답 생성
     */
    private Map<String, Object> createErrorResponse(String error, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", error);
        response.put("message", message);
        return response;
    }
}

