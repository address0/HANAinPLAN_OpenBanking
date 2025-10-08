package com.hanainplan.domain.notification.entity;

import com.hanainplan.domain.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * FCM 토큰 엔티티
 * - 사용자의 디바이스별 FCM 토큰 저장
 * - 푸시 알림 전송에 사용
 */
@Entity
@Table(name = "fcm_tokens", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "device_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FCMToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "device_token", nullable = false, length = 500)
    private String deviceToken;

    @Column(name = "device_id", length = 100)
    private String deviceId; // 디바이스 고유 ID (선택사항)

    @Column(name = "device_type", length = 20)
    private String deviceType; // WEB, ANDROID, IOS

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    /**
     * 토큰 업데이트
     */
    public void updateToken(String newToken) {
        this.deviceToken = newToken;
        this.lastUsedAt = LocalDateTime.now();
        this.isActive = true;
    }

    /**
     * 토큰 비활성화
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 마지막 사용 시간 업데이트
     */
    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }
}