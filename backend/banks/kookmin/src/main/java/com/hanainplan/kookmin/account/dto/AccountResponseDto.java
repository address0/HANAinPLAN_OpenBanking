package com.hanainplan.kookmin.account.dto;

import com.hanainplan.kookmin.account.entity.Account;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class AccountResponseDto {

    private String accountNumber;
    private Integer accountType;
    private BigDecimal balance;
    private LocalDate openingDate;
    private String customerCi;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AccountResponseDto() {}

    public AccountResponseDto(Account account) {
        this.accountNumber = account.getAccountNumber();
        this.accountType = account.getAccountType();
        this.balance = account.getBalance();
        this.openingDate = account.getOpeningDate();
        this.customerCi = account.getCustomerCi();
        this.createdAt = account.getCreatedAt();
        this.updatedAt = account.getUpdatedAt();
    }

    public static AccountResponseDto from(Account account) {
        return new AccountResponseDto(account);
    }
}