package com.hanainplan.user.dto;

public class CiVerificationResponseDto {

    private boolean success;
    private String message;
    private boolean verified;
    private String timestamp;

    // 기본 생성자
    public CiVerificationResponseDto() {}

    // 생성자
    public CiVerificationResponseDto(boolean success, String message, boolean verified) {
        this.success = success;
        this.message = message;
        this.verified = verified;
        this.timestamp = java.time.LocalDateTime.now().toString();
    }

    // 정적 팩토리 메서드
    public static CiVerificationResponseDto success(boolean verified) {
        String message = verified ? "CI 검증이 성공했습니다." : "CI 검증에 실패했습니다.";
        return new CiVerificationResponseDto(true, message, verified);
    }

    public static CiVerificationResponseDto success(boolean verified, String message) {
        return new CiVerificationResponseDto(true, message, verified);
    }

    public static CiVerificationResponseDto error(String message) {
        return new CiVerificationResponseDto(false, message, false);
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
