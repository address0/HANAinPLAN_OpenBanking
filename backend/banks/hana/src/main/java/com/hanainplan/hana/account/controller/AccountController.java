package com.hanainplan.hana.account.controller;

import com.hanainplan.hana.account.dto.AccountRequestDto;
import com.hanainplan.hana.account.dto.AccountResponseDto;
import com.hanainplan.hana.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hana/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponseDto> createAccount(@Valid @RequestBody AccountRequestDto request) {
        try {
            AccountResponseDto response = accountService.createAccount(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("계좌 생성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponseDto> getAccountByNumber(@PathVariable String accountNumber) {
        try {
            AccountResponseDto account = accountService.getAccountByNumber(accountNumber);
            return ResponseEntity.ok(account);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/ci/{ci}")
    public ResponseEntity<List<AccountResponseDto>> getAccountsByCi(@PathVariable String ci) {
        List<AccountResponseDto> accounts = accountService.getAccountsByCi(ci);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponseDto>> getAllAccounts() {
        List<AccountResponseDto> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    @PostMapping("/withdrawal")
    public ResponseEntity<Map<String, Object>> withdrawal(@RequestBody WithdrawalRequest request) {
        log.info("하나은행 계좌 출금 요청 - 계좌번호: {}, 금액: {}원", request.getAccountNumber(), request.getAmount());

        Map<String, Object> response = new HashMap<>();

        try {
            String transactionId = accountService.processWithdrawal(
                request.getAccountNumber(),
                request.getAmount(),
                request.getDescription() != null ? request.getDescription() : "출금"
            );

            response.put("success", true);
            response.put("message", "출금이 완료되었습니다");
            response.put("transactionId", transactionId);
            response.put("accountNumber", request.getAccountNumber());
            response.put("amount", request.getAmount());

            log.info("하나은행 계좌 출금 완료 - 거래ID: {}, 계좌번호: {}, 금액: {}원", 
                    transactionId, request.getAccountNumber(), request.getAmount());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("하나은행 계좌 출금 실패 - 계좌번호: {}, 오류: {}", request.getAccountNumber(), e.getMessage());

            response.put("success", false);
            response.put("message", "출금 처리 중 오류가 발생했습니다: " + e.getMessage());
            response.put("accountNumber", request.getAccountNumber());
            response.put("amount", request.getAmount());

            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<Map<String, Object>> deposit(@RequestBody DepositRequest request) {
        log.info("하나은행 계좌 입금 요청 - 계좌번호: {}, 금액: {}원", request.getAccountNumber(), request.getAmount());

        Map<String, Object> response = new HashMap<>();

        try {
            String transactionId = accountService.processDeposit(
                request.getAccountNumber(),
                request.getAmount(),
                request.getDescription() != null ? request.getDescription() : "입금"
            );

            response.put("success", true);
            response.put("message", "입금이 완료되었습니다");
            response.put("transactionId", transactionId);
            response.put("accountNumber", request.getAccountNumber());
            response.put("amount", request.getAmount());

            log.info("하나은행 계좌 입금 완료 - 거래ID: {}, 계좌번호: {}, 금액: {}원", 
                    transactionId, request.getAccountNumber(), request.getAmount());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("하나은행 계좌 입금 실패 - 계좌번호: {}, 오류: {}", request.getAccountNumber(), e.getMessage());

            response.put("success", false);
            response.put("message", "입금 처리 중 오류가 발생했습니다: " + e.getMessage());
            response.put("accountNumber", request.getAccountNumber());
            response.put("amount", request.getAmount());

            return ResponseEntity.badRequest().body(response);
        }
    }

    public static class WithdrawalRequest {
        private String accountNumber;
        private BigDecimal amount;
        private String description;

        public String getAccountNumber() {
            return accountNumber;
        }

        public void setAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class DepositRequest {
        private String accountNumber;
        private BigDecimal amount;
        private String description;

        public String getAccountNumber() {
            return accountNumber;
        }

        public void setAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}