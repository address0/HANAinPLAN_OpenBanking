package com.hanainplan.domain.user.repository;

import com.hanainplan.domain.user.entity.Consultant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
