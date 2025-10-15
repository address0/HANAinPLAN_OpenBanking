package com.hanainplan.kookmin.account.service;

import com.hanainplan.kookmin.account.dto.AccountRequestDto;
import com.hanainplan.kookmin.account.dto.AccountResponseDto;
import com.hanainplan.kookmin.account.entity.Account;
import com.hanainplan.kookmin.account.entity.Transaction;
import com.hanainplan.kookmin.account.repository.AccountRepository;
import com.hanainplan.kookmin.account.repository.TransactionRepository;
import com.hanainplan.kookmin.user.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private static final String[] KOOKMIN_PATTERNS = {
        "123", "124", "125", "126", "127", "128", "129"
    };

    private String generateAccountNumber() {
        Random random = new Random();
        String pattern = KOOKMIN_PATTERNS[random.nextInt(KOOKMIN_PATTERNS.length)];

        StringBuilder accountNumber = new StringBuilder(pattern);
        for (int i = 0; i < 13; i++) {
            accountNumber.append(random.nextInt(10));
        }

        String generatedNumber = accountNumber.toString();

        if (accountRepository.existsById(generatedNumber)) {
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
        return AccountResponseDto.from(savedAccount);
    }

    @Transactional(readOnly = true)
    public Optional<AccountResponseDto> getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .map(AccountResponseDto::from);
    }

    @Transactional(readOnly = true)
    public List<AccountResponseDto> getAccountsByCi(String ci) {
        List<Account> accounts = accountRepository.findByCustomerCi(ci);
        return accounts.stream()
                .map(AccountResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AccountResponseDto> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        return accounts.stream()
                .map(AccountResponseDto::from)
                .collect(Collectors.toList());
    }

    public AccountResponseDto updateAccount(String accountNumber, AccountRequestDto request) {
        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다: " + accountNumber));

        if (!account.getCustomerCi().equals(request.getCustomerCi()) && 
            !customerRepository.existsByCi(request.getCustomerCi())) {
            throw new IllegalArgumentException("존재하지 않는 고객 CI입니다: " + request.getCustomerCi());
        }

        account.setAccountType(request.getAccountType());
        account.setBalance(request.getBalance());
        account.setOpeningDate(request.getOpeningDate());
        account.setCustomerCi(request.getCustomerCi());

        Account updatedAccount = accountRepository.save(account);
        return AccountResponseDto.from(updatedAccount);
    }

    public void deleteAccount(String accountNumber) {
        if (!accountRepository.existsById(accountNumber)) {
            throw new IllegalArgumentException("계좌를 찾을 수 없습니다: " + accountNumber);
        }
        accountRepository.deleteById(accountNumber);
    }

    public AccountResponseDto updateBalance(String accountNumber, java.math.BigDecimal newBalance) {
        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다: " + accountNumber));

        account.setBalance(newBalance);
        Account updatedAccount = accountRepository.save(account);
        return AccountResponseDto.from(updatedAccount);
    }

    public String processWithdrawal(String accountNumber, BigDecimal amount, String description) throws Exception {
        log.info("국민은행 계좌 출금 처리 시작 - 계좌번호: {}, 금액: {}원", accountNumber, amount);

        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new Exception("국민은행 계좌를 찾을 수 없습니다: " + accountNumber));

        if (account.getBalance() == null || account.getBalance().compareTo(amount) < 0) {
            throw new Exception("국민은행 계좌 잔액이 부족합니다. 현재 잔액: " + 
                    (account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO) + "원, 요청 금액: " + amount + "원");
        }

        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);
        accountRepository.save(account);

        String transactionId = saveWithdrawalTransaction(account, amount, newBalance, description);

        log.info("국민은행 계좌 출금 처리 완료 - 거래ID: {}, 계좌번호: {}, 새 잔액: {}원", 
                transactionId, accountNumber, newBalance);

        return transactionId;
    }

    public String processDeposit(String accountNumber, BigDecimal amount, String description) throws Exception {
        log.info("국민은행 계좌 입금 처리 시작 - 계좌번호: {}, 금액: {}원", accountNumber, amount);

        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new Exception("국민은행 계좌를 찾을 수 없습니다: " + accountNumber));

        BigDecimal newBalance = (account.getBalance() != null ? account.getBalance() : BigDecimal.ZERO).add(amount);
        account.setBalance(newBalance);
        accountRepository.save(account);

        String transactionId = saveDepositTransaction(account, amount, newBalance, description);

        log.info("국민은행 계좌 입금 처리 완료 - 거래ID: {}, 계좌번호: {}, 새 잔액: {}원", 
                transactionId, accountNumber, newBalance);

        return transactionId;
    }

    private String saveWithdrawalTransaction(Account account, BigDecimal amount, BigDecimal balanceAfter, String description) {
        try {
            String transactionId = "KB-WD-" + System.currentTimeMillis() + "-" + 
                    String.format("%04d", (int)(Math.random() * 10000));

            Transaction transaction = Transaction.builder()
                    .transactionId(transactionId)
                    .transactionDatetime(LocalDateTime.now())
                    .transactionType("출금")
                    .transactionCategory("기타")
                    .amount(amount)
                    .balanceAfter(balanceAfter)
                    .branchName("국민은행 본점")
                    .account(account)
                    .build();

            transactionRepository.save(transaction);

            log.info("국민은행 출금 거래내역 저장 완료 - 거래ID: {}, 계좌번호: {}, 금액: {}원", 
                    transactionId, account.getAccountNumber(), amount);

            return transactionId;

        } catch (Exception e) {
            log.error("국민은행 출금 거래내역 저장 실패 - 계좌번호: {}, 오류: {}", account.getAccountNumber(), e.getMessage());
            throw new RuntimeException("거래내역 저장 실패: " + e.getMessage());
        }
    }

    private String saveDepositTransaction(Account account, BigDecimal amount, BigDecimal balanceAfter, String description) {
        try {
            String transactionId = "KB-DP-" + System.currentTimeMillis() + "-" + 
                    String.format("%04d", (int)(Math.random() * 10000));

            Transaction transaction = Transaction.builder()
                    .transactionId(transactionId)
                    .transactionDatetime(LocalDateTime.now())
                    .transactionType("입금")
                    .transactionCategory("기타")
                    .amount(amount)
                    .balanceAfter(balanceAfter)
                    .branchName("국민은행 본점")
                    .account(account)
                    .build();

            transactionRepository.save(transaction);

            log.info("국민은행 입금 거래내역 저장 완료 - 거래ID: {}, 계좌번호: {}, 금액: {}원", 
                    transactionId, account.getAccountNumber(), amount);

            return transactionId;

        } catch (Exception e) {
            log.error("국민은행 입금 거래내역 저장 실패 - 계좌번호: {}, 오류: {}", account.getAccountNumber(), e.getMessage());
            throw new RuntimeException("거래내역 저장 실패: " + e.getMessage());
        }
    }
}