package com.hanainplan.shinhan.account.controller;

import com.hanainplan.shinhan.account.dto.AccountRequestDto;
import com.hanainplan.shinhan.account.dto.AccountResponseDto;
import com.hanainplan.shinhan.account.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/shinhan/accounts")
@CrossOrigin(origins = "*")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponseDto> createAccount(@Valid @RequestBody AccountRequestDto request) {
        try {
            AccountResponseDto response = accountService.createAccount(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponseDto> getAccountByNumber(@PathVariable String accountNumber) {
        Optional<AccountResponseDto> account = accountService.getAccountByNumber(accountNumber);
        return account.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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

    @PutMapping("/{accountNumber}")
    public ResponseEntity<AccountResponseDto> updateAccount(
            @PathVariable String accountNumber,
            @Valid @RequestBody AccountRequestDto request) {
        try {
            AccountResponseDto response = accountService.updateAccount(accountNumber, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String accountNumber) {
        try {
            accountService.deleteAccount(accountNumber);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{accountNumber}/balance")
    public ResponseEntity<AccountResponseDto> updateBalance(
            @PathVariable String accountNumber,
            @RequestBody BalanceUpdateRequest request) {
        try {
            AccountResponseDto response = accountService.updateBalance(accountNumber, request.getBalance());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/withdrawal")
    public ResponseEntity<Object> processWithdrawal(@RequestBody WithdrawalRequest request) {
        try {
            String transactionId = accountService.processWithdrawal(
                    request.getAccountNumber(),
                    request.getAmount(),
                    request.getDescription()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "출금 처리 완료");
            response.put("transactionId", transactionId);
            response.put("accountNumber", request.getAccountNumber());
            response.put("amount", request.getAmount());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "출금 처리 실패: " + e.getMessage());
            errorResponse.put("accountNumber", request.getAccountNumber());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<Object> processDeposit(@RequestBody DepositRequest request) {
        try {
            String transactionId = accountService.processDeposit(
                    request.getAccountNumber(),
                    request.getAmount(),
                    request.getDescription()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "입금 처리 완료");
            response.put("transactionId", transactionId);
            response.put("accountNumber", request.getAccountNumber());
            response.put("amount", request.getAmount());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "입금 처리 실패: " + e.getMessage());
            errorResponse.put("accountNumber", request.getAccountNumber());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    public static class BalanceUpdateRequest {
        private BigDecimal balance;

        public BigDecimal getBalance() {
            return balance;
        }

        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }
    }

    public static class WithdrawalRequest {
        private String accountNumber;
        private BigDecimal amount;
        private String description;
        private String memo;

        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getMemo() { return memo; }
        public void setMemo(String memo) { this.memo = memo; }
    }

    public static class DepositRequest {
        private String accountNumber;
        private BigDecimal amount;
        private String description;

        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}