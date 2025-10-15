package com.hanainplan.domain.banking.dto;

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
public class TransactionResponseDto {

    private boolean success;
    private String message;
    private String transactionNumber;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private BigDecimal fee;
    private String transactionStatus;
    private LocalDateTime transactionDate;
    private String failureReason;

    public static TransactionResponseDto success(String message, String transactionNumber, 
                                               BigDecimal amount, BigDecimal balanceAfter, 
                                               BigDecimal fee, String transactionStatus) {
        return TransactionResponseDto.builder()
                .success(true)
                .message(message)
                .transactionNumber(transactionNumber)
                .amount(amount)
                .balanceAfter(balanceAfter)
                .fee(fee)
                .transactionStatus(transactionStatus)
                .transactionDate(LocalDateTime.now())
                .build();
    }

    public static TransactionResponseDto failure(String message, String failureReason) {
        return TransactionResponseDto.builder()
                .success(false)
                .message(message)
                .failureReason(failureReason)
                .transactionDate(LocalDateTime.now())
                .build();
    }
}