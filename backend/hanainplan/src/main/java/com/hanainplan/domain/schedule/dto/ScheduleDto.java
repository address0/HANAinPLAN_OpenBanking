package com.hanainplan.domain.schedule.dto;

import com.hanainplan.domain.schedule.entity.Schedule;
import com.hanainplan.domain.schedule.entity.ScheduleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 일정 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleDto {

    private Long scheduleId;
    private Long consultantId;
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 프론트엔드에서 사용하는 필드 (fullcalendar 호환)
    private String id;
    private String start;
    private String end;
    private String type;

    /**
     * Entity -> DTO 변환
     */
    public static ScheduleDto fromEntity(Schedule schedule) {
        return ScheduleDto.builder()
                .scheduleId(schedule.getScheduleId())
                .consultantId(schedule.getConsultantId())
                .title(schedule.getTitle())
                .description(schedule.getDescription())
                .scheduleType(schedule.getScheduleType())
                .clientName(schedule.getClientName())
                .clientId(schedule.getClientId())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .isAllDay(schedule.getIsAllDay())
                .status(schedule.getStatus())
                .location(schedule.getLocation())
                .memo(schedule.getMemo())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                // 프론트엔드 호환 필드
                .id(String.valueOf(schedule.getScheduleId()))
                .start(schedule.getStartTime().toString())
                .end(schedule.getEndTime().toString())
                .type(schedule.getScheduleType().name().toLowerCase())
                .build();
    }
}

