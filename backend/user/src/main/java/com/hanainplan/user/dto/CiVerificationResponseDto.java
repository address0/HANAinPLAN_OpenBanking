package com.hanainplan.user.dto;

public class CiVerificationResponseDto {

    private boolean success;
    private String message;
    private boolean verified;
    private String ci;
    private String timestamp;

    public CiVerificationResponseDto() {}

    public CiVerificationResponseDto(boolean success, String message, boolean verified, String ci) {
        this.success = success;
        this.message = message;
        this.verified = verified;
        this.ci = ci;
        this.timestamp = java.time.LocalDateTime.now().toString();
    }

    public static CiVerificationResponseDto success(boolean verified, String ci) {
        String message = verified ? "CI 검증이 성공했습니다." : "CI 검증에 실패했습니다.";
        return new CiVerificationResponseDto(true, message, verified, ci);
    }

    public static CiVerificationResponseDto success(boolean verified, String message, String ci) {
        return new CiVerificationResponseDto(true, message, verified, ci);
    }

    public static CiVerificationResponseDto error(String message) {
        return new CiVerificationResponseDto(false, message, false, null);
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

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getCi() {
        return ci;
    }

    public void setCi(String ci) {
        this.ci = ci;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}