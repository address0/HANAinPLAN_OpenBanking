package com.hanainplan.domain.notification.entity;

public enum NotificationType {
    CONSULTATION("상담"),
    TRANSACTION("거래"),
    SYSTEM("시스템"),
    SCHEDULE("일정"),
    OTHER("기타");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}