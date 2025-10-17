package com.hanainplan.domain.portfolio.controller;

import com.hanainplan.domain.portfolio.dto.RebalancingSimulationRequest;
import com.hanainplan.domain.portfolio.dto.RebalancingSimulationResponse;
import com.hanainplan.domain.portfolio.entity.RebalancingJob;
import com.hanainplan.domain.portfolio.repository.RebalancingJobRepository;
import com.hanainplan.domain.portfolio.service.RebalancingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/irp/rebalancing/batch")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Rebalancing Batch", description = "리밸런싱 배치 관리 API")
public class RebalancingBatchController {

    private final RebalancingJobRepository rebalancingJobRepository;
    private final RebalancingService rebalancingService;

    /**
     * 배치 실행 통계 조회
     */
    @GetMapping("/stats")
    @Operation(summary = "배치 실행 통계", description = "리밸런싱 배치 실행 통계를 조회합니다")
    public ResponseEntity<Map<String, Object>> getBatchStats(
            @Parameter(description = "조회 기간 (일)") @RequestParam(defaultValue = "30") int days) {
        
        log.info("배치 실행 통계 조회 - 기간: {}일", days);

        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        
        // 전체 작업 수
        long totalJobs = rebalancingJobRepository.count();
        
        // 기간 내 작업 수
        long recentJobs = rebalancingJobRepository.countByCreatedAtAfter(startDate);
        
        // 성공한 작업 수
        long successJobs = rebalancingJobRepository.countByStatusAndCreatedAtAfter("COMPLETED", startDate);
        
        // 실패한 작업 수
        long failedJobs = rebalancingJobRepository.countByStatusAndCreatedAtAfter("FAILED", startDate);
        
        // 시간 기반 작업 수
        long timeBasedJobs = rebalancingJobRepository.countByTriggerTypeAndCreatedAtAfter("TIME_BASED", startDate);
        
        // 밴드 기반 작업 수
        long bandBasedJobs = rebalancingJobRepository.countByTriggerTypeAndCreatedAtAfter("BAND_BASED", startDate);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalJobs", totalJobs);
        stats.put("recentJobs", recentJobs);
        stats.put("successJobs", successJobs);
        stats.put("failedJobs", failedJobs);
        stats.put("timeBasedJobs", timeBasedJobs);
        stats.put("bandBasedJobs", bandBasedJobs);
        stats.put("successRate", recentJobs > 0 ? (double) successJobs / recentJobs * 100 : 0.0);
        stats.put("period", days);

        return ResponseEntity.ok(stats);
    }

    /**
     * 배치 작업 목록 조회
     */
    @GetMapping("/jobs")
    @Operation(summary = "배치 작업 목록", description = "리밸런싱 배치 작업 목록을 조회합니다")
    public ResponseEntity<Page<RebalancingJob>> getBatchJobs(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "트리거 타입") @RequestParam(required = false) String triggerType,
            @Parameter(description = "상태") @RequestParam(required = false) String status) {
        
        log.info("배치 작업 목록 조회 - 페이지: {}, 크기: {}, 트리거: {}, 상태: {}", 
                page, size, triggerType, status);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<RebalancingJob> jobs;
        
        if (triggerType != null && status != null) {
            jobs = rebalancingJobRepository.findByJobTypeAndStatus(
                    RebalancingJob.JobType.SIMULATION, 
                    RebalancingJob.JobStatus.valueOf(status), 
                    pageable);
        } else if (triggerType != null) {
            // 간단한 구현으로 전체 조회 후 필터링
            List<RebalancingJob> allJobs = rebalancingJobRepository.findAll();
            List<RebalancingJob> filteredJobs = allJobs.stream()
                    .filter(job -> RebalancingJob.TriggerType.valueOf(triggerType).equals(job.getTriggerType()))
                    .collect(java.util.stream.Collectors.toList());
            
            // Page 객체로 변환
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), filteredJobs.size());
            List<RebalancingJob> pageContent = filteredJobs.subList(start, end);
            jobs = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, filteredJobs.size());
        } else {
            jobs = rebalancingJobRepository.findAll(pageable);
        }

        return ResponseEntity.ok(jobs);
    }

    /**
     * 수동 배치 실행 (테스트용)
     */
    @PostMapping("/execute/time-based")
    @Operation(summary = "시간 기반 배치 수동 실행", description = "시간 기반 리밸런싱 배치를 수동으로 실행합니다")
    public ResponseEntity<Map<String, Object>> executeTimeBasedBatch() {
        log.info("시간 기반 배치 수동 실행 요청");

        try {
            // 테스트용으로 고객 ID 5번만 실행
            Long testCustomerId = 5L;
            
            RebalancingSimulationRequest request = RebalancingSimulationRequest.builder()
                    .customerId(testCustomerId)
                    .triggerType("TIME_BASED")
                    .build();

            RebalancingSimulationResponse response = rebalancingService.simulateRebalancing(request);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "시간 기반 배치 실행 완료");
            result.put("jobId", response.getJobId());
            result.put("customerId", testCustomerId);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("시간 기반 배치 수동 실행 실패", e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "배치 실행 실패: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 수동 배치 실행 (밴드 기반)
     */
    @PostMapping("/execute/band-based")
    @Operation(summary = "밴드 기반 배치 수동 실행", description = "밴드 기반 리밸런싱 배치를 수동으로 실행합니다")
    public ResponseEntity<Map<String, Object>> executeBandBasedBatch() {
        log.info("밴드 기반 배치 수동 실행 요청");

        try {
            // 테스트용으로 고객 ID 5번만 실행
            Long testCustomerId = 5L;
            
            RebalancingSimulationRequest request = RebalancingSimulationRequest.builder()
                    .customerId(testCustomerId)
                    .triggerType("BAND_BASED")
                    .build();

            RebalancingSimulationResponse response = rebalancingService.simulateRebalancing(request);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "밴드 기반 배치 실행 완료");
            result.put("jobId", response.getJobId());
            result.put("customerId", testCustomerId);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("밴드 기반 배치 수동 실행 실패", e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "배치 실행 실패: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(result);
        }
    }
}
