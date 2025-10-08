package com.hanainplan.domain.consult.repository;

import com.hanainplan.domain.consult.entity.Consult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 상담 Repository
 */
@Repository
public interface ConsultRepository extends JpaRepository<Consult, String> {

    /**
     * 고객 ID로 상담 목록 조회
     */
    List<Consult> findByCustomerIdOrderByReservationDatetimeDesc(String customerId);

    /**
     * 상담사 ID로 상담 목록 조회
     */
    List<Consult> findByConsultantIdOrderByReservationDatetimeDesc(String consultantId);

    /**
     * 상담사 ID와 상태로 상담 목록 조회
     */
    List<Consult> findByConsultantIdAndConsultStatusOrderByReservationDatetimeDesc(
            String consultantId, String consultStatus);

    /**
     * 상담사 ID와 날짜 범위로 상담 목록 조회
     */
    @Query("SELECT c FROM Consult c WHERE c.consultantId = :consultantId " +
           "AND c.reservationDatetime BETWEEN :startDate AND :endDate " +
           "ORDER BY c.reservationDatetime ASC")
    List<Consult> findByConsultantIdAndDateRange(
            @Param("consultantId") String consultantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 상담사의 오늘 상담 조회
     */
    @Query("SELECT c FROM Consult c WHERE c.consultantId = :consultantId " +
           "AND DATE(c.reservationDatetime) = DATE(:today) " +
           "ORDER BY c.reservationDatetime ASC")
    List<Consult> findTodayConsultations(
            @Param("consultantId") String consultantId,
            @Param("today") LocalDateTime today
    );

    /**
     * 예약 신청 상태의 상담 조회 (상담사별)
     */
    List<Consult> findByConsultantIdAndConsultStatusInOrderByReservationDatetimeAsc(
            String consultantId, List<String> statuses);
}

