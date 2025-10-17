package com.hanainplan.domain.portfolio.batch;

import com.hanainplan.domain.banking.entity.IrpAccount;
import com.hanainplan.domain.banking.repository.IrpAccountRepository;
import com.hanainplan.domain.portfolio.dto.RebalancingSimulationRequest;
import com.hanainplan.domain.portfolio.entity.RebalancingJob;
import com.hanainplan.domain.portfolio.repository.RebalancingJobRepository;
import com.hanainplan.domain.portfolio.service.RebalancingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RebalancingScheduler {

    private final IrpAccountRepository irpAccountRepository;
    private final RebalancingJobRepository rebalancingJobRepository;
    private final RebalancingService rebalancingService;

    /**
     * 시간 기반 리밸런싱 배치 (매월 1일 오전 9시 실행)
     */
    @Scheduled(cron = "0 0 9 1 * ?")
    @Transactional
    public void executeTimeBasedRebalancing() {
        log.info("시간 기반 리밸런싱 배치 시작 - {}", LocalDateTime.now());

        try {
            // 활성화된 모든 IRP 계좌 조회
            List<IrpAccount> activeIrpAccounts = irpAccountRepository
                    .findByAccountStatusOrderByCreatedDateDesc("ACTIVE");

            log.info("시간 기반 리밸런싱 대상 계좌 수: {}", activeIrpAccounts.size());

            int successCount = 0;
            int failCount = 0;

            for (IrpAccount irpAccount : activeIrpAccounts) {
                try {
                    // 리밸런싱 시뮬레이션 실행
                    RebalancingSimulationRequest request = RebalancingSimulationRequest.builder()
                            .customerId(irpAccount.getCustomerId())
                            .triggerType("TIME_BASED")
                            .build();

                    var simulationResponse = rebalancingService.simulateRebalancing(request);

                    // 시뮬레이션 결과가 있으면 자동 실행
                    if (simulationResponse.getOrders() != null && 
                        !simulationResponse.getOrders().isEmpty()) {
                        
                        // 자동 실행
                        rebalancingService.approveAndExecuteRebalancing(simulationResponse.getJobId());
                        successCount++;
                        
                        log.info("시간 기반 리밸런싱 완료 - 고객 ID: {}, 작업 ID: {}", 
                                irpAccount.getCustomerId(), simulationResponse.getJobId());
                    } else {
                        log.info("시간 기반 리밸런싱 불필요 - 고객 ID: {}", irpAccount.getCustomerId());
                    }

                } catch (Exception e) {
                    failCount++;
                    log.error("시간 기반 리밸런싱 실패 - 고객 ID: {}", irpAccount.getCustomerId(), e);
                }
            }

            log.info("시간 기반 리밸런싱 배치 완료 - 성공: {}, 실패: {}", successCount, failCount);

        } catch (Exception e) {
            log.error("시간 기반 리밸런싱 배치 실행 중 오류 발생", e);
        }
    }

    /**
     * 밴드 기반 리밸런싱 배치 (매일 오전 10시 실행)
     */
    @Scheduled(cron = "0 0 10 * * ?")
    @Transactional
    public void executeBandBasedRebalancing() {
        log.info("밴드 기반 리밸런싱 배치 시작 - {}", LocalDateTime.now());

        try {
            // 활성화된 모든 IRP 계좌 조회
            List<IrpAccount> activeIrpAccounts = irpAccountRepository
                    .findByAccountStatusOrderByCreatedDateDesc("ACTIVE");

            log.info("밴드 기반 리밸런싱 대상 계좌 수: {}", activeIrpAccounts.size());

            int successCount = 0;
            int failCount = 0;
            int skipCount = 0;

            for (IrpAccount irpAccount : activeIrpAccounts) {
                try {
                    // 최근 리밸런싱 작업 확인 (7일 이내 실행된 작업이 있으면 스킵)
                    LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
                    List<RebalancingJob> recentJobs = rebalancingJobRepository
                            .findByCustomerIdOrderByCreatedAtDesc(irpAccount.getCustomerId())
                            .stream()
                            .filter(job -> job.getCreatedAt().isAfter(sevenDaysAgo))
                            .toList();

                    if (!recentJobs.isEmpty()) {
                        skipCount++;
                        log.debug("최근 리밸런싱 작업 존재로 스킵 - 고객 ID: {}", irpAccount.getCustomerId());
                        continue;
                    }

                    // 밴드 기반 리밸런싱 시뮬레이션 실행
                    RebalancingSimulationRequest request = RebalancingSimulationRequest.builder()
                            .customerId(irpAccount.getCustomerId())
                            .triggerType("BAND_BASED")
                            .build();

                    var simulationResponse = rebalancingService.simulateRebalancing(request);

                    // 밴드 임계값 초과 시에만 실행
                    if (isBandThresholdExceeded(simulationResponse)) {
                        // 자동 실행
                        rebalancingService.approveAndExecuteRebalancing(simulationResponse.getJobId());
                        successCount++;
                        
                        log.info("밴드 기반 리밸런싱 완료 - 고객 ID: {}, 작업 ID: {}", 
                                irpAccount.getCustomerId(), simulationResponse.getJobId());
                    } else {
                        skipCount++;
                        log.debug("밴드 임계값 미초과로 스킵 - 고객 ID: {}", irpAccount.getCustomerId());
                    }

                } catch (Exception e) {
                    failCount++;
                    log.error("밴드 기반 리밸런싱 실패 - 고객 ID: {}", irpAccount.getCustomerId(), e);
                }
            }

            log.info("밴드 기반 리밸런싱 배치 완료 - 성공: {}, 실패: {}, 스킵: {}", successCount, failCount, skipCount);

        } catch (Exception e) {
            log.error("밴드 기반 리밸런싱 배치 실행 중 오류 발생", e);
        }
    }

    /**
     * 밴드 임계값 초과 여부 확인
     */
    private boolean isBandThresholdExceeded(com.hanainplan.domain.portfolio.dto.RebalancingSimulationResponse simulation) {
        if (simulation.getCurrentPortfolio() == null || simulation.getTargetPortfolio() == null) {
            return false;
        }

        // 밴드 임계값 설정 (펀드 ±5%, 예금 ±3%, 현금 ±2%)
        double fundThreshold = 5.0;
        double depositThreshold = 3.0;
        double cashThreshold = 2.0;

        // 현재 비중과 목표 비중의 차이 계산
        double fundDeviation = Math.abs(simulation.getCurrentPortfolio().getFundWeight().doubleValue() - 
                                      simulation.getTargetPortfolio().getFundWeight().doubleValue());
        double depositDeviation = Math.abs(simulation.getCurrentPortfolio().getDepositWeight().doubleValue() - 
                                         simulation.getTargetPortfolio().getDepositWeight().doubleValue());
        double cashDeviation = Math.abs(simulation.getCurrentPortfolio().getCashWeight().doubleValue() - 
                                      simulation.getTargetPortfolio().getCashWeight().doubleValue());

        // 임계값 초과 여부 확인
        boolean fundExceeded = fundDeviation >= fundThreshold;
        boolean depositExceeded = depositDeviation >= depositThreshold;
        boolean cashExceeded = cashDeviation >= cashThreshold;

        log.debug("밴드 임계값 확인 - 펀드: {}% (임계값: {}%), 예금: {}% (임계값: {}%), 현금: {}% (임계값: {}%)",
                fundDeviation, fundThreshold, depositDeviation, depositThreshold, cashDeviation, cashThreshold);

        return fundExceeded || depositExceeded || cashExceeded;
    }

    /**
     * 테스트용 배치 (매 5분마다 실행) - 개발 환경에서만 사용
     */
    @Scheduled(cron = "0 */5 * * * ?")
    @Transactional
    public void executeTestRebalancing() {
        // 개발 환경에서만 실행 (프로덕션에서는 비활성화)
        String profile = System.getProperty("spring.profiles.active", "local");
        if (!"local".equals(profile) && !"dev".equals(profile)) {
            return;
        }

        log.info("테스트 리밸런싱 배치 시작 - {}", LocalDateTime.now());

        try {
            // 테스트용으로 고객 ID 5번만 실행
            Long testCustomerId = 5L;
            
            RebalancingSimulationRequest request = RebalancingSimulationRequest.builder()
                    .customerId(testCustomerId)
                    .triggerType("TEST")
                    .build();

            var simulationResponse = rebalancingService.simulateRebalancing(request);
            
            log.info("테스트 리밸런싱 시뮬레이션 완료 - 고객 ID: {}, 작업 ID: {}", 
                    testCustomerId, simulationResponse.getJobId());

        } catch (Exception e) {
            log.error("테스트 리밸런싱 배치 실행 중 오류 발생", e);
        }
    }
}
