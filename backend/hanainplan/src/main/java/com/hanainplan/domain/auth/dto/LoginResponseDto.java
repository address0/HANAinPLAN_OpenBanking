package com.hanainplan.domain.auth.dto;

import com.hanainplan.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답 DTO")
public class LoginResponseDto {

    @Schema(description = "로그인 성공 여부", example = "true")
    private boolean success;

    @Schema(description = "응답 메시지", example = "로그인이 성공적으로 완료되었습니다.")
    private String message;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "사용자 이름", example = "홍길동")
    private String userName;

    @Schema(description = "전화번호", example = "01012345678")
    private String phoneNumber;

    @Schema(description = "사용자 타입", example = "GENERAL_CUSTOMER", allowableValues = {"GENERAL_CUSTOMER", "CONSULTANT"})
    private String userType;

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "로그인 타입", example = "PHONE", allowableValues = {"PHONE", "KAKAO"})
    private String loginType;

    public LoginResponseDto() {}

    public LoginResponseDto(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public LoginResponseDto(boolean success, String message, User user) {
        this.success = success;
        this.message = message;
        this.userId = user.getUserId();
        this.userName = user.getUserName();
        this.phoneNumber = user.getPhoneNumber();
        this.userType = user.getUserType().toString();
        this.email = user.getEmail();
        this.loginType = user.getLoginType().toString();
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLoginType() {
        return loginType;
    }

    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }
}