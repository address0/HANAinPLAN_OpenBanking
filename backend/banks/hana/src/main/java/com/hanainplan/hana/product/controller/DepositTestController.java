package com.hanainplan.hana.product.controller;

import com.hanainplan.hana.product.service.DepositSchedulerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/hana/deposit-test")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Deposit Test", description = "예금 스케줄러 테스트 API")
public class DepositTestController {

    private final DepositSchedulerService depositSchedulerService;

    @PostMapping("/maturity-process")
    @Operation(summary = "만기 처리 수동 실행", description = "오늘 만기인 예금들을 수동으로 처리합니다")
    public ResponseEntity<?> manualProcessMaturity() {
        try {
            log.info("수동 만기 처리 요청");
            
            depositSchedulerService.processMaturityDeposits();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "만기 처리 완료"
            ));
            
        } catch (Exception e) {
            log.error("수동 만기 처리 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "만기 처리 실패: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/interest-payment")
    @Operation(summary = "이자 지급 수동 실행", description = "월간 이자 지급을 수동으로 실행합니다")
    public ResponseEntity<?> manualProcessInterest() {
        try {
            log.info("수동 이자 지급 처리 요청");
            
            depositSchedulerService.processMonthlyInterestPayments();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "이자 지급 완료"
            ));
            
        } catch (Exception e) {
            log.error("수동 이자 지급 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "이자 지급 실패: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/maturity/{accountNumber}")
    @Operation(summary = "특정 계좌 만기 처리", description = "특정 계좌의 만기를 수동으로 처리합니다")
    public ResponseEntity<?> processAccountMaturity(@PathVariable String accountNumber) {
        try {
            log.info("특정 계좌 만기 처리 요청 - 계좌번호: {}", accountNumber);
            
            depositSchedulerService.processMaturityByAccountNumber(accountNumber);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "만기 처리 완료",
                "accountNumber", accountNumber
            ));
            
        } catch (Exception e) {
            log.error("계좌 만기 처리 실패 - 계좌번호: {}", accountNumber, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "만기 처리 실패: " + e.getMessage()
            ));
        }
    }
}

