package com.hanainplan.user.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MyDataConsentResponseDto {
    private String message;
    private List<CustomerAccountInfoDto> bankAccountInfo;
    private int totalBanks;
    private int totalAccounts;
}