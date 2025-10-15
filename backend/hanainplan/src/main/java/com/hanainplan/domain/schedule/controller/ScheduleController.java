package com.hanainplan.domain.schedule.controller;

import com.hanainplan.domain.schedule.dto.ScheduleCreateRequest;
import com.hanainplan.domain.schedule.dto.ScheduleDto;
import com.hanainplan.domain.schedule.dto.ScheduleUpdateRequest;
import com.hanainplan.domain.schedule.entity.Schedule;
import com.hanainplan.domain.schedule.entity.ScheduleType;
import com.hanainplan.domain.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/consultant/schedules")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Schedule", description = "상담사 일정 관리 API")
@CrossOrigin(origins = "*")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping
    @Operation(summary = "일정 목록 조회", description = "상담사의 모든 일정을 조회합니다.")
    public ResponseEntity<List<ScheduleDto>> getSchedules(
            @Parameter(description = "상담사 ID", required = true)
            @RequestParam Long consultantId,

            @Parameter(description = "시작 날짜 (옵션)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,

            @Parameter(description = "종료 날짜 (옵션)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,

            @Parameter(description = "일정 유형 (옵션)")
            @RequestParam(required = false) ScheduleType scheduleType
    ) {
        log.info("GET /api/consultant/schedules - consultantId: {}, startDate: {}, endDate: {}, type: {}", 
                consultantId, startDate, endDate, scheduleType);

        List<ScheduleDto> schedules;

        if (startDate != null && endDate != null) {
            schedules = scheduleService.getSchedulesByDateRange(consultantId, startDate, endDate);
        } else if (scheduleType != null) {
            schedules = scheduleService.getSchedulesByType(consultantId, scheduleType);
        } else {
            schedules = scheduleService.getConsultantSchedules(consultantId);
        }

        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/{scheduleId}")
    @Operation(summary = "일정 상세 조회", description = "특정 일정의 상세 정보를 조회합니다.")
    public ResponseEntity<ScheduleDto> getSchedule(
            @Parameter(description = "일정 ID", required = true)
            @PathVariable Long scheduleId
    ) {
        log.info("GET /api/consultant/schedules/{} - scheduleId: {}", scheduleId, scheduleId);

        ScheduleDto schedule = scheduleService.getSchedule(scheduleId);
        return ResponseEntity.ok(schedule);
    }

    @PostMapping
    @Operation(summary = "일정 생성", description = "새로운 일정을 생성합니다.")
    public ResponseEntity<ScheduleDto> createSchedule(
            @Parameter(description = "상담사 ID", required = true)
            @RequestParam Long consultantId,

            @Parameter(description = "일정 생성 요청", required = true)
            @Valid @RequestBody ScheduleCreateRequest request
    ) {
        log.info("POST /api/consultant/schedules - consultantId: {}, title: {}", 
                consultantId, request.getTitle());

        ScheduleDto createdSchedule = scheduleService.createSchedule(consultantId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSchedule);
    }

    @PutMapping("/{scheduleId}")
    @Operation(summary = "일정 수정", description = "기존 일정을 수정합니다.")
    public ResponseEntity<ScheduleDto> updateSchedule(
            @Parameter(description = "일정 ID", required = true)
            @PathVariable Long scheduleId,

            @Parameter(description = "상담사 ID", required = true)
            @RequestParam Long consultantId,

            @Parameter(description = "일정 수정 요청", required = true)
            @Valid @RequestBody ScheduleUpdateRequest request
    ) {
        log.info("PUT /api/consultant/schedules/{} - scheduleId: {}, consultantId: {}", 
                scheduleId, scheduleId, consultantId);

        ScheduleDto updatedSchedule = scheduleService.updateSchedule(scheduleId, consultantId, request);
        return ResponseEntity.ok(updatedSchedule);
    }

    @DeleteMapping("/{scheduleId}")
    @Operation(summary = "일정 삭제", description = "일정을 삭제합니다.")
    public ResponseEntity<Void> deleteSchedule(
            @Parameter(description = "일정 ID", required = true)
            @PathVariable Long scheduleId,

            @Parameter(description = "상담사 ID", required = true)
            @RequestParam Long consultantId
    ) {
        log.info("DELETE /api/consultant/schedules/{} - scheduleId: {}, consultantId: {}", 
                scheduleId, scheduleId, consultantId);

        scheduleService.deleteSchedule(scheduleId, consultantId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{scheduleId}/status")
    @Operation(summary = "일정 상태 변경", description = "일정의 상태를 변경합니다.")
    public ResponseEntity<ScheduleDto> updateScheduleStatus(
            @Parameter(description = "일정 ID", required = true)
            @PathVariable Long scheduleId,

            @Parameter(description = "상담사 ID", required = true)
            @RequestParam Long consultantId,

            @Parameter(description = "변경할 상태", required = true)
            @RequestParam Schedule.ScheduleStatus status
    ) {
        log.info("PATCH /api/consultant/schedules/{}/status - scheduleId: {}, status: {}", 
                scheduleId, scheduleId, status);

        ScheduleDto updatedSchedule = scheduleService.updateScheduleStatus(scheduleId, consultantId, status);
        return ResponseEntity.ok(updatedSchedule);
    }

    @GetMapping("/today")
    @Operation(summary = "오늘 일정 조회", description = "상담사의 오늘 일정을 조회합니다.")
    public ResponseEntity<List<ScheduleDto>> getTodaySchedules(
            @Parameter(description = "상담사 ID", required = true)
            @RequestParam Long consultantId
    ) {
        log.info("GET /api/consultant/schedules/today - consultantId: {}", consultantId);

        List<ScheduleDto> schedules = scheduleService.getTodaySchedules(consultantId);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/upcoming")
    @Operation(summary = "다가오는 일정 조회", description = "상담사의 예정된 일정을 조회합니다.")
    public ResponseEntity<List<ScheduleDto>> getUpcomingSchedules(
            @Parameter(description = "상담사 ID", required = true)
            @RequestParam Long consultantId
    ) {
        log.info("GET /api/consultant/schedules/upcoming - consultantId: {}", consultantId);

        List<ScheduleDto> schedules = scheduleService.getUpcomingSchedules(consultantId);
        return ResponseEntity.ok(schedules);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("잘못된 요청: {}", e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        log.error("서버 오류: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("서버 오류가 발생했습니다: " + e.getMessage());
    }
}