package com.hanainplan.domain.schedule.entity;

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