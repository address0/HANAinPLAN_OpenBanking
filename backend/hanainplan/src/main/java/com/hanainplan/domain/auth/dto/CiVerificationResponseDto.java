package com.hanainplan.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CI 검증 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CiVerificationResponseDto {

    private boolean success;
    private String message;
    private String ci; // 실명인증 서버에서 반환받은 CI 값
    private String errorCode; // 오류 코드 (선택사항)

    // 성공 응답용 생성자
    public static CiVerificationResponseDto success(String ci) {
        return CiVerificationResponseDto.builder()
                .success(true)
                .message("CI 검증이 성공적으로 완료되었습니다.")
                .ci(ci)
                .build();
    }

    // 실패 응답용 생성자
    public static CiVerificationResponseDto failure(String message) {
        return CiVerificationResponseDto.builder()
                .success(false)
                .message(message)
                .build();
    }

    // 실패 응답용 생성자 (오류 코드 포함)
    public static CiVerificationResponseDto failure(String message, String errorCode) {
        return CiVerificationResponseDto.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
}
