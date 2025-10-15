package com.hanainplan.domain.banking.controller;

import com.hanainplan.domain.banking.dto.InterestRateDto;
import com.hanainplan.domain.banking.service.InterestRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banking/interest-rates")
@Tag(name = "금리 정보 조회", description = "각 은행의 정기예금 금리 정보 조회 API")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class InterestRateController {

    private final InterestRateService interestRateService;

    @GetMapping("/all")
    @Operation(summary = "전체 은행 금리 조회", description = "하나/국민/신한 은행의 모든 정기예금 금리 정보를 조회합니다.")
    public ResponseEntity<List<InterestRateDto>> getAllInterestRates() {
        log.info("전체 은행 금리 조회 API 호출");

        try {
            List<InterestRateDto> rates = interestRateService.getAllInterestRates();
            return ResponseEntity.ok(rates);
        } catch (Exception e) {
            log.error("금리 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}