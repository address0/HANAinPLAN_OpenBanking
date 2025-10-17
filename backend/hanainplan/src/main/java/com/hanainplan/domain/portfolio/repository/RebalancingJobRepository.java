package com.hanainplan.domain.portfolio.repository;

import com.hanainplan.domain.portfolio.entity.RebalancingJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RebalancingJobRepository extends JpaRepository<RebalancingJob, Long> {

    List<RebalancingJob> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<RebalancingJob> findByCustomerIdAndJobTypeOrderByCreatedAtDesc(Long customerId, String jobType);

    List<RebalancingJob> findByStatusOrderByCreatedAtDesc(String status);

    @Query("SELECT rj FROM RebalancingJob rj WHERE rj.customerId = :customerId AND rj.status = :status ORDER BY rj.createdAt DESC")
    List<RebalancingJob> findByCustomerIdAndStatusOrderByCreatedAtDesc(@Param("customerId") Long customerId, @Param("status") String status);

    @Query("SELECT rj FROM RebalancingJob rj WHERE rj.triggerType = :triggerType AND rj.status = :status ORDER BY rj.createdAt DESC")
    List<RebalancingJob> findByTriggerTypeAndStatusOrderByCreatedAtDesc(@Param("triggerType") String triggerType, @Param("status") String status);

    @Query("SELECT rj FROM RebalancingJob rj WHERE rj.createdAt >= :fromDate AND rj.createdAt <= :toDate ORDER BY rj.createdAt DESC")
    List<RebalancingJob> findByCreatedAtBetweenOrderByCreatedAtDesc(@Param("fromDate") LocalDateTime fromDate, @Param("toDate") LocalDateTime toDate);

    Optional<RebalancingJob> findByJobIdAndCustomerId(Long jobId, Long customerId);

    @Query("SELECT COUNT(rj) FROM RebalancingJob rj WHERE rj.customerId = :customerId AND rj.status = :status")
    long countByCustomerIdAndStatus(@Param("customerId") Long customerId, @Param("status") String status);

    @Query("SELECT COUNT(rj) FROM RebalancingJob rj WHERE rj.status = :status AND rj.createdAt >= :fromDate")
    long countByStatusAndCreatedAtAfter(@Param("status") String status, @Param("fromDate") LocalDateTime fromDate);

    // 배치 통계용 메서드들
    @Query("SELECT COUNT(rj) FROM RebalancingJob rj WHERE rj.createdAt >= :fromDate")
    long countByCreatedAtAfter(@Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT COUNT(rj) FROM RebalancingJob rj WHERE rj.triggerType = :triggerType AND rj.createdAt >= :fromDate")
    long countByTriggerTypeAndCreatedAtAfter(@Param("triggerType") String triggerType, @Param("fromDate") LocalDateTime fromDate);

    List<RebalancingJob> findByJobTypeAndStatus(RebalancingJob.JobType jobType, RebalancingJob.JobStatus status);

    // Pageable을 포함한 메서드들
    org.springframework.data.domain.Page<RebalancingJob> findByJobTypeAndStatus(RebalancingJob.JobType jobType, RebalancingJob.JobStatus status, org.springframework.data.domain.Pageable pageable);
}
