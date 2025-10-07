package com.hanainplan.domain.schedule.repository;

import com.hanainplan.domain.schedule.entity.Schedule;
import com.hanainplan.domain.schedule.entity.ScheduleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 일정 Repository
 */
@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    /**
     * 상담사 ID로 일정 목록 조회
     */
    List<Schedule> findByConsultantIdOrderByStartTimeDesc(Long consultantId);

    /**
     * 상담사 ID와 일정 유형으로 조회
     */
    List<Schedule> findByConsultantIdAndScheduleTypeOrderByStartTimeDesc(Long consultantId, ScheduleType scheduleType);

    /**
     * 상담사 ID와 기간으로 조회
     */
    @Query("SELECT s FROM Schedule s WHERE s.consultantId = :consultantId " +
           "AND s.startTime >= :startDate AND s.endTime <= :endDate " +
           "ORDER BY s.startTime ASC")
    List<Schedule> findByConsultantIdAndDateRange(
            @Param("consultantId") Long consultantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 상담사 ID와 상태로 조회
     */
    List<Schedule> findByConsultantIdAndStatusOrderByStartTimeDesc(
            Long consultantId, 
            Schedule.ScheduleStatus status
    );

    /**
     * 상담사의 특정 시간대에 겹치는 일정 조회
     */
    @Query("SELECT s FROM Schedule s WHERE s.consultantId = :consultantId " +
           "AND s.status != 'CANCELLED' " +
           "AND ((s.startTime <= :startTime AND s.endTime > :startTime) " +
           "OR (s.startTime < :endTime AND s.endTime >= :endTime) " +
           "OR (s.startTime >= :startTime AND s.endTime <= :endTime))")
    List<Schedule> findOverlappingSchedules(
            @Param("consultantId") Long consultantId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 상담사의 오늘 일정 조회
     */
    @Query("SELECT s FROM Schedule s WHERE s.consultantId = :consultantId " +
           "AND DATE(s.startTime) = DATE(:today) " +
           "ORDER BY s.startTime ASC")
    List<Schedule> findTodaySchedules(
            @Param("consultantId") Long consultantId,
            @Param("today") LocalDateTime today
    );

    /**
     * 고객 ID로 일정 조회 (고객의 상담 일정 확인)
     */
    List<Schedule> findByClientIdOrderByStartTimeDesc(Long clientId);

    /**
     * 상담사의 다가오는 일정 조회 (예정된 일정만)
     */
    @Query("SELECT s FROM Schedule s WHERE s.consultantId = :consultantId " +
           "AND s.status = 'SCHEDULED' " +
           "AND s.startTime > :now " +
           "ORDER BY s.startTime ASC")
    List<Schedule> findUpcomingSchedules(
            @Param("consultantId") Long consultantId,
            @Param("now") LocalDateTime now
    );

    /**
     * 상담사의 과거 일정 조회
     */
    @Query("SELECT s FROM Schedule s WHERE s.consultantId = :consultantId " +
           "AND s.endTime < :now " +
           "ORDER BY s.startTime DESC")
    List<Schedule> findPastSchedules(
            @Param("consultantId") Long consultantId,
            @Param("now") LocalDateTime now
    );
}

