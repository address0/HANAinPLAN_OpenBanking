package com.hanainplan.hana.account.controller;

import com.hanainplan.hana.account.entity.Transaction;
import com.hanainplan.hana.account.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionRepository transactionRepository;

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<List<Transaction>> getTransactionsByAccount(@PathVariable String accountNumber) {
        log.info("하나은행 거래내역 조회 - 계좌번호: {}", accountNumber);

        try {
            List<Transaction> transactions = transactionRepository
                    .findByAccountAccountNumberOrderByTransactionDatetimeDesc(accountNumber);

            log.info("하나은행 거래내역 조회 완료 - 계좌번호: {}, 거래 수: {}", accountNumber, transactions.size());

            return ResponseEntity.ok(transactions);

        } catch (Exception e) {
            log.error("하나은행 거래내역 조회 실패 - 계좌번호: {}, 오류: {}", accountNumber, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}