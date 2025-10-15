package com.hanainplan.domain.banking.service;

import com.hanainplan.domain.banking.dto.AccountDto;
import com.hanainplan.domain.banking.entity.BankingAccount;
import com.hanainplan.domain.banking.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyDataAccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public List<AccountDto> saveMyDataAccounts(Long userId, String customerCi, List<MyDataAccountInfo> accountInfos) {
        log.info("마이데이터 계좌 정보 저장 시작 - 사용자 ID: {}, CI: {}, 계좌 수: {}", 
                userId, customerCi, accountInfos.size());

        List<AccountDto> savedAccounts = accountInfos.stream()
                .map(accountInfo -> saveSingleAccount(userId, customerCi, accountInfo))
                .collect(Collectors.toList());

        log.info("마이데이터 계좌 정보 저장 완료 - 저장된 계좌 수: {}", savedAccounts.size());
        return savedAccounts;
    }

    private AccountDto saveSingleAccount(Long userId, String customerCi, MyDataAccountInfo accountInfo) {
        if (accountRepository.existsByAccountNumber(accountInfo.getAccountNumber())) {
            log.info("계좌번호 {}는 이미 존재합니다. 건너뜁니다.", accountInfo.getAccountNumber());
            return accountRepository.findByAccountNumber(accountInfo.getAccountNumber())
                    .map(AccountDto::fromEntity)
                    .orElse(null);
        }

        BankingAccount account = BankingAccount.builder()
                .userId(userId)
                .customerCi(customerCi)
                .accountNumber(accountInfo.getAccountNumber())
                .accountName(generateAccountName(accountInfo.getAccountType()))
                .accountType(accountInfo.getAccountType())
                .accountStatus(BankingAccount.AccountStatus.ACTIVE)
                .balance(accountInfo.getBalance())
                .currencyCode("KRW")
                .openedDate(accountInfo.getOpeningDate())
                .description("마이데이터에서 가져온 계좌")
                .build();

        BankingAccount savedAccount = accountRepository.save(account);
        log.info("계좌 저장 완료 - 계좌 ID: {}, 계좌번호: {}", savedAccount.getAccountId(), savedAccount.getAccountNumber());

        return AccountDto.fromEntity(savedAccount);
    }

    private String generateAccountName(Integer accountType) {
        switch (accountType) {
            case BankingAccount.AccountType.CHECKING:
                return "입출금통장";
            case BankingAccount.AccountType.SAVINGS:
                return "예적금통장";
            case BankingAccount.AccountType.SECURITIES:
                return "수익증권계좌";
            case BankingAccount.AccountType.INTEGRATED:
                return "통합계좌";
            default:
                return "기타계좌";
        }
    }

    public static class MyDataAccountInfo {
        private String accountNumber;
        private Integer accountType;
        private BigDecimal balance;
        private LocalDateTime openingDate;

        public MyDataAccountInfo() {}

        public MyDataAccountInfo(String accountNumber, Integer accountType, BigDecimal balance, LocalDateTime openingDate) {
            this.accountNumber = accountNumber;
            this.accountType = accountType;
            this.balance = balance;
            this.openingDate = openingDate;
        }

        public String getAccountNumber() { return accountNumber; }
        public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

        public Integer getAccountType() { return accountType; }
        public void setAccountType(Integer accountType) { this.accountType = accountType; }

        public BigDecimal getBalance() { return balance; }
        public void setBalance(BigDecimal balance) { this.balance = balance; }

        public LocalDateTime getOpeningDate() { return openingDate; }
        public void setOpeningDate(LocalDateTime openingDate) { this.openingDate = openingDate; }
    }
}