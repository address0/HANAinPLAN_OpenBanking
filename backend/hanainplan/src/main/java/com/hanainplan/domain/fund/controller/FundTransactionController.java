package com.hanainplan.domain.fund.controller;

import com.hanainplan.domain.fund.dto.FundTransactionDto;
import com.hanainplan.domain.fund.dto.FundTransactionStatsDto;
import com.hanainplan.domain.fund.service.FundTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banking/fund-transactions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fund Transaction", description = "펀드 거래 내역 API")
@CrossOrigin(origins = "*")
public class FundTransactionController {

    private final FundTransactionService fundTransactionService;

    @GetMapping("/customer/{customerCi}")
    @Operation(summary = "거래 내역 조회", description = "고객의 모든 펀드 거래 내역을 조회합니다")
    public ResponseEntity<List<FundTransactionDto>> getCustomerTransactions(
            @Parameter(description = "고객 CI", required = true)
            @PathVariable String customerCi
    ) {
        log.info("GET /api/banking/fund-transactions/customer/{} - 거래 내역 조회", customerCi);

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
        log.info("GET /api/banking/fund-transactions/subscription/{} - 가입 거래 내역 조회", subscriptionId);

        List<FundTransactionDto> transactions = fundTransactionService.getSubscriptionTransactions(subscriptionId);

        log.info("거래 내역 조회 완료 - {}건", transactions.size());
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/customer/{customerCi}/stats")
    @Operation(summary = "거래 통계", description = "고객의 펀드 거래 통계를 조회합니다")
    public ResponseEntity<FundTransactionStatsDto> getTransactionStats(
            @Parameter(description = "고객 CI", required = true)
            @PathVariable String customerCi
    ) {
        log.info("GET /api/banking/fund-transactions/customer/{}/stats - 거래 통계 조회", customerCi);

        FundTransactionStatsDto stats = fundTransactionService.getTransactionStats(customerCi);

        log.info("거래 통계 조회 완료");
        return ResponseEntity.ok(stats);
    }
}