package com.hanainplan.hana.account.service;

import com.hanainplan.hana.account.dto.AccountResponseDto;
import com.hanainplan.hana.account.entity.Account;
import com.hanainplan.hana.account.entity.Transaction;
import com.hanainplan.hana.account.repository.AccountRepository;
import com.hanainplan.hana.account.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 하나은행 계좌 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    /**
     * 계좌 출금 처리 및 거래내역 저장
     */
    public String processWithdrawal(String accountNumber, BigDecimal amount, String description) throws Exception {
        log.info("하나은행 계좌 출금 처리 시작 - 계좌번호: {}, 금액: {}원", accountNumber, amount);

        // 1. 계좌 조회
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new Exception("계좌를 찾을 수 없습니다: " + accountNumber));

        // 2. 잔액 확인
        if (account.getBalance() == null || account.getBalance().compareTo(amount) < 0) {
            throw new Exception("잔액이 부족합니다. 현재 잔액: " + 
                    (account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO) + "원, 요청 금액: " + amount + "원");
        }

        // 3. 계좌 잔액 차감
        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);
        accountRepository.save(account);

        // 4. 거래내역 저장
        String transactionId = saveWithdrawalTransaction(account, amount, newBalance, description);

        log.info("하나은행 계좌 출금 처리 완료 - 거래ID: {}, 계좌번호: {}, 새 잔액: {}원", 
                transactionId, accountNumber, newBalance);

        return transactionId;
    }

    /**
     * 출금 거래내역을 하나은행 DB에 저장
     */
    private String saveWithdrawalTransaction(Account account, BigDecimal amount, BigDecimal balanceAfter, String description) {
        try {
            // 거래 ID 생성 (HANA-WD-{timestamp}-{random})
            String transactionId = "HANA-WD-" + System.currentTimeMillis() + "-" + 
                    String.format("%04d", (int)(Math.random() * 10000));

            // 거래내역 생성
            Transaction transaction = Transaction.builder()
                    .transactionId(transactionId)
                    .transactionDatetime(LocalDateTime.now())
                    .transactionType("출금")
                    .transactionCategory("기타")
                    .amount(amount)
                    .balanceAfter(balanceAfter)
                    .branchName("하나은행 본점")
                    .account(account)
                    .build();

            transactionRepository.save(transaction);
            
            log.info("하나은행 출금 거래내역 저장 완료 - 거래ID: {}, 계좌번호: {}, 금액: {}원", 
                    transactionId, account.getAccountNumber(), amount);
            
            return transactionId;
            
        } catch (Exception e) {
            log.error("하나은행 출금 거래내역 저장 실패 - 계좌번호: {}, 오류: {}", account.getAccountNumber(), e.getMessage());
            throw new RuntimeException("거래내역 저장 실패: " + e.getMessage());
        }
    }

    /**
     * CI로 계좌 목록 조회
     */
    public List<AccountResponseDto> getAccountsByCi(String ci) {
        log.info("하나은행 계좌 목록 조회 - CI: {}", ci);
        
        List<Account> accounts = accountRepository.findByCustomerCi(ci);
        
        return accounts.stream()
                .map(AccountResponseDto::from)
                .collect(Collectors.toList());
    }
}