package com.hanainplan.domain.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IRP 계좌 개설 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IrpAccountOpenResponseDto {

    private boolean success;
    private String message;
    private String accountNumber;
    private String errorCode;

    // 성공 응답용 생성자
    public static IrpAccountOpenResponseDto success(String accountNumber, String message) {
        return IrpAccountOpenResponseDto.builder()
                .success(true)
                .message(message)
                .accountNumber(accountNumber)
                .build();
    }

    // 실패 응답용 생성자
    public static IrpAccountOpenResponseDto failure(String message) {
        return IrpAccountOpenResponseDto.builder()
                .success(false)
                .message(message)
                .build();
    }

    // 실패 응답용 생성자 (오류 코드 포함)
    public static IrpAccountOpenResponseDto failure(String message, String errorCode) {
        return IrpAccountOpenResponseDto.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
}
