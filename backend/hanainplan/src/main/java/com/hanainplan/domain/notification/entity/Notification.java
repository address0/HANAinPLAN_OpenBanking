package com.hanainplan.domain.notification.entity;

import com.hanainplan.domain.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 알림 엔터티
 * - 사용자에게 전달할 알림 정보 저장
 */
@Entity
@Table(name = "notifications",
       indexes = {
           @Index(name = "idx_user_id", columnList = "user_id"),
           @Index(name = "idx_user_read", columnList = "user_id, is_read"),
           @Index(name = "idx_created_at", columnList = "created_at")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    @Column(name = "is_read")
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    /**
     * 알림을 읽음 처리
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    /**
     * 알림을 읽지 않음으로 변경
     */
    public void markAsUnread() {
        this.isRead = false;
        this.readAt = null;
    }

    /**
     * 알림 정보 업데이트
     */
    public void updateNotification(String title, String content, NotificationType type) {
        this.title = title;
        this.content = content;
        this.type = type;
    }
}
