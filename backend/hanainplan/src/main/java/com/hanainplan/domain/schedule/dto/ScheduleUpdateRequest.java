package com.hanainplan.domain.schedule.dto;

import com.hanainplan.domain.schedule.entity.Schedule;
import com.hanainplan.domain.schedule.entity.ScheduleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 일정 수정 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleUpdateRequest {

    private String title;
    private String description;
    private ScheduleType scheduleType;
    private String clientName;
    private Long clientId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean isAllDay;
    private Schedule.ScheduleStatus status;
    private String location;
    private String memo;

    /**
     * 엔터티 업데이트
     */
    public void updateEntity(Schedule schedule) {
        if (title != null) {
            schedule.setTitle(title);
        }
        if (description != null) {
            schedule.setDescription(description);
        }
        if (scheduleType != null) {
            schedule.setScheduleType(scheduleType);
        }
        if (clientName != null) {
            schedule.setClientName(clientName);
        }
        if (clientId != null) {
            schedule.setClientId(clientId);
        }
        if (startTime != null) {
            schedule.setStartTime(startTime);
        }
        if (endTime != null) {
            schedule.setEndTime(endTime);
        }
        if (isAllDay != null) {
            schedule.setIsAllDay(isAllDay);
        }
        if (status != null) {
            schedule.setStatus(status);
        }
        if (location != null) {
            schedule.setLocation(location);
        }
        if (memo != null) {
            schedule.setMemo(memo);
        }
    }
}

