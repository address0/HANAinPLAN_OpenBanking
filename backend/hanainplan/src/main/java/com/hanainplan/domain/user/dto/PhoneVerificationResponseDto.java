package com.hanainplan.domain.user.dto;

import java.time.LocalDateTime;

public class PhoneVerificationResponseDto {

    private boolean success;
    private String message;
    private String verificationCode;
    private LocalDateTime timestamp;

    public PhoneVerificationResponseDto() {}

    public PhoneVerificationResponseDto(boolean success, String message, String verificationCode) {
        this.success = success;
        this.message = message;
        this.verificationCode = verificationCode;
        this.timestamp = LocalDateTime.now();
    }

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