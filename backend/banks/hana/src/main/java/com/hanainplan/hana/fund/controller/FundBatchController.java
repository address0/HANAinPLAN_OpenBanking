package com.hanainplan.hana.fund.controller;

import com.hanainplan.hana.fund.batch.FundNavBatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/hana/fund-batch")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Hana Fund Batch", description = "하나은행 펀드 배치 작업 API (관리자용)")
public class FundBatchController {

    private final FundNavBatchService fundNavBatchService;

    @PostMapping("/update-nav")
    @Operation(summary = "수동 기준가 업데이트", description = "일일 기준가 업데이트 배치를 수동으로 실행합니다")
    public ResponseEntity<Map<String, String>> manualUpdateNav() {
        log.info("POST /api/hana/fund-batch/update-nav - 수동 기준가 업데이트 요청");

        try {
            fundNavBatchService.manualUpdateNav();

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "기준가 업데이트가 완료되었습니다"
            ));
        } catch (Exception e) {
            log.error("수동 기준가 업데이트 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "기준가 업데이트 실패: " + e.getMessage()
            ));
        }
    }
}