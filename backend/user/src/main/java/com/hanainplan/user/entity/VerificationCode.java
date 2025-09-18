package com.hanainplan.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 인증번호 엔터티
 * - 전화번호 인증을 위한 인증번호 관리
 */
@Entity
@Table(name = "verification_codes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_id")
    private Long verificationId;

    @Column(name = "phone_number", length = 13, nullable = false)
    private String phoneNumber; // 010-0000-0000 형태

    @Column(name = "verification_code", length = 6, nullable = false)
    private String code; // 6자리 인증번호

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false; // 인증 완료 여부

    @Column(name = "attempt_count")
    @Builder.Default
    private Integer attemptCount = 0; // 인증 시도 횟수

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; // 만료 시간

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt; // 인증 완료 시간

    /**
     * 인증번호 만료 여부 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 인증번호 유효성 확인 (만료되지 않고 인증되지 않은 상태)
     */
    public boolean isValid() {
        return !isExpired() && !Boolean.TRUE.equals(isVerified);
    }

    /**
     * 최대 시도 횟수 초과 여부 확인
     */
    public boolean isMaxAttemptsExceeded() {
        return attemptCount >= 5; // 최대 5회 시도
    }

    /**
     * 인증 시도 횟수 증가
     */
    public void incrementAttemptCount() {
        this.attemptCount = (this.attemptCount != null) ? this.attemptCount + 1 : 1;
    }

    /**
     * 인증 완료 처리
     */
    public void markAsVerified() {
        this.isVerified = true;
        this.verifiedAt = LocalDateTime.now();
    }

    /**
     * 만료 시간 설정 (3분)
     */
    public static LocalDateTime calculateExpiryTime() {
        return LocalDateTime.now().plusMinutes(3);
    }
}
