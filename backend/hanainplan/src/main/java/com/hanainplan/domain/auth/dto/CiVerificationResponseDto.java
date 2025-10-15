package com.hanainplan.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CiVerificationResponseDto {

    private boolean success;
    private String message;
    private String ci;
    private String errorCode;

    public static CiVerificationResponseDto success(String ci) {
        return CiVerificationResponseDto.builder()
                .success(true)
                .message("CI 검증이 성공적으로 완료되었습니다.")
                .ci(ci)
                .build();
    }

    public static CiVerificationResponseDto failure(String message) {
        return CiVerificationResponseDto.builder()
                .success(false)
                .message(message)
                .build();
    }

    public static CiVerificationResponseDto failure(String message, String errorCode) {
        return CiVerificationResponseDto.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
}