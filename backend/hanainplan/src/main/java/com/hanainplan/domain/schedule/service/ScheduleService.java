package com.hanainplan.domain.schedule.service;

import com.hanainplan.domain.schedule.dto.ScheduleCreateRequest;
import com.hanainplan.domain.schedule.dto.ScheduleDto;
import com.hanainplan.domain.schedule.dto.ScheduleUpdateRequest;
import com.hanainplan.domain.schedule.entity.Schedule;
import com.hanainplan.domain.schedule.entity.ScheduleType;
import com.hanainplan.domain.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    public List<ScheduleDto> getConsultantSchedules(Long consultantId) {
        log.info("상담사 일정 조회 - consultantId: {}", consultantId);

        List<Schedule> schedules = scheduleRepository.findByConsultantIdOrderByStartTimeDesc(consultantId);

        return schedules.stream()
                .map(ScheduleDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ScheduleDto> getSchedulesByDateRange(Long consultantId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("상담사 기간별 일정 조회 - consultantId: {}, 기간: {} ~ {}", 
                consultantId, startDate, endDate);

        List<Schedule> schedules = scheduleRepository.findByConsultantIdAndDateRange(
                consultantId, startDate, endDate
        );

        return schedules.stream()
                .map(ScheduleDto::fromEntity)
                .collect(Collectors.toList());
    }

    public ScheduleDto getSchedule(Long scheduleId) {
        log.info("일정 상세 조회 - scheduleId: {}", scheduleId);

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다. ID: " + scheduleId));

        return ScheduleDto.fromEntity(schedule);
    }

    @Transactional
    public ScheduleDto createSchedule(Long consultantId, ScheduleCreateRequest request) {
        log.info("일정 생성 - consultantId: {}, 제목: {}", consultantId, request.getTitle());

        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new IllegalArgumentException("시작 시간이 종료 시간보다 늦을 수 없습니다.");
        }

        List<Schedule> overlappingSchedules = scheduleRepository.findOverlappingSchedules(
                consultantId, request.getStartTime(), request.getEndTime()
        );

        if (!overlappingSchedules.isEmpty()) {
            log.warn("일정 겹침 감지 - consultantId: {}, 겹치는 일정 수: {}", 
                    consultantId, overlappingSchedules.size());
        }

        Schedule schedule = request.toEntity(consultantId);
        Schedule savedSchedule = scheduleRepository.save(schedule);

        log.info("일정 생성 완료 - scheduleId: {}", savedSchedule.getScheduleId());
        return ScheduleDto.fromEntity(savedSchedule);
    }

    @Transactional
    public ScheduleDto updateSchedule(Long scheduleId, Long consultantId, ScheduleUpdateRequest request) {
        log.info("일정 수정 - scheduleId: {}, consultantId: {}", scheduleId, consultantId);

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다. ID: " + scheduleId));

        if (!schedule.getConsultantId().equals(consultantId)) {
            throw new IllegalArgumentException("일정을 수정할 권한이 없습니다.");
        }

        request.updateEntity(schedule);

        if (!schedule.isValidTimeRange()) {
            throw new IllegalArgumentException("시작 시간이 종료 시간보다 늦을 수 없습니다.");
        }

        Schedule updatedSchedule = scheduleRepository.save(schedule);

        log.info("일정 수정 완료 - scheduleId: {}", scheduleId);
        return ScheduleDto.fromEntity(updatedSchedule);
    }

    @Transactional
    public void deleteSchedule(Long scheduleId, Long consultantId) {
        log.info("일정 삭제 - scheduleId: {}, consultantId: {}", scheduleId, consultantId);

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다. ID: " + scheduleId));

        if (!schedule.getConsultantId().equals(consultantId)) {
            throw new IllegalArgumentException("일정을 삭제할 권한이 없습니다.");
        }

        scheduleRepository.delete(schedule);
        log.info("일정 삭제 완료 - scheduleId: {}", scheduleId);
    }

    @Transactional
    public ScheduleDto updateScheduleStatus(Long scheduleId, Long consultantId, Schedule.ScheduleStatus status) {
        log.info("일정 상태 변경 - scheduleId: {}, status: {}", scheduleId, status);

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다. ID: " + scheduleId));

        if (!schedule.getConsultantId().equals(consultantId)) {
            throw new IllegalArgumentException("일정을 수정할 권한이 없습니다.");
        }

        schedule.setStatus(status);
        Schedule updatedSchedule = scheduleRepository.save(schedule);

        log.info("일정 상태 변경 완료 - scheduleId: {}, status: {}", scheduleId, status);
        return ScheduleDto.fromEntity(updatedSchedule);
    }

    public List<ScheduleDto> getTodaySchedules(Long consultantId) {
        log.info("오늘 일정 조회 - consultantId: {}", consultantId);

        List<Schedule> schedules = scheduleRepository.findTodaySchedules(
                consultantId, LocalDateTime.now()
        );

        return schedules.stream()
                .map(ScheduleDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ScheduleDto> getUpcomingSchedules(Long consultantId) {
        log.info("다가오는 일정 조회 - consultantId: {}", consultantId);

        List<Schedule> schedules = scheduleRepository.findUpcomingSchedules(
                consultantId, LocalDateTime.now()
        );

        return schedules.stream()
                .map(ScheduleDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ScheduleDto> getSchedulesByType(Long consultantId, ScheduleType scheduleType) {
        log.info("일정 유형별 조회 - consultantId: {}, type: {}", consultantId, scheduleType);

        List<Schedule> schedules = scheduleRepository.findByConsultantIdAndScheduleTypeOrderByStartTimeDesc(
                consultantId, scheduleType
        );

        return schedules.stream()
                .map(ScheduleDto::fromEntity)
                .collect(Collectors.toList());
    }

    public boolean hasScheduleAt(Long consultantId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Schedule> overlappingSchedules = scheduleRepository.findOverlappingSchedules(
                consultantId, startTime, endTime
        );

        return !overlappingSchedules.isEmpty();
    }

    @Transactional
    public ScheduleDto createConsultationSchedule(Long consultantId, Long clientId, String clientName, 
                                                   LocalDateTime startTime, LocalDateTime endTime) {
        log.info("상담 일정 자동 생성 - consultantId: {}, clientId: {}, clientName: {}", 
                consultantId, clientId, clientName);

        Schedule schedule = Schedule.builder()
                .consultantId(consultantId)
                .title(clientName + "님 상담")
                .description("고객 상담")
                .scheduleType(ScheduleType.CONSULTATION)
                .clientName(clientName)
                .clientId(clientId)
                .startTime(startTime)
                .endTime(endTime)
                .isAllDay(false)
                .status(Schedule.ScheduleStatus.SCHEDULED)
                .build();

        Schedule savedSchedule = scheduleRepository.save(schedule);

        log.info("상담 일정 자동 생성 완료 - scheduleId: {}", savedSchedule.getScheduleId());
        return ScheduleDto.fromEntity(savedSchedule);
    }

    @Transactional
    public void deleteConsultationSchedule(Long consultantId, Long clientId, LocalDateTime startTime) {
        log.info("상담 일정 삭제 - consultantId: {}, clientId: {}, startTime: {}", 
                consultantId, clientId, startTime);

        List<Schedule> consultationSchedules = scheduleRepository.findByConsultantIdAndClientIdAndScheduleType(
                consultantId, clientId, ScheduleType.CONSULTATION
        );

        Schedule targetSchedule = null;
        for (Schedule schedule : consultationSchedules) {
            if (schedule.getStartTime().equals(startTime)) {
                targetSchedule = schedule;
                break;
            }
        }

        if (targetSchedule != null) {
            scheduleRepository.delete(targetSchedule);
            log.info("상담 일정 삭제 완료 - scheduleId: {}, title: {}", 
                    targetSchedule.getScheduleId(), targetSchedule.getTitle());
        } else {
            log.warn("삭제할 상담 일정을 찾을 수 없음 - consultantId: {}, clientId: {}, startTime: {}", 
                    consultantId, clientId, startTime);
        }
    }
}