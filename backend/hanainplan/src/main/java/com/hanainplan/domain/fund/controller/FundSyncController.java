package com.hanainplan.domain.fund.controller;

import com.hanainplan.domain.fund.service.FundSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/fund-sync")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fund Sync", description = "펀드 상품 동기화 API (관리자용)")
@CrossOrigin(origins = "*")
public class FundSyncController {

    private final FundSyncService fundSyncService;

    @PostMapping("/trigger")
    @Operation(summary = "펀드 상품 수동 동기화", description = "하나은행에서 펀드 상품 데이터를 즉시 동기화합니다")
    public ResponseEntity<Map<String, Object>> triggerFundSync() {
        log.info("POST /api/admin/fund-sync/trigger - 수동 펀드 동기화 요청");

        Map<String, Object> response = new HashMap<>();

        try {
            fundSyncService.syncFundProducts();

            response.put("success", true);
            response.put("message", "펀드 상품 동기화가 완료되었습니다");

            log.info("수동 펀드 동기화 완료");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("수동 펀드 동기화 실패", e);

            response.put("success", false);
            response.put("message", "펀드 상품 동기화에 실패했습니다: " + e.getMessage());

            return ResponseEntity.internalServerError().body(response);
        }
    }
}