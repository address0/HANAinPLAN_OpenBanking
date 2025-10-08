package com.hanainplan.domain.user.repository;

import com.hanainplan.domain.user.entity.Consultant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 상담원 Repository
 */
@Repository
public interface ConsultantRepository extends JpaRepository<Consultant, Long> {

    /**
     * 사용자 ID로 상담원 조회
     */
    Optional<Consultant> findByConsultantId(Long consultantId);

    /**
     * 직원번호로 상담원 조회
     */
    Optional<Consultant> findByEmployeeId(String employeeId);

    /**
     * 사용자 ID로 상담원 존재 여부 확인
     */
    boolean existsByConsultantId(Long consultantId);

    /**
     * 직원번호 존재 여부 확인
     */
    boolean existsByEmployeeId(String employeeId);

    /**
     * 상담 가능한 상담원 목록 조회 (평점 높은 순)
     */
    @Query("SELECT c FROM Consultant c WHERE c.workStatus = 'ACTIVE' AND c.consultationStatus = 'AVAILABLE' ORDER BY c.consultationRating DESC, c.totalConsultations ASC")
    List<Consultant> findAvailableConsultants();

    /**
     * 특정 상태의 상담원 목록 조회
     */
    @Query("SELECT c FROM Consultant c WHERE c.consultationStatus = :status")
    List<Consultant> findByConsultationStatus(@Param("status") Consultant.ConsultationStatus status);

    /**
     * 상담 가능한 상담원 수 조회
     */
    @Query("SELECT COUNT(c) FROM Consultant c WHERE c.workStatus = 'ACTIVE' AND c.consultationStatus = 'AVAILABLE'")
    Long countAvailableConsultants();

    /**
     * 특정 시간대에 일정이 없는 상담원 목록 조회
     * - ACTIVE 상태의 상담원 중에서
     * - 해당 시간대에 겹치는 일정(CANCELLED가 아닌)이 없는 상담원
     * - 평점 높은 순, 상담 건수 적은 순으로 정렬
     */
    @Query("SELECT DISTINCT c FROM Consultant c " +
           "WHERE c.workStatus = 'ACTIVE' " +
           "AND c.consultantId NOT IN (" +
           "    SELECT s.consultantId FROM Schedule s " +
           "    WHERE s.status != 'CANCELLED' " +
           "    AND ((s.startTime <= :startTime AND s.endTime > :startTime) " +
           "    OR (s.startTime < :endTime AND s.endTime >= :endTime) " +
           "    OR (s.startTime >= :startTime AND s.endTime <= :endTime))" +
           ") " +
           "ORDER BY c.consultationRating DESC, c.totalConsultations ASC")
    List<Consultant> findAvailableConsultantsAtTime(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
