package com.hanainplan.user.dto;

public class CiConversionResponseDto {

    private boolean success;
    private String message;
    private String ci;
    private String timestamp;

    public CiConversionResponseDto() {}

    public CiConversionResponseDto(boolean success, String message, String ci) {
        this.success = success;
        this.message = message;
        this.ci = ci;
        this.timestamp = java.time.LocalDateTime.now().toString();
    }

    public static CiConversionResponseDto success(String ci) {
        return new CiConversionResponseDto(true, "CI 변환이 성공적으로 완료되었습니다.", ci);
    }

    public static CiConversionResponseDto error(String message) {
        return new CiConversionResponseDto(false, message, null);
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