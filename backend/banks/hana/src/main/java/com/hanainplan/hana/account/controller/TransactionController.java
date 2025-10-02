package com.hanainplan.hana.account.controller;

import com.hanainplan.hana.account.entity.Transaction;
import com.hanainplan.hana.account.repository.TransactionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 거래내역 조회 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction", description = "거래내역 API")
public class TransactionController {

    private final TransactionRepository transactionRepository;

    /**
     * 계좌별 거래내역 조회
     */
    @Operation(summary = "계좌별 거래내역 조회", description = "특정 계좌의 모든 거래내역을 조회합니다")
    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<List<Transaction>> getTransactionsByAccount(
            @PathVariable String accountNumber) {
        log.info("계좌 거래내역 조회 요청: accountNumber={}", accountNumber);
        
        List<Transaction> transactions = transactionRepository
                .findByAccountAccountNumberOrderByTransactionDatetimeDesc(accountNumber);
        
        log.info("거래내역 조회 완료: count={}", transactions.size());
        return ResponseEntity.ok(transactions);
    }

    /**
     * 계좌별 기간별 거래내역 조회
     */
    @Operation(summary = "계좌별 기간별 거래내역 조회", description = "특정 계좌의 특정 기간 거래내역을 조회합니다")
    @GetMapping("/account/{accountNumber}/range")
    public ResponseEntity<List<Transaction>> getTransactionsByAccountAndDateRange(
            @PathVariable String accountNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("계좌 기간별 거래내역 조회 요청: accountNumber={}, startDate={}, endDate={}", 
                accountNumber, startDate, endDate);
        
        List<Transaction> transactions = transactionRepository
                .findByAccountNumberAndDateRange(accountNumber, startDate, endDate);
        
        log.info("거래내역 조회 완료: count={}", transactions.size());
        return ResponseEntity.ok(transactions);
    }

    /**
     * 고객 CI별 거래내역 조회
     */
    @Operation(summary = "고객 CI별 거래내역 조회", description = "특정 고객의 모든 거래내역을 조회합니다")
    @GetMapping("/customer/{customerCi}")
    public ResponseEntity<List<Transaction>> getTransactionsByCustomerCi(
            @PathVariable String customerCi) {
        log.info("고객 거래내역 조회 요청: customerCi={}", customerCi);
        
        List<Transaction> transactions = transactionRepository
                .findByCustomerCiOrderByTransactionDatetimeDesc(customerCi);
        
        log.info("거래내역 조회 완료: count={}", transactions.size());
        return ResponseEntity.ok(transactions);
    }

    /**
     * 고객 CI별 기간별 거래내역 조회
     */
    @Operation(summary = "고객 CI별 기간별 거래내역 조회", description = "특정 고객의 특정 기간 거래내역을 조회합니다")
    @GetMapping("/customer/{customerCi}/range")
    public ResponseEntity<List<Transaction>> getTransactionsByCustomerCiAndDateRange(
            @PathVariable String customerCi,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("고객 기간별 거래내역 조회 요청: customerCi={}, startDate={}, endDate={}", 
                customerCi, startDate, endDate);
        
        List<Transaction> transactions = transactionRepository
                .findByCustomerCiAndDateRange(customerCi, startDate, endDate);
        
        log.info("거래내역 조회 완료: count={}", transactions.size());
        return ResponseEntity.ok(transactions);
    }
}


