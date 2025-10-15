package com.hanainplan.domain.banking.dto;

import com.hanainplan.domain.banking.entity.BankingAccount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {

    private Long accountId;
    private Long userId;
    private String accountNumber;
    private String accountName;
    private Integer accountType;
    private String accountTypeDescription;
    private BankingAccount.AccountStatus accountStatus;
    private String accountStatusDescription;
    private BigDecimal balance;
    private String currencyCode;
    private LocalDateTime openedDate;
    private LocalDateTime expiryDate;
    private BigDecimal interestRate;
    private BigDecimal minimumBalance;
    private BigDecimal creditLimit;
    private String description;
    private String purpose;
    private BigDecimal monthlyDepositAmount;
    private Integer depositPeriod;
    private String interestPaymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AccountDto fromEntity(BankingAccount account) {
        return AccountDto.builder()
                .accountId(account.getAccountId())
                .userId(account.getUserId())
                .accountNumber(account.getAccountNumber())
                .accountName(account.getAccountName())
                .accountType(account.getAccountType())
                .accountTypeDescription(BankingAccount.AccountType.getDescription(account.getAccountType()))
                .accountStatus(account.getAccountStatus())
                .accountStatusDescription(account.getAccountStatus().getDescription())
                .balance(account.getBalance())
                .currencyCode(account.getCurrencyCode())
                .openedDate(account.getOpenedDate())
                .expiryDate(account.getExpiryDate())
                .interestRate(account.getInterestRate())
                .minimumBalance(account.getMinimumBalance())
                .creditLimit(account.getCreditLimit())
                .description(account.getDescription())
                .purpose(account.getPurpose())
                .monthlyDepositAmount(account.getMonthlyDepositAmount())
                .depositPeriod(account.getDepositPeriod())
                .interestPaymentMethod(account.getInterestPaymentMethod())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    public BankingAccount toEntity() {
        return BankingAccount.builder()
                .accountId(this.accountId)
                .userId(this.userId)
                .accountNumber(this.accountNumber)
                .accountName(this.accountName)
                .accountType(this.accountType)
                .accountStatus(this.accountStatus)
                .balance(this.balance)
                .currencyCode(this.currencyCode)
                .openedDate(this.openedDate)
                .expiryDate(this.expiryDate)
                .interestRate(this.interestRate)
                .minimumBalance(this.minimumBalance)
                .creditLimit(this.creditLimit)
                .description(this.description)
                .purpose(this.purpose)
                .monthlyDepositAmount(this.monthlyDepositAmount)
                .depositPeriod(this.depositPeriod)
                .interestPaymentMethod(this.interestPaymentMethod)
                .build();
    }
}