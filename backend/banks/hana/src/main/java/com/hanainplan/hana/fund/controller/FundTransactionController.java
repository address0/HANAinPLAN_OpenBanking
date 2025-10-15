package com.hanainplan.hana.fund.controller;

import com.hanainplan.hana.fund.dto.FundTransactionDto;
import com.hanainplan.hana.fund.dto.FundTransactionStatsDto;
import com.hanainplan.hana.fund.service.FundTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/hana/fund-transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Hana Fund Transaction", description = "하나은행 펀드 거래 내역 API")
public class FundTransactionController {

    private final FundTransactionService fundTransactionService;

    @GetMapping("/customer/{customerCi}")
    @Operation(summary = "거래 내역 조회", description = "고객의 모든 펀드 거래 내역을 조회합니다")
    public ResponseEntity<List<FundTransactionDto>> getCustomerTransactions(
            @Parameter(description = "고객 CI", required = true)
            @PathVariable String customerCi
    ) {
        log.info("GET /api/hana/fund-transactions/customer/{} - 거래 내역 조회", customerCi);

        List<FundTransactionDto> transactions = fundTransactionService.getCustomerTransactions(customerCi);

        log.info("거래 내역 조회 완료 - {}건", transactions.size());
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/subscription/{subscriptionId}")
    @Operation(summary = "가입별 거래 내역", description = "특정 가입의 거래 내역을 조회합니다")
    public ResponseEntity<List<FundTransactionDto>> getSubscriptionTransactions(
            @Parameter(description = "가입 ID", required = true)
            @PathVariable Long subscriptionId
    ) {
        log.info("GET /api/hana/fund-transactions/subscription/{} - 가입 거래 내역 조회", subscriptionId);

        List<FundTransactionDto> transactions = fundTransactionService.getSubscriptionTransactions(subscriptionId);

        log.info("거래 내역 조회 완료 - {}건", transactions.size());
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/customer/{customerCi}/date-range")
    @Operation(summary = "기간별 거래 내역", description = "특정 기간의 거래 내역을 조회합니다")
    public ResponseEntity<List<FundTransactionDto>> getTransactionsByDateRange(
            @Parameter(description = "고객 CI", required = true)
            @PathVariable String customerCi,
            @Parameter(description = "시작일 (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "종료일 (yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("GET /api/hana/fund-transactions/customer/{}/date-range - 기간별 조회: {} ~ {}", 
                customerCi, startDate, endDate);

        List<FundTransactionDto> transactions = fundTransactionService
                .getTransactionsByDateRange(customerCi, startDate, endDate);

        log.info("거래 내역 조회 완료 - {}건", transactions.size());
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/customer/{customerCi}/type/{transactionType}")
    @Operation(summary = "유형별 거래 내역", description = "매수/매도 내역만 조회합니다 (BUY, SELL)")
    public ResponseEntity<List<FundTransactionDto>> getTransactionsByType(
            @Parameter(description = "고객 CI", required = true)
            @PathVariable String customerCi,
            @Parameter(description = "거래 유형 (BUY, SELL)", required = true)
            @PathVariable String transactionType
    ) {
        log.info("GET /api/hana/fund-transactions/customer/{}/type/{} - 유형별 조회", 
                customerCi, transactionType);

        List<FundTransactionDto> transactions = fundTransactionService
                .getTransactionsByType(customerCi, transactionType);

        log.info("거래 내역 조회 완료 - {}건", transactions.size());
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/customer/{customerCi}/stats")
    @Operation(summary = "거래 통계", description = "고객의 펀드 거래 통계를 조회합니다")
    public ResponseEntity<FundTransactionStatsDto> getTransactionStats(
            @Parameter(description = "고객 CI", required = true)
            @PathVariable String customerCi
    ) {
        log.info("GET /api/hana/fund-transactions/customer/{}/stats - 거래 통계 조회", customerCi);

        FundTransactionStatsDto stats = fundTransactionService.getTransactionStats(customerCi);

        log.info("거래 통계 조회 완료 - 총 거래: {}건, 실현손익: {}원", 
                stats.getTotalTransactionCount(), stats.getTotalRealizedProfit());
        return ResponseEntity.ok(stats);
    }
}