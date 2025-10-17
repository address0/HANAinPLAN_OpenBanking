package com.hanainplan.domain.portfolio.controller;

import com.hanainplan.domain.portfolio.dto.RebalancingSimulationRequest;
import com.hanainplan.domain.portfolio.dto.RebalancingSimulationResponse;
import com.hanainplan.domain.portfolio.service.RebalancingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/irp/rebalancing")
@RequiredArgsConstructor
@Slf4j
public class RebalancingController {

    private final RebalancingService rebalancingService;

    /**
     * 리밸런싱 시뮬레이션 실행
     */
    @PostMapping("/{customerId}/simulate")
    public ResponseEntity<RebalancingSimulationResponse> simulateRebalancing(
            @PathVariable Long customerId,
            @RequestBody(required = false) RebalancingSimulationRequest request) {
        
        log.info("리밸런싱 시뮬레이션 요청 - 고객 ID: {}", customerId);

        // 요청이 없으면 기본값으로 생성
        if (request == null) {
            request = RebalancingSimulationRequest.builder()
                    .customerId(customerId)
                    .triggerType("MANUAL")
                    .build();
        } else {
            request.setCustomerId(customerId);
        }

        RebalancingSimulationResponse response = 
                rebalancingService.simulateRebalancing(request);

        return ResponseEntity.ok(response);
    }

    /**
     * 추천 포트폴리오 기반 리밸런싱 시뮬레이션
     */
    @PostMapping("/{customerId}/simulate-recommended")
    public ResponseEntity<RebalancingSimulationResponse> simulateRecommendedRebalancing(
            @PathVariable Long customerId) {
        
        log.info("추천 포트폴리오 기반 리밸런싱 시뮬레이션 요청 - 고객 ID: {}", customerId);

        RebalancingSimulationRequest request = 
                RebalancingSimulationRequest.builder()
                        .customerId(customerId)
                        .triggerType("MANUAL")
                        .build();

        RebalancingSimulationResponse response = 
                rebalancingService.simulateRebalancing(request);

        return ResponseEntity.ok(response);
    }

    /**
     * 사용자 지정 목표 비중으로 리밸런싱 시뮬레이션
     */
    @PostMapping("/{customerId}/simulate-custom")
    public ResponseEntity<RebalancingSimulationResponse> simulateCustomRebalancing(
            @PathVariable Long customerId,
            @RequestParam BigDecimal cashWeight,
            @RequestParam BigDecimal depositWeight,
            @RequestParam BigDecimal fundWeight) {
        
        log.info("사용자 지정 리밸런싱 시뮬레이션 요청 - 고객 ID: {}, 현금: {}%, 예금: {}%, 펀드: {}%", 
                customerId, cashWeight, depositWeight, fundWeight);

        RebalancingSimulationRequest request = 
                RebalancingSimulationRequest.builder()
                        .customerId(customerId)
                        .triggerType("MANUAL")
                        .cashWeight(cashWeight)
                        .depositWeight(depositWeight)
                        .fundWeight(fundWeight)
                        .build();

        RebalancingSimulationResponse response = 
                rebalancingService.simulateRebalancing(request);

        return ResponseEntity.ok(response);
    }

    /**
     * 리밸런싱 실행 승인
     */
    @PostMapping("/{jobId}/approve")
    public ResponseEntity<RebalancingSimulationResponse> approveRebalancing(
            @PathVariable Long jobId) {
        
        log.info("리밸런싱 실행 승인 요청 - Job ID: {}", jobId);

        RebalancingSimulationResponse response = 
                rebalancingService.approveAndExecuteRebalancing(jobId);

        return ResponseEntity.ok(response);
    }

    /**
     * 리밸런싱 실행 상태 조회
     */
    @GetMapping("/{jobId}/status")
    public ResponseEntity<RebalancingSimulationResponse> getRebalancingStatus(
            @PathVariable Long jobId) {
        
        log.info("리밸런싱 상태 조회 요청 - Job ID: {}", jobId);

        RebalancingSimulationResponse response = 
                rebalancingService.getRebalancingStatus(jobId);

        return ResponseEntity.ok(response);
    }
}
