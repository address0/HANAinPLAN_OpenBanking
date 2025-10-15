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
public class MyDataConsentResponseDto {

    private String message;
    private List<CustomerAccountInfoDto> bankAccountInfo;
    private int totalBanks;
    private int totalAccounts;

    public static MyDataConsentResponseDto success(String message, List<CustomerAccountInfoDto> bankAccountInfo, int totalBanks, int totalAccounts) {
        return MyDataConsentResponseDto.builder()
                .message(message)
                .bankAccountInfo(bankAccountInfo)
                .totalBanks(totalBanks)
                .totalAccounts(totalAccounts)
                .build();
    }
}