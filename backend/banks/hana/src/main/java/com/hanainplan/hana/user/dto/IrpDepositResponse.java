package com.hanainplan.hana.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IrpDepositResponse {

    private boolean success;

    private String message;

    private String transactionId;

    private String accountNumber;

    private BigDecimal depositAmount;

    private BigDecimal newBalance;

    private BigDecimal totalContribution;

    public static IrpDepositResponse success(String message, String transactionId, String accountNumber, 
                                            BigDecimal depositAmount, BigDecimal newBalance, BigDecimal totalContribution) {
        return IrpDepositResponse.builder()
                .success(true)
                .message(message)
                .transactionId(transactionId)
                .accountNumber(accountNumber)
                .depositAmount(depositAmount)
                .newBalance(newBalance)
                .totalContribution(totalContribution)
                .build();
    }

    public static IrpDepositResponse failure(String message) {
        return IrpDepositResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}