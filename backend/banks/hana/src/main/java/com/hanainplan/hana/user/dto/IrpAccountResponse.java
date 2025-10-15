package com.hanainplan.hana.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IrpAccountResponse {

    private String accountNumber;
    private String accountStatus;
    private BigDecimal initialDeposit;
    private BigDecimal monthlyDeposit;
    private String investmentStyle;
    private String productCode;
    private LocalDate openDate;
    private LocalDateTime createdAt;
    private String message;
    private boolean success;

    public static IrpAccountResponse success(String accountNumber, String message) {
        return IrpAccountResponse.builder()
                .accountNumber(accountNumber)
                .success(true)
                .message(message)
                .build();
    }

    public static IrpAccountResponse failure(String message) {
        return IrpAccountResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}