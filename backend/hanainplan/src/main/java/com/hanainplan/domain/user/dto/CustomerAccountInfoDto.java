package com.hanainplan.domain.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAccountInfoDto {
    
    private String bankName;
    private String bankCode;
    private String customerName;
    private String customerCi;
    private List<AccountInfoDto> accounts;
    private boolean isCustomer;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AccountInfoDto {
        private String accountNumber;
        private int accountType;
        private long balance;
        private String openingDate;
        private String createdAt;
        private String updatedAt;
    }
}
