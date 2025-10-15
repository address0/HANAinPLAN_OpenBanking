package com.hanainplan.domain.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CiVerificationResponseDto {

    private boolean success;
    private String message;
    private String ci;

    public static CiVerificationResponseDto success(String ci) {
        return CiVerificationResponseDto.builder()
                .success(true)
                .message("CI 생성이 성공적으로 완료되었습니다.")
                .ci(ci)
                .build();
    }

    public static CiVerificationResponseDto error(String message) {
        return CiVerificationResponseDto.builder()
                .success(false)
                .message(message)
                .ci(null)
                .build();
    }
}