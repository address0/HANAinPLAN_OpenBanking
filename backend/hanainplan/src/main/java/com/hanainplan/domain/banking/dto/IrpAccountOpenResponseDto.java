package com.hanainplan.domain.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IrpAccountOpenResponseDto {

    private boolean success;
    private String message;
    private String accountNumber;
    private String errorCode;

    public static IrpAccountOpenResponseDto success(String accountNumber, String message) {
        return IrpAccountOpenResponseDto.builder()
                .success(true)
                .message(message)
                .accountNumber(accountNumber)
                .build();
    }

    public static IrpAccountOpenResponseDto failure(String message) {
        return IrpAccountOpenResponseDto.builder()
                .success(false)
                .message(message)
                .build();
    }

    public static IrpAccountOpenResponseDto failure(String message, String errorCode) {
        return IrpAccountOpenResponseDto.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
}