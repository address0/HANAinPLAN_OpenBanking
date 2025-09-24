package com.hanainplan.hana.account.dto;

import com.hanainplan.hana.account.entity.Account;
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
    private String customerCi; // 고객 CI
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 기본 생성자
    public AccountResponseDto() {}

    // 생성자
    public AccountResponseDto(Account account) {
        this.accountNumber = account.getAccountNumber();
        this.accountType = account.getAccountType();
        this.balance = account.getBalance();
        this.openingDate = account.getOpeningDate();
        this.customerCi = account.getCustomerCi();
        this.createdAt = account.getCreatedAt();
        this.updatedAt = account.getUpdatedAt();
    }

    // 정적 팩토리 메서드
    public static AccountResponseDto from(Account account) {
        return new AccountResponseDto(account);
    }
}
