package com.hanainplan.domain.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountVerificationResponseDto {

    private boolean exists;

    private String accountType;

    private String bankCode;

    private String accountStatus;

    private String accountNumber;

    private String message;

    public static AccountVerificationResponseDto success(String accountType, String bankCode, String accountStatus, String accountNumber) {
        return AccountVerificationResponseDto.builder()
                .exists(true)
                .accountType(accountType)
                .bankCode(bankCode)
                .accountStatus(accountStatus)
                .accountNumber(accountNumber)
                .message("계좌 확인 완료")
                .build();
    }

    public static AccountVerificationResponseDto notFound(String accountNumber) {
        return AccountVerificationResponseDto.builder()
                .exists(false)
                .accountNumber(accountNumber)
                .message("계좌를 찾을 수 없습니다")
                .build();
    }

    public static AccountVerificationResponseDto error(String message) {
        return AccountVerificationResponseDto.builder()
                .exists(false)
                .message(message)
                .build();
    }
}