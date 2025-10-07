package com.hanainplan.domain.schedule.entity;

/**
 * 일정 타입 enum
 * - 상담사의 일정 유형 구분
 */
public enum ScheduleType {
    CONSULTATION("고객 상담"),
    MEETING("회의"),
    OTHER("기타");

    private final String description;

    ScheduleType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

