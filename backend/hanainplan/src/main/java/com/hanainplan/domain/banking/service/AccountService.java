package com.hanainplan.domain.banking.service;

import com.hanainplan.domain.banking.dto.AccountDto;
import com.hanainplan.domain.banking.dto.CreateAccountRequest;
import com.hanainplan.domain.banking.entity.BankingAccount;
import com.hanainplan.domain.banking.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public AccountDto createAccount(CreateAccountRequest request) {
        log.info("계좌 생성 요청 - 사용자 ID: {}, 계좌 유형: {}, 계좌명: {}", 
                request.getUserId(), request.getAccountType(), request.getAccountName());

        String accountNumber;
        do {
            accountNumber = BankingAccount.generateAccountNumber();
        } while (accountRepository.existsByAccountNumber(accountNumber));

        LocalDateTime expiryDate = null;
        if (request.getDepositPeriod() != null && 
            (request.getAccountType() == BankingAccount.AccountType.SAVINGS)) {
            expiryDate = LocalDateTime.now().plusMonths(request.getDepositPeriod());
        }

        BankingAccount account = BankingAccount.builder()
                .userId(request.getUserId())
                .accountNumber(accountNumber)
                .accountName(request.getAccountName())
                .accountType(request.getAccountType())
                .accountStatus(BankingAccount.AccountStatus.ACTIVE)
                .balance(request.getInitialBalance() != null ? request.getInitialBalance() : BigDecimal.ZERO)
                .currencyCode("KRW")
                .openedDate(LocalDateTime.now())
                .expiryDate(expiryDate)
                .description(request.getDescription())
                .purpose(request.getPurpose())
                .monthlyDepositAmount(request.getMonthlyDepositAmount())
                .depositPeriod(request.getDepositPeriod())
                .interestPaymentMethod(request.getInterestPaymentMethod())
                .accountPassword(request.getAccountPassword())
                .build();

        BankingAccount savedAccount = accountRepository.save(account);
        log.info("계좌 생성 완료 - 계좌 ID: {}, 계좌번호: {}", savedAccount.getAccountId(), savedAccount.getAccountNumber());

        return AccountDto.fromEntity(savedAccount);
    }

    @Transactional
    public AccountDto createAccount(Long userId, Integer accountType, String accountName, 
                                   BigDecimal initialBalance, String description) {
        log.info("계좌 생성 요청 - 사용자 ID: {}, 계좌 유형: {}, 계좌명: {}", userId, accountType, accountName);

        String accountNumber;
        do {
            accountNumber = BankingAccount.generateAccountNumber();
        } while (accountRepository.existsByAccountNumber(accountNumber));

        BankingAccount account = BankingAccount.builder()
                .userId(userId)
                .accountNumber(accountNumber)
                .accountName(accountName)
                .accountType(accountType)
                .accountStatus(BankingAccount.AccountStatus.ACTIVE)
                .balance(initialBalance != null ? initialBalance : BigDecimal.ZERO)
                .currencyCode("KRW")
                .openedDate(LocalDateTime.now())
                .description(description)
                .build();

        BankingAccount savedAccount = accountRepository.save(account);
        log.info("계좌 생성 완료 - 계좌 ID: {}, 계좌번호: {}", savedAccount.getAccountId(), savedAccount.getAccountNumber());

        return AccountDto.fromEntity(savedAccount);
    }

    public List<AccountDto> getUserAccounts(Long userId) {
        log.info("사용자 계좌 목록 조회 - 사용자 ID: {}", userId);

        List<BankingAccount> accounts = accountRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return accounts.stream()
                .map(AccountDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<AccountDto> getActiveUserAccounts(Long userId) {
        log.info("활성 계좌 목록 조회 - 사용자 ID: {}", userId);

        List<BankingAccount> accounts = accountRepository.findActiveAccountsByUserId(userId);
        return accounts.stream()
                .map(AccountDto::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<AccountDto> getAccount(Long accountId) {
        log.info("계좌 상세 조회 - 계좌 ID: {}", accountId);

        return accountRepository.findById(accountId)
                .map(AccountDto::fromEntity);
    }

    public Optional<AccountDto> getAccountByNumber(String accountNumber) {
        log.info("계좌번호로 계좌 조회 - 계좌번호: {}", accountNumber);

        return accountRepository.findByAccountNumber(accountNumber)
                .map(AccountDto::fromEntity);
    }

    public Optional<AccountDto> getUserAccount(Long userId, Long accountId) {
        log.info("사용자 계좌 조회 - 사용자 ID: {}, 계좌 ID: {}", userId, accountId);

        return accountRepository.findById(accountId)
                .filter(account -> account.getUserId().equals(userId))
                .map(AccountDto::fromEntity);
    }

    @Transactional
    public AccountDto updateAccountStatus(Long accountId, BankingAccount.AccountStatus newStatus) {
        log.info("계좌 상태 변경 - 계좌 ID: {}, 새 상태: {}", accountId, newStatus);

        BankingAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("계좌를 찾을 수 없습니다: " + accountId));

        account.setAccountStatus(newStatus);
        BankingAccount savedAccount = accountRepository.save(account);

        log.info("계좌 상태 변경 완료 - 계좌 ID: {}, 상태: {}", savedAccount.getAccountId(), savedAccount.getAccountStatus());
        return AccountDto.fromEntity(savedAccount);
    }

    @Transactional
    public AccountDto updateAccount(Long accountId, String accountName, String description) {
        log.info("계좌 정보 수정 - 계좌 ID: {}, 계좌명: {}", accountId, accountName);

        BankingAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("계좌를 찾을 수 없습니다: " + accountId));

        if (accountName != null) {
            account.setAccountName(accountName);
        }
        if (description != null) {
            account.setDescription(description);
        }

        BankingAccount savedAccount = accountRepository.save(account);

        log.info("계좌 정보 수정 완료 - 계좌 ID: {}", savedAccount.getAccountId());
        return AccountDto.fromEntity(savedAccount);
    }

    public BigDecimal getAccountBalance(Long accountId) {
        log.info("계좌 잔액 조회 - 계좌 ID: {}", accountId);

        BankingAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("계좌를 찾을 수 없습니다: " + accountId));

        return account.getBalance();
    }

    @Transactional
    public void updateAccountBalance(Long accountId, BigDecimal amount) {
        log.debug("계좌 잔액 업데이트 - 계좌 ID: {}, 금액: {}", accountId, amount);

        BankingAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("계좌를 찾을 수 없습니다: " + accountId));

        account.updateBalance(amount);
        accountRepository.save(account);

        log.debug("계좌 잔액 업데이트 완료 - 계좌 ID: {}, 새 잔액: {}", accountId, account.getBalance());
    }

    public boolean existsAccount(Long accountId) {
        return accountRepository.existsById(accountId);
    }

    public boolean existsUserAccount(Long userId, Long accountId) {
        return accountRepository.findById(accountId)
                .map(account -> account.getUserId().equals(userId))
                .orElse(false);
    }

    public boolean hasSufficientBalance(Long accountId, BigDecimal amount) {
        BankingAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("계좌를 찾을 수 없습니다: " + accountId));

        return account.hasSufficientBalance(amount);
    }

    public List<Object[]> getAccountStats(Long userId) {
        log.info("계좌 통계 조회 - 사용자 ID: {}", userId);

        return accountRepository.getBalanceSumByAccountType(userId);
    }
}