package com.hanainplan.hana.product.controller;

import com.hanainplan.hana.product.dto.DepositSubscriptionRequest;
import com.hanainplan.hana.product.dto.DepositSubscriptionResponse;
import com.hanainplan.hana.product.service.DepositSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 하나은행 정기예금 가입 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/deposit")
@RequiredArgsConstructor
@Tag(name = "정기예금 관리", description = "하나은행 정기예금 가입/조회/해지 API")
public class DepositSubscriptionController {

    private final DepositSubscriptionService depositSubscriptionService;

    /**
     * 정기예금 가입
     */
    @PostMapping("/subscribe")
    @Operation(summary = "정기예금 가입", description = "하나은행 정기예금 상품에 가입합니다 (IRP 계좌 연동)")
    public ResponseEntity<?> subscribe(@Valid @RequestBody DepositSubscriptionRequest request) {
        try {
            log.info("정기예금 가입 API 호출: customerCi={}, productType={}, amount={}",
                    request.getCustomerCi(), request.getProductType(), request.getSubscriptionAmount());

            DepositSubscriptionResponse response = depositSubscriptionService.subscribe(request);

            log.info("정기예금 가입 성공: accountNumber={}, expectedInterest={}",
                    response.getAccountNumber(), response.getExpectedInterest());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("정기예금 가입 실패 - 유효성 검증: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("유효성 검증 실패", e.getMessage()));

        } catch (RuntimeException e) {
            log.error("정기예금 가입 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(createErrorResponse("가입 실패", e.getMessage()));
        }
    }

    /**
     * 고객의 정기예금 가입 내역 조회
     */
    @GetMapping("/customer/{customerCi}")
    @Operation(summary = "고객 정기예금 내역 조회", description = "고객의 모든 정기예금 가입 내역을 조회합니다")
    public ResponseEntity<?> getCustomerSubscriptions(
            @Parameter(description = "고객 CI") @PathVariable String customerCi) {
        try {
            log.info("정기예금 내역 조회 API 호출: customerCi={}", customerCi);

            List<DepositSubscriptionResponse> subscriptions = 
                    depositSubscriptionService.getCustomerSubscriptions(customerCi);

            log.info("정기예금 내역 조회 성공: customerCi={}, count={}", customerCi, subscriptions.size());

            Map<String, Object> response = new HashMap<>();
            response.put("customerCi", customerCi);
            response.put("count", subscriptions.size());
            response.put("subscriptions", subscriptions);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("정기예금 내역 조회 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(createErrorResponse("조회 실패", e.getMessage()));
        }
    }

    /**
     * 계좌번호로 가입 내역 조회
     */
    @GetMapping("/account/{accountNumber}")
    @Operation(summary = "계좌번호로 가입 내역 조회", description = "특정 정기예금 계좌의 가입 내역을 조회합니다")
    public ResponseEntity<?> getSubscriptionByAccountNumber(
            @Parameter(description = "예금 계좌번호") @PathVariable String accountNumber) {
        try {
            log.info("정기예금 계좌 조회 API 호출: accountNumber={}", accountNumber);

            DepositSubscriptionResponse subscription = 
                    depositSubscriptionService.getSubscriptionByAccountNumber(accountNumber);

            log.info("정기예금 계좌 조회 성공: accountNumber={}", accountNumber);

            return ResponseEntity.ok(subscription);

        } catch (RuntimeException e) {
            log.warn("정기예금 계좌 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse("조회 실패", e.getMessage()));

        } catch (Exception e) {
            log.error("정기예금 계좌 조회 오류: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(createErrorResponse("조회 오류", e.getMessage()));
        }
    }

    /**
     * 중도해지
     */
    @PostMapping("/terminate/{accountNumber}")
    @Operation(summary = "정기예금 중도해지", description = "정기예금을 중도해지하고 중도해지 이자를 계산합니다")
    public ResponseEntity<?> terminateEarly(
            @Parameter(description = "예금 계좌번호") @PathVariable String accountNumber) {
        try {
            log.info("정기예금 중도해지 API 호출: accountNumber={}", accountNumber);

            DepositSubscriptionResponse response = 
                    depositSubscriptionService.terminateEarly(accountNumber);

            log.info("정기예금 중도해지 성공: accountNumber={}, interest={}",
                    accountNumber, response.getUnpaidInterest());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.warn("정기예금 중도해지 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("중도해지 실패", e.getMessage()));

        } catch (Exception e) {
            log.error("정기예금 중도해지 오류: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(createErrorResponse("중도해지 오류", e.getMessage()));
        }
    }

    /**
     * 만기 처리
     */
    @PostMapping("/mature/{accountNumber}")
    @Operation(summary = "정기예금 만기 처리", description = "정기예금 만기 처리 및 만기 이자를 지급합니다")
    public ResponseEntity<?> processMaturity(
            @Parameter(description = "예금 계좌번호") @PathVariable String accountNumber) {
        try {
            log.info("정기예금 만기 처리 API 호출: accountNumber={}", accountNumber);

            depositSubscriptionService.processMaturity(accountNumber);

            log.info("정기예금 만기 처리 성공: accountNumber={}", accountNumber);

            Map<String, String> response = new HashMap<>();
            response.put("message", "만기 처리가 완료되었습니다");
            response.put("accountNumber", accountNumber);
            response.put("status", "MATURED");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.warn("정기예금 만기 처리 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse("만기 처리 실패", e.getMessage()));

        } catch (Exception e) {
            log.error("정기예금 만기 처리 오류: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(createErrorResponse("만기 처리 오류", e.getMessage()));
        }
    }

    /**
     * 에러 응답 생성
     */
    private Map<String, String> createErrorResponse(String error, String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", error);
        response.put("message", message);
        return response;
    }
}


