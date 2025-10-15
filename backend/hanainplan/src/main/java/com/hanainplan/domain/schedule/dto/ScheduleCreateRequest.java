package com.hanainplan.domain.schedule.dto;

import com.hanainplan.domain.schedule.entity.Schedule;
import com.hanainplan.domain.schedule.entity.ScheduleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleCreateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    private String description;

    @NotNull(message = "일정 유형은 필수입니다.")
    private ScheduleType scheduleType;

    private String clientName;
    private Long clientId;

    @NotNull(message = "시작 시간은 필수입니다.")
    private LocalDateTime startTime;

    @NotNull(message = "종료 시간은 필수입니다.")
    private LocalDateTime endTime;

    @Builder.Default
    private Boolean isAllDay = false;

    private String location;
    private String memo;

    public Schedule toEntity(Long consultantId) {
        return Schedule.builder()
                .consultantId(consultantId)
                .title(title)
                .description(description)
                .scheduleType(scheduleType)
                .clientName(clientName)
                .clientId(clientId)
                .startTime(startTime)
                .endTime(endTime)
                .isAllDay(isAllDay != null ? isAllDay : false)
                .status(Schedule.ScheduleStatus.SCHEDULED)
                .location(location)
                .memo(memo)
                .build();
    }

    public static ScheduleCreateRequest fromFrontend(String title, String description, 
                                                     String type, String clientName, 
                                                     String start, String end) {
        return ScheduleCreateRequest.builder()
                .title(title)
                .description(description)
                .scheduleType(ScheduleType.valueOf(type.toUpperCase()))
                .clientName(clientName)
                .startTime(LocalDateTime.parse(start))
                .endTime(LocalDateTime.parse(end))
                .isAllDay(false)
                .build();
    }
}