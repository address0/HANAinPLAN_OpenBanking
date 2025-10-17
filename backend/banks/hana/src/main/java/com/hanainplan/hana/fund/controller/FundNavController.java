package com.hanainplan.hana.fund.controller;

import com.hanainplan.hana.fund.dto.FundNavUpdateRequest;
import com.hanainplan.hana.fund.entity.FundNav;
import com.hanainplan.hana.fund.service.FundNavService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hana/fund-nav")
@RequiredArgsConstructor
@Slf4j
public class FundNavController {

    private final FundNavService fundNavService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> updateFundNav(@Valid @RequestBody FundNavUpdateRequest request) {
        log.info("펀드 기준가 업데이트 요청: childFundCd={}, navDate={}, nav={}", 
                request.getChildFundCd(), request.getNavDate(), request.getNav());

        try {
            FundNav fundNav = fundNavService.saveOrUpdateNav(
                    request.getChildFundCd(),
                    request.getNavDate(),
                    request.getNav()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "기준가가 업데이트되었습니다");
            response.put("childFundCd", fundNav.getChildFundCd());
            response.put("navDate", fundNav.getNavDate());
            response.put("nav", fundNav.getNav());
            response.put("publishedAt", fundNav.getPublishedAt());

            log.info("펀드 기준가 업데이트 완료: {}", response);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("펀드 기준가 업데이트 실패", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "기준가 업데이트 실패: " + e.getMessage());
            errorResponse.put("childFundCd", request.getChildFundCd());

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> updateFundNavBatch(@Valid @RequestBody List<FundNavUpdateRequest> requests) {
        log.info("펀드 기준가 배치 업데이트 요청: {} 건", requests.size());

        int successCount = 0;
        int failureCount = 0;

        for (FundNavUpdateRequest request : requests) {
            try {
                fundNavService.saveOrUpdateNav(
                        request.getChildFundCd(),
                        request.getNavDate(),
                        request.getNav()
                );
                successCount++;
            } catch (Exception e) {
                failureCount++;
                log.error("기준가 업데이트 실패: childFundCd={}, error={}", 
                        request.getChildFundCd(), e.getMessage());
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "배치 업데이트 완료");
        response.put("totalCount", requests.size());
        response.put("successCount", successCount);
        response.put("failureCount", failureCount);

        log.info("펀드 기준가 배치 업데이트 완료: {}", response);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{childFundCd}/latest")
    public ResponseEntity<FundNav> getLatestNav(@PathVariable String childFundCd) {
        return fundNavService.getLatestNav(childFundCd)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{childFundCd}/{navDate}")
    public ResponseEntity<FundNav> getNavByDate(
            @PathVariable String childFundCd,
            @PathVariable LocalDate navDate) {
        return fundNavService.getNavByDate(childFundCd, navDate)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}




