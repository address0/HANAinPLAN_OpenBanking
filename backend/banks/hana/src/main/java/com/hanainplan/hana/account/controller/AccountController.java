package com.hanainplan.hana.account.controller;

import com.hanainplan.hana.account.dto.AccountRequestDto;
import com.hanainplan.hana.account.dto.AccountResponseDto;
import com.hanainplan.hana.account.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/hana/accounts")
@CrossOrigin(origins = "*")
public class AccountController {

    @Autowired
    private AccountService accountService;

    /**
     * 계좌 생성
     */
    @PostMapping
    public ResponseEntity<AccountResponseDto> createAccount(@Valid @RequestBody AccountRequestDto request) {
        try {
            AccountResponseDto response = accountService.createAccount(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 계좌 조회 (계좌번호)
     */
    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponseDto> getAccountByNumber(@PathVariable String accountNumber) {
        Optional<AccountResponseDto> account = accountService.getAccountByNumber(accountNumber);
        return account.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * CI로 계좌 조회
     */
    @GetMapping("/ci/{ci}")
    public ResponseEntity<List<AccountResponseDto>> getAccountsByCi(@PathVariable String ci) {
        List<AccountResponseDto> accounts = accountService.getAccountsByCi(ci);
        return ResponseEntity.ok(accounts);
    }

    /**
     * 모든 계좌 조회
     */
    @GetMapping
    public ResponseEntity<List<AccountResponseDto>> getAllAccounts() {
        List<AccountResponseDto> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    /**
     * 계좌 수정
     */
    @PutMapping("/{accountNumber}")
    public ResponseEntity<AccountResponseDto> updateAccount(
            @PathVariable String accountNumber,
            @Valid @RequestBody AccountRequestDto request) {
        try {
            AccountResponseDto response = accountService.updateAccount(accountNumber, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 계좌 잔액 업데이트
     */
    @PatchMapping("/{accountNumber}/balance")
    public ResponseEntity<AccountResponseDto> updateBalance(
            @PathVariable String accountNumber, 
            @RequestParam BigDecimal balance) {
        try {
            AccountResponseDto response = accountService.updateBalance(accountNumber, balance);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 계좌 삭제
     */
    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String accountNumber) {
        try {
            accountService.deleteAccount(accountNumber);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
