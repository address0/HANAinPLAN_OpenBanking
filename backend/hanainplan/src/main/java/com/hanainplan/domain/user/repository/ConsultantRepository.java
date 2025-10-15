package com.hanainplan.domain.user.repository;

import com.hanainplan.domain.user.entity.Consultant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultantRepository extends JpaRepository<Consultant, Long> {

    Optional<Consultant> findByConsultantId(Long consultantId);

    Optional<Consultant> findByEmployeeId(String employeeId);

    boolean existsByConsultantId(Long consultantId);

    boolean existsByEmployeeId(String employeeId);

    @Query("SELECT c FROM Consultant c WHERE c.workStatus = 'ACTIVE' AND c.consultationStatus = 'AVAILABLE' ORDER BY c.consultationRating DESC, c.totalConsultations ASC")
    List<Consultant> findAvailableConsultants();

    @Query("SELECT c FROM Consultant c WHERE c.consultationStatus = :status")
    List<Consultant> findByConsultationStatus(@Param("status") Consultant.ConsultationStatus status);

    @Query("SELECT COUNT(c) FROM Consultant c WHERE c.workStatus = 'ACTIVE' AND c.consultationStatus = 'AVAILABLE'")
    Long countAvailableConsultants();

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