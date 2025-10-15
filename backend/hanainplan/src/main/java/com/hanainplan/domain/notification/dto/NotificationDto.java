package com.hanainplan.domain.notification.dto;

import com.hanainplan.domain.notification.entity.Notification;
import com.hanainplan.domain.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class NotificationDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long userId;
        private String title;
        private String content;
        private NotificationType type;
        private Boolean isRead;
        private LocalDateTime createdAt;
        private LocalDateTime readAt;

        public static Response from(Notification notification) {
            return Response.builder()
                    .id(notification.getId())
                    .userId(notification.getUserId())
                    .title(notification.getTitle())
                    .content(notification.getContent())
                    .type(notification.getType())
                    .isRead(notification.getIsRead())
                    .createdAt(notification.getCreatedAt())
                    .readAt(notification.getReadAt())
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private Long userId;
        private String title;
        private String content;
        private NotificationType type;

        public Notification toEntity() {
            return Notification.builder()
                    .userId(userId)
                    .title(title)
                    .content(content)
                    .type(type)
                    .build();
        }
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String title;
        private String content;
        private NotificationType type;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Summary {
        private Long totalCount;
        private Long unreadCount;

        public static Summary of(Long totalCount, Long unreadCount) {
            return Summary.builder()
                    .totalCount(totalCount)
                    .unreadCount(unreadCount)
                    .build();
        }
    }
}