package com.hanainplan.hana.product.controller;

import com.hanainplan.hana.product.dto.DepositSubscriptionRequest;
import com.hanainplan.hana.product.dto.ProductSubscriptionResponseDto;
import com.hanainplan.hana.product.service.DepositSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/deposit")
@Tag(name = "정기예금 가입", description = "IRP 계좌에서 정기예금 가입 처리 API")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DepositSubscriptionController {

    private final DepositSubscriptionService depositSubscriptionService;

    @PostMapping("/subscribe")
    @Operation(summary = "정기예금 가입 (IRP 계좌 출금 포함)", 
               description = "IRP 계좌에서 출금 후 정기예금에 가입합니다.")
    public ResponseEntity<Map<String, Object>> subscribeDeposit(
            @Valid @RequestBody DepositSubscriptionRequest request) {

        log.info("정기예금 가입 요청 - 상품코드: {}, 금액: {}원, IRP계좌: {}", 
                request.getProductCode(), request.getContractPrincipal(), request.getIrpAccountNumber());

        try {
            ProductSubscriptionResponseDto response = depositSubscriptionService.subscribeDepositFromIrp(request);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "정기예금 가입 완료");
            result.put("subscriptionId", response.getSubscriptionId());
            result.put("productCode", response.getProductCode());
            result.put("accountNumber", response.getAccountNumber());
            result.put("subscriptionDate", response.getSubscriptionDate());
            result.put("maturityDate", response.getMaturityDate());
            result.put("finalAppliedRate", response.getFinalAppliedRate());

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.error("정기예금 가입 요청 오류: {}", e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "잘못된 요청: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            log.error("정기예금 가입 처리 실패", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "정기예금 가입 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}