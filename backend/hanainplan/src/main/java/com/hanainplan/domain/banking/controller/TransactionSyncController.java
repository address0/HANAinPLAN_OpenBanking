package com.hanainplan.domain.banking.controller;

import com.hanainplan.domain.banking.service.TransactionSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/banking")
@RequiredArgsConstructor
@Tag(name = "Transaction Sync", description = "거래내역 동기화 API")
public class TransactionSyncController {

    private final TransactionSyncService transactionSyncService;

    @Operation(summary = "계좌 거래내역 동기화", description = "은행 서버로부터 특정 계좌의 거래내역을 조회하여 동기화합니다")
    @PostMapping("/accounts/{accountNumber}/sync-transactions")
    public ResponseEntity<Map<String, Object>> syncTransactionsByAccount(
            @PathVariable String accountNumber) {
        log.info("계좌 거래내역 동기화 요청: accountNumber={}", accountNumber);

        try {
            int syncedCount = transactionSyncService.syncTransactionsByAccount(accountNumber);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "거래내역 동기화가 완료되었습니다");
            response.put("accountNumber", accountNumber);
            response.put("syncedCount", syncedCount);

            log.info("계좌 거래내역 동기화 완료: accountNumber={}, syncedCount={}", accountNumber, syncedCount);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("계좌 거래내역 동기화 실패: accountNumber={}, error={}", accountNumber, e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "거래내역 동기화에 실패했습니다: " + e.getMessage());
            response.put("accountNumber", accountNumber);

            return ResponseEntity.status(500).body(response);
        }
    }

    @Operation(summary = "사용자 전체 계좌 거래내역 동기화", description = "사용자의 모든 계좌에 대해 거래내역을 동기화합니다")
    @PostMapping("/users/{userId}/sync-all-transactions")
    public ResponseEntity<Map<String, Object>> syncAllTransactionsByUser(
            @PathVariable Long userId) {
        log.info("사용자 전체 계좌 거래내역 동기화 요청: userId={}", userId);

        try {
            int totalSyncedCount = transactionSyncService.syncAllTransactionsByUser(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "전체 계좌 거래내역 동기화가 완료되었습니다");
            response.put("userId", userId);
            response.put("totalSyncedCount", totalSyncedCount);

            log.info("사용자 전체 계좌 거래내역 동기화 완료: userId={}, totalSyncedCount={}", userId, totalSyncedCount);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("사용자 전체 계좌 거래내역 동기화 실패: userId={}, error={}", userId, e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "전체 계좌 거래내역 동기화에 실패했습니다: " + e.getMessage());
            response.put("userId", userId);

            return ResponseEntity.status(500).body(response);
        }
    }
}