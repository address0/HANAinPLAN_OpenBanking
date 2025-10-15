package com.hanainplan.domain.banking.controller;

import com.hanainplan.domain.banking.dto.DepositSubscriptionRequestDto;
import com.hanainplan.domain.banking.dto.DepositSubscriptionResponseDto;
import com.hanainplan.domain.banking.service.DepositSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/banking/deposit")
@Tag(name = "정기예금 상품 가입", description = "정기예금 상품 가입 관리 API")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DepositSubscriptionController {

    private final DepositSubscriptionService depositSubscriptionService;

    @PostMapping("/subscribe")
    @Operation(summary = "정기예금 상품 가입", description = "하나/국민/신한은행 정기예금 상품에 가입합니다.")
    public ResponseEntity<DepositSubscriptionResponseDto> subscribeDeposit(
            @Valid @RequestBody DepositSubscriptionRequestDto request) {

        log.info("정기예금 가입 API 호출 - 사용자 ID: {}, 은행: {}, 상품: {}", 
                request.getUserId(), request.getBankCode(), request.getDepositCode());

        try {
            DepositSubscriptionResponseDto response = depositSubscriptionService.subscribeDeposit(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.error("정기예금 가입 요청 오류", e);
            return ResponseEntity.badRequest().body(
                    DepositSubscriptionResponseDto.failure(e.getMessage(), "INVALID_REQUEST"));
        } catch (Exception e) {
            log.error("정기예금 가입 실패", e);
            return ResponseEntity.internalServerError().body(
                    DepositSubscriptionResponseDto.failure(e.getMessage(), "SUBSCRIPTION_FAILED"));
        }
    }
}