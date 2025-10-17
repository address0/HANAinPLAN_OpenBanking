package com.hanainplan.domain.portfolio.service;

import com.hanainplan.domain.banking.entity.DepositPortfolio;
import com.hanainplan.domain.banking.entity.IrpAccount;
import com.hanainplan.domain.banking.repository.DepositPortfolioRepository;
import com.hanainplan.domain.banking.repository.IrpAccountRepository;
import com.hanainplan.domain.fund.entity.FundPortfolio;
import com.hanainplan.domain.fund.repository.FundPortfolioRepository;
import com.hanainplan.domain.notification.entity.NotificationType;
import com.hanainplan.domain.notification.service.NotificationService;
import com.hanainplan.domain.notification.dto.NotificationDto;
import com.hanainplan.domain.portfolio.client.HanaBankClient;
import com.hanainplan.domain.portfolio.dto.PortfolioRecommendationResponse;
import com.hanainplan.domain.portfolio.dto.RebalancingSimulationRequest;
import com.hanainplan.domain.portfolio.dto.RebalancingSimulationResponse;
import com.hanainplan.domain.portfolio.entity.RebalancingJob;
import com.hanainplan.domain.portfolio.repository.RebalancingJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RebalancingService {

    private final IrpAccountRepository irpAccountRepository;
    private final DepositPortfolioRepository depositPortfolioRepository;
    private final FundPortfolioRepository fundPortfolioRepository;
    private final PortfolioRecommendationService portfolioRecommendationService;
    private final RebalancingJobRepository rebalancingJobRepository;
    private final NotificationService notificationService;
    private final HanaBankClient hanaBankClient;

    /**
     * 리밸런싱 시뮬레이션 실행
     */
    @Transactional
    public RebalancingSimulationResponse simulateRebalancing(
            RebalancingSimulationRequest request) {
        
        log.info("리밸런싱 시뮬레이션 시작 - 고객 ID: {}", request.getCustomerId());

        // 1. IRP 계좌 조회
        Optional<IrpAccount> irpAccountOpt = irpAccountRepository.findByCustomerIdAndAccountStatus(
                request.getCustomerId(), "ACTIVE");
        
        if (irpAccountOpt.isEmpty()) {
            throw new IllegalArgumentException("활성화된 IRP 계좌를 찾을 수 없습니다.");
        }

        IrpAccount irpAccount = irpAccountOpt.get();
        String irpAccountNumber = irpAccount.getAccountNumber();

        // 2. 현재 포트폴리오 조회
        RebalancingSimulationResponse.PortfolioSnapshot currentPortfolio = 
                getCurrentPortfolioSnapshot(request.getCustomerId(), irpAccountNumber);

        // 3. 목표 포트폴리오 결정
        RebalancingSimulationResponse.PortfolioSnapshot targetPortfolio;
        
        if (request.getCashWeight() != null && request.getDepositWeight() != null && request.getFundWeight() != null) {
            // 사용자가 직접 목표 비중을 지정한 경우
            targetPortfolio = createTargetPortfolioFromWeights(
                    currentPortfolio.getTotalValue(),
                    request.getCashWeight(),
                    request.getDepositWeight(),
                    request.getFundWeight()
            );
        } else {
            // 추천 서비스를 통해 목표 포트폴리오 가져오기
            PortfolioRecommendationResponse.SimilarUserPortfolio recommendation = 
                    portfolioRecommendationService.getSimilarUserPortfolio(request.getCustomerId());
            
            targetPortfolio = RebalancingSimulationResponse.PortfolioSnapshot.builder()
                    .totalValue(currentPortfolio.getTotalValue())
                    .cashWeight(recommendation.getCashWeight())
                    .depositWeight(recommendation.getDepositWeight())
                    .fundWeight(recommendation.getFundWeight())
                    .cashAmount(currentPortfolio.getTotalValue().multiply(recommendation.getCashWeight()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                    .depositAmount(currentPortfolio.getTotalValue().multiply(recommendation.getDepositWeight()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                    .fundAmount(currentPortfolio.getTotalValue().multiply(recommendation.getFundWeight()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                    .build();
        }

        // 4. 리밸런싱 주문 생성
        List<RebalancingSimulationResponse.RebalancingOrder> orders = 
                generateRebalancingOrders(request.getCustomerId(), irpAccountNumber, currentPortfolio, targetPortfolio);

        // 5. 예상 포트폴리오 계산
        RebalancingSimulationResponse.PortfolioSnapshot expectedPortfolio = 
                calculateExpectedPortfolio(currentPortfolio, orders);

        // 6. RebalancingJob 저장
        RebalancingJob job = RebalancingJob.builder()
                .customerId(request.getCustomerId())
                .irpAccountNumber(irpAccountNumber)
                .jobType(RebalancingJob.JobType.SIMULATION)
                .triggerType(RebalancingJob.TriggerType.valueOf(request.getTriggerType()))
                .status(RebalancingJob.JobStatus.PENDING)
                .currentPortfolio("{}") // 임시
                .targetPortfolio("{}") // 임시
                .rebalancingPlan("[]") // 임시
                .createdAt(LocalDateTime.now())
                .build();

        RebalancingJob savedJob = rebalancingJobRepository.save(job);

        // 7. 응답 생성
        BigDecimal totalFee = orders.stream()
                .map(RebalancingSimulationResponse.RebalancingOrder::getFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOrderAmount = orders.stream()
                .map(RebalancingSimulationResponse.RebalancingOrder::getOrderAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return RebalancingSimulationResponse.builder()
                .jobId(savedJob.getJobId())
                .customerId(request.getCustomerId())
                .irpAccountNumber(irpAccountNumber)
                .triggerType(request.getTriggerType())
                .status("PENDING")
                .currentPortfolio(currentPortfolio)
                .targetPortfolio(targetPortfolio)
                .orders(orders)
                .expectedPortfolio(expectedPortfolio)
                .totalFee(totalFee)
                .totalOrderAmount(totalOrderAmount)
                .message("리밸런싱 시뮬레이션 완료")
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 현재 포트폴리오 스냅샷 생성
     */
    private RebalancingSimulationResponse.PortfolioSnapshot getCurrentPortfolioSnapshot(
            Long customerId, String irpAccountNumber) {
        
        // 현금 잔액
        BigDecimal cashBalance = irpAccountRepository.findByCustomerIdAndAccountStatus(customerId, "ACTIVE")
                .map(IrpAccount::getCurrentBalance)
                .orElse(BigDecimal.ZERO);

        // 예금 총액
        BigDecimal depositTotal = depositPortfolioRepository.findByUserIdAndStatus(customerId, "ACTIVE")
                .stream()
                .map(DepositPortfolio::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 펀드 총액
        BigDecimal fundTotal = fundPortfolioRepository.findByIrpAccountNumberOrderByCreatedAtDesc(irpAccountNumber)
                .stream()
                .filter(FundPortfolio::isActive)
                .map(FundPortfolio::getCurrentValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalValue = cashBalance.add(depositTotal).add(fundTotal);

        return RebalancingSimulationResponse.PortfolioSnapshot.builder()
                .totalValue(totalValue)
                .cashAmount(cashBalance)
                .depositAmount(depositTotal)
                .fundAmount(fundTotal)
                .cashWeight(totalValue.compareTo(BigDecimal.ZERO) > 0 ? 
                        cashBalance.multiply(BigDecimal.valueOf(100)).divide(totalValue, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .depositWeight(totalValue.compareTo(BigDecimal.ZERO) > 0 ? 
                        depositTotal.multiply(BigDecimal.valueOf(100)).divide(totalValue, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .fundWeight(totalValue.compareTo(BigDecimal.ZERO) > 0 ? 
                        fundTotal.multiply(BigDecimal.valueOf(100)).divide(totalValue, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .build();
    }

    /**
     * 비중으로부터 목표 포트폴리오 생성
     */
    private RebalancingSimulationResponse.PortfolioSnapshot createTargetPortfolioFromWeights(
            BigDecimal totalValue, BigDecimal cashWeight, BigDecimal depositWeight, BigDecimal fundWeight) {
        
        return RebalancingSimulationResponse.PortfolioSnapshot.builder()
                .totalValue(totalValue)
                .cashWeight(cashWeight)
                .depositWeight(depositWeight)
                .fundWeight(fundWeight)
                .cashAmount(totalValue.multiply(cashWeight).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                .depositAmount(totalValue.multiply(depositWeight).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                .fundAmount(totalValue.multiply(fundWeight).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP))
                .build();
    }

    /**
     * 리밸런싱 주문 생성
     */
    private List<RebalancingSimulationResponse.RebalancingOrder> generateRebalancingOrders(
            Long customerId, String irpAccountNumber,
            RebalancingSimulationResponse.PortfolioSnapshot current,
            RebalancingSimulationResponse.PortfolioSnapshot target) {
        
        List<RebalancingSimulationResponse.RebalancingOrder> orders = new ArrayList<>();

        // 펀드 비중 편차 계산
        BigDecimal fundDrift = current.getFundWeight().subtract(target.getFundWeight());
        
        // 펀드 비중이 목표보다 낮은 경우 (매수 필요)
        if (fundDrift.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal buyAmount = target.getFundAmount().subtract(current.getFundAmount());
            
            if (buyAmount.compareTo(BigDecimal.valueOf(100000)) > 0) { // 최소 거래 금액 10만원
                // 현금에서 펀드 매수
                orders.add(RebalancingSimulationResponse.RebalancingOrder.builder()
                        .orderType("BUY")
                        .assetType("FUND")
                        .fundCode("RECOMMENDED_FUND")
                        .fundName("추천 펀드")
                        .orderAmount(buyAmount)
                        .expectedNav(BigDecimal.valueOf(1000)) // 임시 NAV
                        .orderUnits(buyAmount.divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP))
                        .fee(buyAmount.multiply(BigDecimal.valueOf(0.0015))) // 0.15% 수수료
                        .reason("펀드 비중 부족으로 매수 필요")
                        .build());
            }
        }
        
        // 펀드 비중이 목표보다 높은 경우 (매도 필요)
        else if (fundDrift.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal sellAmount = current.getFundAmount().subtract(target.getFundAmount());
            
            if (sellAmount.compareTo(BigDecimal.valueOf(100000)) > 0) { // 최소 거래 금액 10만원
                // 펀드 매도
                orders.add(RebalancingSimulationResponse.RebalancingOrder.builder()
                        .orderType("SELL")
                        .assetType("FUND")
                        .fundCode("EXISTING_FUND")
                        .fundName("기존 펀드")
                        .orderAmount(sellAmount)
                        .expectedNav(BigDecimal.valueOf(1000)) // 임시 NAV
                        .orderUnits(sellAmount.divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP))
                        .fee(sellAmount.multiply(BigDecimal.valueOf(0.0015))) // 0.15% 수수료
                        .reason("펀드 비중 초과로 매도 필요")
                        .build());
            }
        }

        return orders;
    }

    /**
     * 예상 포트폴리오 계산
     */
    private RebalancingSimulationResponse.PortfolioSnapshot calculateExpectedPortfolio(
            RebalancingSimulationResponse.PortfolioSnapshot current,
            List<RebalancingSimulationResponse.RebalancingOrder> orders) {
        
        BigDecimal expectedCash = current.getCashAmount();
        BigDecimal expectedDeposit = current.getDepositAmount();
        BigDecimal expectedFund = current.getFundAmount();

        for (RebalancingSimulationResponse.RebalancingOrder order : orders) {
            if ("BUY".equals(order.getOrderType()) && "FUND".equals(order.getAssetType())) {
                expectedCash = expectedCash.subtract(order.getOrderAmount());
                expectedFund = expectedFund.add(order.getOrderAmount());
            } else if ("SELL".equals(order.getOrderType()) && "FUND".equals(order.getAssetType())) {
                expectedCash = expectedCash.add(order.getOrderAmount());
                expectedFund = expectedFund.subtract(order.getOrderAmount());
            }
        }

        BigDecimal totalValue = expectedCash.add(expectedDeposit).add(expectedFund);

        return RebalancingSimulationResponse.PortfolioSnapshot.builder()
                .totalValue(totalValue)
                .cashAmount(expectedCash)
                .depositAmount(expectedDeposit)
                .fundAmount(expectedFund)
                .cashWeight(totalValue.compareTo(BigDecimal.ZERO) > 0 ? 
                        expectedCash.multiply(BigDecimal.valueOf(100)).divide(totalValue, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .depositWeight(totalValue.compareTo(BigDecimal.ZERO) > 0 ? 
                        expectedDeposit.multiply(BigDecimal.valueOf(100)).divide(totalValue, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .fundWeight(totalValue.compareTo(BigDecimal.ZERO) > 0 ? 
                        expectedFund.multiply(BigDecimal.valueOf(100)).divide(totalValue, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .build();
    }

    /**
     * 리밸런싱 실행 승인 및 실행
     */
    @Transactional
    public RebalancingSimulationResponse approveAndExecuteRebalancing(Long jobId) {
        log.info("리밸런싱 실행 승인 및 실행 - Job ID: {}", jobId);

        // 1. RebalancingJob 조회
        RebalancingJob job = rebalancingJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("리밸런싱 작업을 찾을 수 없습니다: " + jobId));

        // 2. 상태 확인 (SIMULATION이어야 함)
        if (!RebalancingJob.JobType.SIMULATION.equals(job.getJobType())) {
            throw new IllegalArgumentException("시뮬레이션 작업만 실행할 수 있습니다.");
        }

        if (!RebalancingJob.JobStatus.PENDING.equals(job.getStatus())) {
            throw new IllegalArgumentException("대기 상태의 작업만 실행할 수 있습니다.");
        }

        // 3. 상태를 EXECUTING으로 변경
        job.setStatus(RebalancingJob.JobStatus.EXECUTING);
        job.setApprovedAt(LocalDateTime.now());
        rebalancingJobRepository.save(job);

        // 4. 실제 리밸런싱 실행 (하나은행 API 연동)
        try {
            // 실제 펀드 거래 실행
            executeFundTransactions(job);

                // 5. 실행 완료 처리
                job.setStatus(RebalancingJob.JobStatus.COMPLETED);
                job.setCompletedAt(LocalDateTime.now());
                rebalancingJobRepository.save(job);

                // 6. 성공 알림 전송
                sendRebalancingSuccessNotification(job.getCustomerId(), job.getJobId());

                // 7. 응답 생성
                return RebalancingSimulationResponse.builder()
                        .jobId(jobId)
                        .customerId(job.getCustomerId())
                        .irpAccountNumber(job.getIrpAccountNumber())
                        .triggerType(job.getTriggerType().name())
                        .status("COMPLETED")
                        .message("리밸런싱 실행이 완료되었습니다.")
                        .createdAt(job.getCreatedAt())
                        .build();

        } catch (Exception e) {
            // 실행 실패 처리
            job.setStatus(RebalancingJob.JobStatus.FAILED);
            rebalancingJobRepository.save(job);

            // 실패 알림 전송
            sendRebalancingFailureNotification(job.getCustomerId(), job.getJobId(), e.getMessage());

            throw new RuntimeException("리밸런싱 실행 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 리밸런싱 상태 조회
     */
    public RebalancingSimulationResponse getRebalancingStatus(Long jobId) {
        log.info("리밸런싱 상태 조회 - Job ID: {}", jobId);

        RebalancingJob job = rebalancingJobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("리밸런싱 작업을 찾을 수 없습니다: " + jobId));

        return RebalancingSimulationResponse.builder()
                .jobId(jobId)
                .customerId(job.getCustomerId())
                .irpAccountNumber(job.getIrpAccountNumber())
                .triggerType(job.getTriggerType().name())
                .status(job.getStatus().name())
                .message("리밸런싱 상태: " + job.getStatus().getDescription())
                .createdAt(job.getCreatedAt())
                .build();
    }

    /**
     * 실제 펀드 거래 실행 (하나은행 API 연동)
     */
    private void executeFundTransactions(RebalancingJob job) {
        log.info("펀드 거래 실행 시작 - Job ID: {}", job.getJobId());

        try {
            // TODO: 실제 구현 시에는 다음과 같이 처리
            // 1. RebalancingOrder 목록 조회
            // 2. 각 주문에 대해 하나은행 API 호출
            // 3. 체결 결과를 RebalancingOrder에 업데이트
            // 4. FundPortfolio 테이블 업데이트
            // (펀드 NAV는 파이썬 크롤러가 정기적으로 업데이트하므로 별도 크롤링 불필요)

            // 실제 펀드 거래 실행 (하나은행 API 연동)
            executeActualFundTransactions(job);

            // 현재는 시뮬레이션으로 처리
            Thread.sleep(2000); // 실제 API 호출 시뮬레이션
            log.info("펀드 거래 실행 완료 - Job ID: {}", job.getJobId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("펀드 거래 실행 중 중단됨", e);
        }
    }

    /**
     * 실제 펀드 거래 실행 (하나은행 API 연동)
     */
    private void executeActualFundTransactions(RebalancingJob job) {
        log.info("실제 펀드 거래 실행 시작 - Job ID: {}", job.getJobId());

        try {
            // 고객 CI 조회 (IRP 계좌번호로 고객 정보 조회)
            String customerCi = getCustomerCiByIrpAccount(job.getIrpAccountNumber());
            
            // 현재 보유 펀드 조회
            List<FundPortfolio> currentFunds = fundPortfolioRepository
                    .findByIrpAccountNumberOrderByCreatedAtDesc(job.getIrpAccountNumber());

            // 리밸런싱 주문 생성 (간단한 예시)
            // 실제로는 시뮬레이션 결과의 주문을 사용해야 함
            BigDecimal targetFundAmount = BigDecimal.valueOf(1000000); // 목표 펀드 금액
            BigDecimal currentFundAmount = currentFunds.stream()
                    .filter(FundPortfolio::isActive)
                    .map(FundPortfolio::getCurrentValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal fundDifference = targetFundAmount.subtract(currentFundAmount);

            if (fundDifference.compareTo(BigDecimal.ZERO) > 0) {
                // 펀드 매수 필요
                log.info("펀드 매수 실행 - 금액: {}원", fundDifference);
                
                // 예시: 첫 번째 펀드에 매수 (실제로는 추천 펀드 선택 로직 필요)
                String fundCode = "513051"; // 예시 펀드 코드
                
                HanaBankClient.FundPurchaseResponse response = hanaBankClient.purchaseFund(
                        customerCi, fundCode, fundDifference);
                
                if (response.isSuccess()) {
                    log.info("펀드 매수 성공 - 구독ID: {}, 매수좌수: {}", 
                            response.getSubscriptionId(), response.getPurchaseUnits());
                } else {
                    log.error("펀드 매수 실패 - {}", response.getErrorMessage());
                }
                
            } else if (fundDifference.compareTo(BigDecimal.ZERO) < 0) {
                // 펀드 매도 필요
                log.info("펀드 매도 실행 - 금액: {}원", fundDifference.abs());
                
                // 예시: 첫 번째 활성 펀드 매도
                FundPortfolio fundToSell = currentFunds.stream()
                        .filter(FundPortfolio::isActive)
                        .findFirst()
                        .orElse(null);
                
                if (fundToSell != null) {
                    // 전체 매도
                    HanaBankClient.FundRedemptionResponse response = hanaBankClient.redeemFund(
                            customerCi, fundToSell.getSubscriptionId(), fundToSell.getCurrentUnits(), true);
                    
                    if (response.isSuccess()) {
                        log.info("펀드 매도 성공 - 매도좌수: {}, 실수령액: {}원", 
                                response.getSellUnits(), response.getNetAmount());
                    } else {
                        log.error("펀드 매도 실패 - {}", response.getErrorMessage());
                    }
                }
            }

        } catch (Exception e) {
            log.error("실제 펀드 거래 실행 중 오류 발생", e);
            throw new RuntimeException("펀드 거래 실행 실패: " + e.getMessage());
        }
    }

    /**
     * IRP 계좌번호로 고객 CI 조회
     */
    private String getCustomerCiByIrpAccount(String irpAccountNumber) {
        // 실제로는 IRP 계좌 테이블에서 고객 CI를 조회해야 함
        // 현재는 테스트용 고객 CI 반환
        return "QjVLbDVMeWhFSGgrQkR6QkRxTkhza1Fp";
    }

    /**
     * 리밸런싱 성공 알림 전송
     */
    private void sendRebalancingSuccessNotification(Long customerId, Long jobId) {
        try {
            NotificationDto.CreateRequest request = NotificationDto.CreateRequest.builder()
                    .userId(customerId)
                    .title("IRP 포트폴리오 리밸런싱 완료")
                    .content(String.format("IRP 포트폴리오 리밸런싱이 성공적으로 완료되었습니다.\n작업 ID: %d\n완료 시간: %s", 
                            jobId, LocalDateTime.now().toString()))
                    .type(NotificationType.REBALANCING)
                    .build();

            notificationService.createNotification(request);
            log.info("리밸런싱 성공 알림 전송 완료 - 고객 ID: {}, 작업 ID: {}", customerId, jobId);
        } catch (Exception e) {
            log.error("리밸런싱 성공 알림 전송 실패 - 고객 ID: {}, 작업 ID: {}", customerId, jobId, e);
        }
    }

    /**
     * 리밸런싱 실패 알림 전송
     */
    private void sendRebalancingFailureNotification(Long customerId, Long jobId, String errorMessage) {
        try {
            NotificationDto.CreateRequest request = NotificationDto.CreateRequest.builder()
                    .userId(customerId)
                    .title("IRP 포트폴리오 리밸런싱 실패")
                    .content(String.format("IRP 포트폴리오 리밸런싱 중 오류가 발생했습니다.\n작업 ID: %d\n오류 내용: %s\n실패 시간: %s", 
                            jobId, errorMessage, LocalDateTime.now().toString()))
                    .type(NotificationType.REBALANCING)
                    .build();

            notificationService.createNotification(request);
            log.info("리밸런싱 실패 알림 전송 완료 - 고객 ID: {}, 작업 ID: {}", customerId, jobId);
        } catch (Exception e) {
            log.error("리밸런싱 실패 알림 전송 실패 - 고객 ID: {}, 작업 ID: {}", customerId, jobId, e);
        }
    }
}