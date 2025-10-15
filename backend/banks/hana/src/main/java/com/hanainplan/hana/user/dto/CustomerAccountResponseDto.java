package com.hanainplan.hana.user.dto;

import com.hanainplan.hana.account.dto.AccountResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CustomerAccountResponseDto {
    private boolean isCustomer;
    private String customerName;
    private String customerCi;
    private List<AccountResponseDto> accounts;
    private int accountCount;

    public CustomerAccountResponseDto() {}

    public CustomerAccountResponseDto(boolean isCustomer, String customerName, String customerCi, List<AccountResponseDto> accounts) {
        this.isCustomer = isCustomer;
        this.customerName = customerName;
        this.customerCi = customerCi;
        this.accounts = accounts;
        this.accountCount = accounts != null ? accounts.size() : 0;
    }
}