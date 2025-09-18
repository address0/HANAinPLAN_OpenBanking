package com.hanainplan.user.dto;

import java.time.LocalDateTime;

public class PhoneVerificationResponseDto {

    private boolean success;
    private String message;
    private String verificationCode; // 개발용으로만 사용
    private LocalDateTime timestamp;

    // 기본 생성자
    public PhoneVerificationResponseDto() {}

    // 생성자
    public PhoneVerificationResponseDto(boolean success, String message, String verificationCode) {
        this.success = success;
        this.message = message;
        this.verificationCode = verificationCode;
        this.timestamp = LocalDateTime.now();
    }

    // 정적 팩토리 메서드
    public static PhoneVerificationResponseDto success(String message, String verificationCode) {
        return new PhoneVerificationResponseDto(true, message, verificationCode);
    }

    public static PhoneVerificationResponseDto error(String message) {
        return new PhoneVerificationResponseDto(false, message, null);
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
