package com.hanainplan.domain.user.repository;

import com.hanainplan.domain.user.entity.RiskProfileAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RiskProfileAnswerRepository extends JpaRepository<RiskProfileAnswer, Long> {

    List<RiskProfileAnswer> findByCustomerIdOrderByAnsweredAtDesc(Long customerId);

    List<RiskProfileAnswer> findByCustomerIdAndSessionIdOrderByQuestionNumber(Long customerId, String sessionId);

    @Query("SELECT rpa FROM RiskProfileAnswer rpa WHERE rpa.customerId = :customerId AND rpa.sessionId = :sessionId ORDER BY rpa.questionNumber")
    List<RiskProfileAnswer> findLatestAnswersByCustomerAndSession(@Param("customerId") Long customerId, @Param("sessionId") String sessionId);

    @Query("SELECT rpa FROM RiskProfileAnswer rpa WHERE rpa.customerId = :customerId AND rpa.answeredAt = (SELECT MAX(rpa2.answeredAt) FROM RiskProfileAnswer rpa2 WHERE rpa2.customerId = :customerId)")
    List<RiskProfileAnswer> findLatestAnswersByCustomer(@Param("customerId") Long customerId);

    @Query("SELECT AVG(rpa.answerScore) FROM RiskProfileAnswer rpa WHERE rpa.customerId = :customerId AND rpa.sessionId = :sessionId")
    Optional<Double> calculateAverageScoreByCustomerAndSession(@Param("customerId") Long customerId, @Param("sessionId") String sessionId);

    @Query("SELECT AVG(rpa.answerScore) FROM RiskProfileAnswer rpa WHERE rpa.customerId = :customerId AND rpa.answeredAt = (SELECT MAX(rpa2.answeredAt) FROM RiskProfileAnswer rpa2 WHERE rpa2.customerId = :customerId)")
    Optional<Double> calculateLatestAverageScoreByCustomer(@Param("customerId") Long customerId);

    @Query("SELECT COUNT(rpa) FROM RiskProfileAnswer rpa WHERE rpa.customerId = :customerId AND rpa.sessionId = :sessionId")
    Long countAnswersByCustomerAndSession(@Param("customerId") Long customerId, @Param("sessionId") String sessionId);

    @Query("SELECT DISTINCT rpa.sessionId FROM RiskProfileAnswer rpa WHERE rpa.customerId = :customerId ORDER BY rpa.answeredAt DESC")
    List<String> findSessionIdsByCustomer(@Param("customerId") Long customerId);

    @Query("SELECT rpa FROM RiskProfileAnswer rpa WHERE rpa.customerId = :customerId AND rpa.questionNumber = :questionNumber ORDER BY rpa.answeredAt DESC")
    List<RiskProfileAnswer> findByCustomerAndQuestion(@Param("customerId") Long customerId, @Param("questionNumber") Integer questionNumber);

    @Query("SELECT rpa FROM RiskProfileAnswer rpa WHERE rpa.answeredAt >= :fromDate AND rpa.answeredAt <= :toDate")
    List<RiskProfileAnswer> findByAnsweredAtBetween(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);

    boolean existsByCustomerIdAndSessionId(Long customerId, String sessionId);

    void deleteByCustomerIdAndSessionId(Long customerId, String sessionId);
}
