package com.hanainplan.domain.user.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class CustomerAccountInfoDto {
    private String bankName;
    private String bankCode;
    private String customerName;
    private String customerCi;
    private List<AccountInfoDto> accounts;
    private boolean isCustomer;

    @Getter
    @Setter
    public static class AccountInfoDto {
        private String accountNumber;
        private Integer accountType;
        private Object balance;
        private LocalDateTime openingDate;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}