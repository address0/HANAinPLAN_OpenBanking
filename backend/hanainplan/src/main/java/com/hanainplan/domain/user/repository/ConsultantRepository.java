package com.hanainplan.domain.user.repository;

import com.hanainplan.domain.user.entity.Consultant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
