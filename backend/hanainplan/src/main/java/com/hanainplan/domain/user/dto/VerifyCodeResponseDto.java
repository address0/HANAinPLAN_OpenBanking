package com.hanainplan.domain.user.dto;

import java.time.LocalDateTime;

public class VerifyCodeResponseDto {

    private boolean success;
    private String message;
    private boolean verified;
    private LocalDateTime timestamp;

    // 기본 생성자
    public VerifyCodeResponseDto() {}

    // 생성자
    public VerifyCodeResponseDto(boolean success, String message, boolean verified) {
        this.success = success;
        this.message = message;
        this.verified = verified;
        this.timestamp = LocalDateTime.now();
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

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
