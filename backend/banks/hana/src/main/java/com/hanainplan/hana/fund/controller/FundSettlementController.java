package com.hanainplan.hana.fund.controller;

import com.hanainplan.hana.fund.batch.FundSettlementBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/hana/fund-settlement")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fund Settlement", description = "펀드 결제 처리 API (테스트용)")
public class FundSettlementController {

    private final FundSettlementBatchService fundSettlementBatchService;

    @PostMapping("/manual-process")
    @Operation(summary = "T+2 결제 수동 실행", description = "오늘 결제일인 거래들을 수동으로 처리합니다")
    public ResponseEntity<?> manualProcessSettlements() {
        try {
            log.info("수동 T+2 결제 처리 요청");
            
            fundSettlementBatchService.processT2Settlements();
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "T+2 결제 처리 완료"
            ));
            
        } catch (Exception e) {
            log.error("수동 결제 처리 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "결제 처리 실패: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/transaction/{transactionId}")
    @Operation(summary = "특정 거래 결제 처리", description = "특정 거래ID의 결제를 수동으로 처리합니다")
    public ResponseEntity<?> processTransaction(@PathVariable Long transactionId) {
        try {
            log.info("특정 거래 결제 처리 요청 - 거래ID: {}", transactionId);
            
            fundSettlementBatchService.processManualSettlement(transactionId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "거래 결제 완료",
                "transactionId", transactionId
            ));
            
        } catch (Exception e) {
            log.error("거래 결제 실패 - 거래ID: {}", transactionId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "결제 실패: " + e.getMessage()
            ));
        }
    }
}

