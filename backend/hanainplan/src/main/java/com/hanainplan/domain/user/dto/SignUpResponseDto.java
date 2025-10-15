package com.hanainplan.domain.user.dto;

import com.hanainplan.domain.user.entity.User.UserType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpResponseDto {

    private Long userId;
    private UserType userType;
    private String name;
    private String phoneNumber;
    private Boolean isPhoneVerified;
    private LocalDateTime createdDate;
    private String message;

    public static SignUpResponseDto success(Long userId, UserType userType, String name, String phoneNumber, LocalDateTime createdDate) {
        return SignUpResponseDto.builder()
                .userId(userId)
                .userType(userType)
                .name(name)
                .phoneNumber(phoneNumber)
                .isPhoneVerified(true)
                .createdDate(createdDate)
                .message("회원가입이 성공적으로 완료되었습니다.")
                .build();
    }

    public static SignUpResponseDto failure(String message) {
        return SignUpResponseDto.builder()
                .message(message)
                .build();
    }
}