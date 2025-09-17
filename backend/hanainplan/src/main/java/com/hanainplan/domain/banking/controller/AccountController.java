package com.hanainplan.domain.banking.controller;

import com.hanainplan.domain.banking.dto.AccountDto;
import com.hanainplan.domain.banking.dto.CreateAccountRequest;
import com.hanainplan.domain.banking.entity.BankingAccount;
import com.hanainplan.domain.banking.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/banking/accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "계좌 관리", description = "계좌 생성, 조회, 수정 API")
public class AccountController {
    
    private final AccountService accountService;
    
    @PostMapping
    @Operation(summary = "계좌 생성", description = "새로운 계좌를 생성합니다")
    public ResponseEntity<AccountDto> createAccount(
            @Parameter(description = "계좌 생성 요청") @Valid @RequestBody CreateAccountRequest request) {
        
        log.info("계좌 생성 API 호출 - 사용자 ID: {}, 계좌 유형: {}", request.getUserId(), request.getAccountType());
        
        AccountDto account = accountService.createAccount(request);
        return ResponseEntity.ok(account);
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자 계좌 목록 조회", description = "특정 사용자의 모든 계좌를 조회합니다")
    public ResponseEntity<List<AccountDto>> getUserAccounts(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        
        log.info("사용자 계좌 목록 조회 API 호출 - 사용자 ID: {}", userId);
        
        List<AccountDto> accounts = accountService.getUserAccounts(userId);
        return ResponseEntity.ok(accounts);
    }
    
    @GetMapping("/user/{userId}/active")
    @Operation(summary = "사용자 활성 계좌 목록 조회", description = "특정 사용자의 활성 계좌만 조회합니다")
    public ResponseEntity<List<AccountDto>> getActiveUserAccounts(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        
        log.info("사용자 활성 계좌 목록 조회 API 호출 - 사용자 ID: {}", userId);
        
        List<AccountDto> accounts = accountService.getActiveUserAccounts(userId);
        return ResponseEntity.ok(accounts);
    }
    
    @GetMapping("/{accountId}")
    @Operation(summary = "계좌 상세 조회", description = "계좌 ID로 계좌 상세 정보를 조회합니다")
    public ResponseEntity<AccountDto> getAccount(
            @Parameter(description = "계좌 ID") @PathVariable Long accountId) {
        
        log.info("계좌 상세 조회 API 호출 - 계좌 ID: {}", accountId);
        
        Optional<AccountDto> account = accountService.getAccount(accountId);
        return account.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/number/{accountNumber}")
    @Operation(summary = "계좌번호로 계좌 조회", description = "계좌번호로 계좌 정보를 조회합니다")
    public ResponseEntity<AccountDto> getAccountByNumber(
            @Parameter(description = "계좌번호") @PathVariable String accountNumber) {
        
        log.info("계좌번호로 계좌 조회 API 호출 - 계좌번호: {}", accountNumber);
        
        Optional<AccountDto> account = accountService.getAccountByNumber(accountNumber);
        return account.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/user/{userId}/account/{accountId}")
    @Operation(summary = "사용자 계좌 조회", description = "사용자 권한으로 계좌 정보를 조회합니다")
    public ResponseEntity<AccountDto> getUserAccount(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "계좌 ID") @PathVariable Long accountId) {
        
        log.info("사용자 계좌 조회 API 호출 - 사용자 ID: {}, 계좌 ID: {}", userId, accountId);
        
        Optional<AccountDto> account = accountService.getUserAccount(userId, accountId);
        return account.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{accountId}/status")
    @Operation(summary = "계좌 상태 변경", description = "계좌의 상태를 변경합니다")
    public ResponseEntity<AccountDto> updateAccountStatus(
            @Parameter(description = "계좌 ID") @PathVariable Long accountId,
            @Parameter(description = "새 상태") @RequestParam BankingAccount.AccountStatus status) {
        
        log.info("계좌 상태 변경 API 호출 - 계좌 ID: {}, 새 상태: {}", accountId, status);
        
        AccountDto account = accountService.updateAccountStatus(accountId, status);
        return ResponseEntity.ok(account);
    }
    
    @PutMapping("/{accountId}")
    @Operation(summary = "계좌 정보 수정", description = "계좌명과 설명을 수정합니다")
    public ResponseEntity<AccountDto> updateAccount(
            @Parameter(description = "계좌 ID") @PathVariable Long accountId,
            @Parameter(description = "계좌명") @RequestParam(required = false) String accountName,
            @Parameter(description = "계좌 설명") @RequestParam(required = false) String description) {
        
        log.info("계좌 정보 수정 API 호출 - 계좌 ID: {}, 계좌명: {}", accountId, accountName);
        
        AccountDto account = accountService.updateAccount(accountId, accountName, description);
        return ResponseEntity.ok(account);
    }
    
    @GetMapping("/{accountId}/balance")
    @Operation(summary = "계좌 잔액 조회", description = "계좌의 현재 잔액을 조회합니다")
    public ResponseEntity<BigDecimal> getAccountBalance(
            @Parameter(description = "계좌 ID") @PathVariable Long accountId) {
        
        log.info("계좌 잔액 조회 API 호출 - 계좌 ID: {}", accountId);
        
        BigDecimal balance = accountService.getAccountBalance(accountId);
        return ResponseEntity.ok(balance);
    }
    
    @GetMapping("/user/{userId}/stats")
    @Operation(summary = "계좌 통계 조회", description = "사용자의 계좌 유형별 잔액 통계를 조회합니다")
    public ResponseEntity<List<Object[]>> getAccountStats(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        
        log.info("계좌 통계 조회 API 호출 - 사용자 ID: {}", userId);
        
        List<Object[]> stats = accountService.getAccountStats(userId);
        return ResponseEntity.ok(stats);
    }
}
