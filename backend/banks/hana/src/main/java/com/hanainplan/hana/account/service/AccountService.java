package com.hanainplan.hana.account.service;

import com.hanainplan.hana.account.dto.AccountRequestDto;
import com.hanainplan.hana.account.dto.AccountResponseDto;
import com.hanainplan.hana.account.entity.Account;
import com.hanainplan.hana.account.entity.Transaction;
import com.hanainplan.hana.account.repository.AccountRepository;
import com.hanainplan.hana.account.repository.TransactionRepository;
import com.hanainplan.hana.user.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;

    private static final String[] HANA_PATTERNS = {
        "100", "101", "102", "103", "104", "105", "110", "111"
    };

    private String generateAccountNumber() {
        Random random = new Random();
        String pattern = HANA_PATTERNS[random.nextInt(HANA_PATTERNS.length)];

        StringBuilder accountNumber = new StringBuilder(pattern);
        for (int i = 0; i < 13; i++) {
            accountNumber.append(random.nextInt(10));
        }

        String generatedNumber = accountNumber.toString();

        if (accountRepository.existsByAccountNumber(generatedNumber)) {
            return generateAccountNumber();
        }

        return generatedNumber;
    }

    public AccountResponseDto createAccount(AccountRequestDto request) {
        if (!customerRepository.existsByCi(request.getCustomerCi())) {
            throw new IllegalArgumentException("존재하지 않는 고객 CI입니다: " + request.getCustomerCi());
        }

        String accountNumber = generateAccountNumber();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .accountType(request.getAccountType())
                .balance(request.getBalance())
                .openingDate(request.getOpeningDate())
                .customerCi(request.getCustomerCi())
                .build();

        Account savedAccount = accountRepository.save(account);
        log.info("하나은행 계좌 생성 완료 - 계좌번호: {}, CI: {}", accountNumber, request.getCustomerCi());

        return AccountResponseDto.from(savedAccount);
    }

    @Transactional(readOnly = true)
    public AccountResponseDto getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다: " + accountNumber));
        return AccountResponseDto.from(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponseDto> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        return accounts.stream()
                .map(AccountResponseDto::from)
                .collect(Collectors.toList());
    }

    public String processWithdrawal(String accountNumber, BigDecimal amount, String description) throws Exception {
        log.info("하나은행 계좌 출금 처리 시작 - 계좌번호: {}, 금액: {}원", accountNumber, amount);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new Exception("계좌를 찾을 수 없습니다: " + accountNumber));

        if (account.getBalance() == null || account.getBalance().compareTo(amount) < 0) {
            throw new Exception("잔액이 부족합니다. 현재 잔액: " + 
                    (account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO) + "원, 요청 금액: " + amount + "원");
        }

        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);
        accountRepository.save(account);

        String transactionId = saveWithdrawalTransaction(account, amount, newBalance, description);

        log.info("하나은행 계좌 출금 처리 완료 - 거래ID: {}, 계좌번호: {}, 새 잔액: {}원", 
                transactionId, accountNumber, newBalance);

        return transactionId;
    }

    private String saveWithdrawalTransaction(Account account, BigDecimal amount, BigDecimal balanceAfter, String description) {
        try {
            String transactionId = "HANA-WD-" + System.currentTimeMillis() + "-" + 
                    String.format("%04d", (int)(Math.random() * 10000));

            Transaction transaction = Transaction.builder()
                    .transactionId(transactionId)
                    .accountNumber(account.getAccountNumber())
                    .transactionDatetime(LocalDateTime.now())
                    .transactionType("출금")
                    .transactionCategory("기타")
                    .transactionStatus("COMPLETED")
                    .transactionDirection("DEBIT")
                    .amount(amount)
                    .balanceAfter(balanceAfter)
                    .branchName("하나은행 본점")
                    .description(description)
                    .referenceNumber(transactionId)
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

    public String processDeposit(String accountNumber, BigDecimal amount, String description) throws Exception {
        log.info("하나은행 계좌 입금 처리 시작 - 계좌번호: {}, 금액: {}원", accountNumber, amount);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new Exception("계좌를 찾을 수 없습니다: " + accountNumber));

        BigDecimal newBalance = (account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO).add(amount);
        account.setBalance(newBalance);
        accountRepository.save(account);

        String transactionId = saveDepositTransaction(account, amount, newBalance, description);

        log.info("하나은행 계좌 입금 처리 완료 - 거래ID: {}, 계좌번호: {}, 새 잔액: {}원", 
                transactionId, accountNumber, newBalance);

        return transactionId;
    }

    private String saveDepositTransaction(Account account, BigDecimal amount, BigDecimal balanceAfter, String description) {
        try {
            String transactionId = "HANA-DP-" + System.currentTimeMillis() + "-" + 
                    String.format("%04d", (int)(Math.random() * 10000));

            Transaction transaction = Transaction.builder()
                    .transactionId(transactionId)
                    .accountNumber(account.getAccountNumber())
                    .transactionDatetime(LocalDateTime.now())
                    .transactionType("입금")
                    .transactionCategory("기타")
                    .transactionStatus("COMPLETED")
                    .transactionDirection("CREDIT")
                    .amount(amount)
                    .balanceAfter(balanceAfter)
                    .description(description)
                    .branchName("하나은행 본점")
                    .referenceNumber(transactionId)
                    .account(account)
                    .build();

            transactionRepository.save(transaction);

            log.info("하나은행 입금 거래내역 저장 완료 - 거래ID: {}, 계좌번호: {}, 금액: {}원", 
                    transactionId, account.getAccountNumber(), amount);

            return transactionId;

        } catch (Exception e) {
            log.error("하나은행 입금 거래내역 저장 실패 - 계좌번호: {}, 오류: {}", account.getAccountNumber(), e.getMessage());
            throw new RuntimeException("거래내역 저장 실패: " + e.getMessage());
        }
    }

    public List<AccountResponseDto> getAccountsByCi(String ci) {
        log.info("하나은행 계좌 목록 조회 - CI: {}", ci);

        List<Account> accounts = accountRepository.findByCustomerCi(ci);

        return accounts.stream()
                .map(AccountResponseDto::from)
                .collect(Collectors.toList());
    }
}