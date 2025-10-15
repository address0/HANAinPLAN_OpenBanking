package com.hanainplan.domain.schedule.repository;

import com.hanainplan.domain.schedule.entity.Schedule;
import com.hanainplan.domain.schedule.entity.ScheduleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByConsultantIdOrderByStartTimeDesc(Long consultantId);

    List<Schedule> findByConsultantIdAndScheduleTypeOrderByStartTimeDesc(Long consultantId, ScheduleType scheduleType);

    @Query("SELECT s FROM Schedule s WHERE s.consultantId = :consultantId " +
           "AND s.startTime >= :startDate AND s.endTime <= :endDate " +
           "ORDER BY s.startTime ASC")
    List<Schedule> findByConsultantIdAndDateRange(
            @Param("consultantId") Long consultantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    List<Schedule> findByConsultantIdAndStatusOrderByStartTimeDesc(
            Long consultantId, 
            Schedule.ScheduleStatus status
    );

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

    @Query("SELECT s FROM Schedule s WHERE s.consultantId = :consultantId " +
           "AND DATE(s.startTime) = DATE(:today) " +
           "ORDER BY s.startTime ASC")
    List<Schedule> findTodaySchedules(
            @Param("consultantId") Long consultantId,
            @Param("today") LocalDateTime today
    );

    List<Schedule> findByClientIdOrderByStartTimeDesc(Long clientId);

    @Query("SELECT s FROM Schedule s WHERE s.consultantId = :consultantId " +
           "AND s.status = 'SCHEDULED' " +
           "AND s.startTime > :now " +
           "ORDER BY s.startTime ASC")
    List<Schedule> findUpcomingSchedules(
            @Param("consultantId") Long consultantId,
            @Param("now") LocalDateTime now
    );

    @Query("SELECT s FROM Schedule s WHERE s.consultantId = :consultantId " +
           "AND s.endTime < :now " +
           "ORDER BY s.startTime DESC")
    List<Schedule> findPastSchedules(
            @Param("consultantId") Long consultantId,
            @Param("now") LocalDateTime now
    );

    List<Schedule> findByConsultantIdAndClientIdAndScheduleType(
            Long consultantId, 
            Long clientId, 
            ScheduleType scheduleType
    );
}