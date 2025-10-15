package com.hanainplan.domain.consult.repository;

import com.hanainplan.domain.consult.entity.Consult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConsultRepository extends JpaRepository<Consult, String> {

    List<Consult> findByCustomerIdOrderByReservationDatetimeDesc(String customerId);

    List<Consult> findByConsultantIdOrderByReservationDatetimeDesc(String consultantId);

    List<Consult> findByConsultantIdAndConsultStatusOrderByReservationDatetimeDesc(
            String consultantId, String consultStatus);

    @Query("SELECT c FROM Consult c WHERE c.consultantId = :consultantId " +
           "AND c.reservationDatetime BETWEEN :startDate AND :endDate " +
           "ORDER BY c.reservationDatetime ASC")
    List<Consult> findByConsultantIdAndDateRange(
            @Param("consultantId") String consultantId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT c FROM Consult c WHERE c.consultantId = :consultantId " +
           "AND DATE(c.reservationDatetime) = DATE(:today) " +
           "ORDER BY c.reservationDatetime ASC")
    List<Consult> findTodayConsultations(
            @Param("consultantId") String consultantId,
            @Param("today") LocalDateTime today
    );

    List<Consult> findByConsultantIdAndConsultStatusInOrderByReservationDatetimeAsc(
            String consultantId, List<String> statuses);

    @Query("SELECT c FROM Consult c WHERE c.consultStatus = '예약확정' " +
           "AND c.notificationSent10min = false " +
           "AND c.reservationDatetime BETWEEN :startTime AND :endTime")
    List<Consult> findConsultationsFor10MinNotification(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT c FROM Consult c WHERE c.consultStatus = '예약확정' " +
           "AND c.notificationSentOntime = false " +
           "AND c.reservationDatetime BETWEEN :startTime AND :endTime")
    List<Consult> findConsultationsForOntimeNotification(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}