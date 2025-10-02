package com.hanainplan.domain.banking.controller;

import com.hanainplan.domain.banking.dto.DepositSubscriptionRequest;
import com.hanainplan.domain.banking.service.DepositSubscriptionIntegrationService;
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
 * HANAinPLAN 정기예금 통합 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/banking/deposit")
@RequiredArgsConstructor
@Tag(name = "정기예금 통합 관리", description = "HANAinPLAN 정기예금 가입/조회/해지 통합 API")
public class DepositSubscriptionIntegrationController {

    private final DepositSubscriptionIntegrationService depositSubscriptionIntegrationService;

    /**
     * 정기예금 가입
     */
    @PostMapping("/subscribe")
    @Operation(summary = "정기예금 가입", description = "은행별 정기예금 상품에 가입합니다 (HANAinPLAN 통합)")
    public ResponseEntity<?> subscribe(@Valid @RequestBody DepositSubscriptionRequest request) {
        try {
            log.info("정기예금 가입 API 호출: userId={}, bankCode={}, depositCode={}, productType={}", 
                    request.getUserId(), request.getBankCode(), request.getDepositCode(), request.getProductType());

            Map<String, Object> response = depositSubscriptionIntegrationService.subscribeDeposit(request);

            log.info("정기예금 가입 성공: accountNumber={}", response.get("accountNumber"));

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.warn("정기예금 가입 실패 - 유효성 검증: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(createErrorResponse("유효성 검증 실패", e.getMessage()));

        } catch (RuntimeException e) {
            log.error("정기예금 가입 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("가입 실패", e.getMessage()));
        } catch (Exception e) {
            log.error("정기예금 가입 오류: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(createErrorResponse("시스템 오류", e.getMessage()));
        }
    }

    /**
     * 사용자의 정기예금 가입 내역 조회
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자 정기예금 내역 조회", description = "사용자의 모든 은행 정기예금 가입 내역을 조회합니다")
    public ResponseEntity<?> getUserSubscriptions(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        try {
            log.info("사용자 정기예금 내역 조회 API 호출: userId={}", userId);

            List<Map<String, Object>> subscriptions = 
                    depositSubscriptionIntegrationService.getUserSubscriptions(userId);

            log.info("정기예금 내역 조회 성공: userId={}, count={}", userId, subscriptions.size());

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
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

            Map<String, Object> subscription = 
                    depositSubscriptionIntegrationService.getSubscriptionByAccountNumber(accountNumber);

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

            Map<String, Object> response = 
                    depositSubscriptionIntegrationService.terminateEarly(accountNumber);

            log.info("정기예금 중도해지 성공: accountNumber={}", accountNumber);

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
     * 은행별 가입 통계
     */
    @GetMapping("/statistics/user/{userId}")
    @Operation(summary = "사용자 예금 통계", description = "사용자의 은행별 예금 가입 통계를 조회합니다")
    public ResponseEntity<?> getUserStatistics(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        try {
            log.info("사용자 예금 통계 조회 API 호출: userId={}", userId);

            List<Map<String, Object>> subscriptions = 
                    depositSubscriptionIntegrationService.getUserSubscriptions(userId);

            // 통계 계산
            Map<String, Map<String, Object>> bankStats = new HashMap<>();
            for (Map<String, Object> sub : subscriptions) {
                String bankCode = (String) sub.get("bankCode");
                String bankName = (String) sub.get("bankName");
                
                Map<String, Object> stats = bankStats.computeIfAbsent(bankCode, k -> {
                    Map<String, Object> s = new HashMap<>();
                    s.put("bankCode", bankCode);
                    s.put("bankName", bankName);
                    s.put("count", 0);
                    s.put("totalBalance", 0.0);
                    return s;
                });
                
                stats.put("count", (Integer) stats.get("count") + 1);
                
                Object balance = sub.get("currentBalance");
                if (balance != null) {
                    double currentTotal = (Double) stats.get("totalBalance");
                    double addBalance = balance instanceof Number 
                            ? ((Number) balance).doubleValue() 
                            : Double.parseDouble(balance.toString());
                    stats.put("totalBalance", currentTotal + addBalance);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("totalSubscriptions", subscriptions.size());
            response.put("bankStatistics", bankStats.values());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("사용자 예금 통계 조회 실패: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(createErrorResponse("통계 조회 실패", e.getMessage()));
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

