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
import com.hanainplan.domain.portfolio.entity.RebalancingOrder;
import com.hanainplan.domain.portfolio.repository.RebalancingJobRepository;
import com.hanainplan.domain.portfolio.repository.RebalancingOrderRepository;
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
    private final RebalancingOrderRepository rebalancingOrderRepository;
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

        // 7. 실제 RebalancingOrder 엔티티 저장
        saveRebalancingOrders(savedJob.getJobId(), orders);

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
                // 추천 펀드 선택 (실제 존재하는 펀드 클래스 사용)
                String recommendedFundCode = getRecommendedFundCode();
                BigDecimal expectedNav = getExpectedNav(recommendedFundCode);
                
                orders.add(RebalancingSimulationResponse.RebalancingOrder.builder()
                        .orderType("BUY")
                        .assetType("FUND")
                        .fundCode(recommendedFundCode)
                        .fundName("미래에셋퇴직플랜30증권자투자신탁1호(채권혼합) P클래스")
                        .orderAmount(buyAmount)
                        .expectedNav(expectedNav)
                        .orderUnits(buyAmount.divide(expectedNav, 4, RoundingMode.HALF_UP))
                        .fee(buyAmount.multiply(BigDecimal.valueOf(0.0015))) // 0.15% 수수료
                        .reason("펀드 비중 부족으로 매수 필요")
                        .build());
            }
        }
        
        // 펀드 비중이 목표보다 높은 경우 (매도 필요)
        else if (fundDrift.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal sellAmount = current.getFundAmount().subtract(target.getFundAmount());
            
            if (sellAmount.compareTo(BigDecimal.valueOf(100000)) > 0) { // 최소 거래 금액 10만원
                // 기존 보유 펀드에서 매도할 펀드 선택
                FundPortfolio fundToSell = getFundToSell(irpAccountNumber);
                
                if (fundToSell != null) {
                    BigDecimal expectedNav = getExpectedNav(fundToSell.getFundCode());
                    
                    orders.add(RebalancingSimulationResponse.RebalancingOrder.builder()
                            .orderType("SELL")
                            .assetType("FUND")
                            .fundCode(fundToSell.getFundCode())
                            .fundName(fundToSell.getFundName())
                            .orderAmount(sellAmount)
                            .expectedNav(expectedNav)
                            .orderUnits(sellAmount.divide(expectedNav, 4, RoundingMode.HALF_UP))
                            .fee(sellAmount.multiply(BigDecimal.valueOf(0.0015))) // 0.15% 수수료
                            .reason("펀드 비중 초과로 매도 필요")
                            .build());
                }
            }
        }

        return orders;
    }

    /**
     * 추천 펀드 코드 반환 (실제 존재하는 펀드 클래스)
     */
    private String getRecommendedFundCode() {
        // 실제 존재하는 펀드 클래스 코드 반환
        return "51305P"; // 미래에셋퇴직플랜30 P클래스
    }

    /**
     * 예상 NAV 조회
     */
    private BigDecimal getExpectedNav(String fundCode) {
        // 실제로는 하나은행 서버에서 최신 NAV를 조회해야 함
        // 현재는 테스트용 고정값 사용
        return BigDecimal.valueOf(1006.48); // 최신 NAV
    }

    /**
     * 매도할 펀드 선택
     */
    private FundPortfolio getFundToSell(String irpAccountNumber) {
        List<FundPortfolio> currentFunds = fundPortfolioRepository
                .findByIrpAccountNumberOrderByCreatedAtDesc(irpAccountNumber);
        
        return currentFunds.stream()
                .filter(FundPortfolio::isActive)
                .findFirst()
                .orElse(null);
    }

    /**
     * 리밸런싱 주문을 RebalancingOrder 엔티티로 저장
     */
    private void saveRebalancingOrders(Long jobId, List<RebalancingSimulationResponse.RebalancingOrder> orders) {
        for (RebalancingSimulationResponse.RebalancingOrder orderDto : orders) {
            RebalancingOrder order = RebalancingOrder.builder()
                    .jobId(jobId)
                    .orderType(RebalancingOrder.OrderType.valueOf(orderDto.getOrderType()))
                    .assetType(RebalancingOrder.AssetType.valueOf(orderDto.getAssetType()))
                    .fundCode(orderDto.getFundCode())
                    .fundName(orderDto.getFundName())
                    .classCode(orderDto.getFundCode()) // 펀드 코드를 클래스 코드로도 사용
                    .expectedNav(orderDto.getExpectedNav())
                    .orderUnits(orderDto.getOrderUnits())
                    .orderAmount(orderDto.getOrderAmount())
                    .fee(orderDto.getFee())
                    .status(RebalancingOrder.OrderStatus.PENDING)
                    .executionReason(orderDto.getReason())
                    .build();
            
            rebalancingOrderRepository.save(order);
        }
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
            // 실제 펀드 거래 실행 (하나은행 API 연동)
            executeActualFundTransactions(job);
            log.info("펀드 거래 실행 완료 - Job ID: {}", job.getJobId());
        } catch (Exception e) {
            log.error("펀드 거래 실행 중 오류 발생", e);
            throw new RuntimeException("펀드 거래 실행 실패: " + e.getMessage());
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
            
            // 리밸런싱 주문 조회 (실제 시뮬레이션 결과의 주문 사용)
            List<RebalancingOrder> orders = rebalancingOrderRepository.findByJobIdAndStatus(
                    job.getJobId(), RebalancingOrder.OrderStatus.PENDING);
            
            if (orders.isEmpty()) {
                log.warn("실행할 리밸런싱 주문이 없습니다 - Job ID: {}", job.getJobId());
                return;
            }

            log.info("리밸런싱 주문 실행 - Job ID: {}, 주문 수: {}", job.getJobId(), orders.size());

            for (RebalancingOrder order : orders) {
                try {
                    if (order.isFund() && order.isBuy()) {
                        // 펀드 매수 실행
                        executeFundPurchase(customerCi, order);
                    } else if (order.isFund() && order.isSell()) {
                        // 펀드 매도 실행
                        executeFundRedemption(customerCi, order, job);
                    }
                } catch (Exception e) {
                    log.error("주문 실행 실패 - Order ID: {}, 오류: {}", order.getOrderId(), e.getMessage());
                    order.fail("주문 실행 실패: " + e.getMessage());
                    rebalancingOrderRepository.save(order);
                }
            }

        } catch (Exception e) {
            log.error("펀드 거래 실행 중 오류 발생", e);
            throw new RuntimeException("펀드 거래 실행 실패: " + e.getMessage());
        }
    }

    /**
     * 펀드 매수 실행
     */
    private void executeFundPurchase(String customerCi, RebalancingOrder order) {
        log.info("펀드 매수 실행 - Order ID: {}, 펀드코드: {}, 금액: {}원", 
                order.getOrderId(), order.getFundCode(), order.getOrderAmount());

        // 펀드 코드 확인 (classCode 우선, 없으면 fundCode 사용)
        String fundCode = order.getClassCode() != null ? order.getClassCode() : order.getFundCode();
        
        if (fundCode == null) {
            order.fail("펀드 코드가 없습니다");
            rebalancingOrderRepository.save(order);
            return;
        }

        HanaBankClient.FundPurchaseResponse response = hanaBankClient.purchaseFund(
                customerCi, fundCode, order.getOrderAmount());
        
        if (response.isSuccess()) {
            log.info("펀드 매수 성공 - Order ID: {}, 구독ID: {}, 매수좌수: {}", 
                    order.getOrderId(), response.getSubscriptionId(), response.getPurchaseUnits());
            
            // HanaBankClient의 응답에는 purchaseNav와 purchaseAmount가 없으므로 주문 정보 사용
            order.fill(
                order.getExpectedNav(), // 예상 NAV 사용
                response.getPurchaseUnits(),
                order.getOrderAmount() // 주문 금액 사용
            );
            order.submit(String.valueOf(response.getSubscriptionId()));
        } else {
            log.error("펀드 매수 실패 - Order ID: {}, 오류: {}", order.getOrderId(), response.getErrorMessage());
            order.fail("펀드 매수 실패: " + response.getErrorMessage());
        }
        
        rebalancingOrderRepository.save(order);
    }

    /**
     * 펀드 매도 실행
     */
    private void executeFundRedemption(String customerCi, RebalancingOrder order, RebalancingJob job) {
        log.info("펀드 매도 실행 - Order ID: {}, 펀드코드: {}, 좌수: {}", 
                order.getOrderId(), order.getFundCode(), order.getOrderUnits());

        // 현재 보유 펀드에서 구독 ID 조회
        List<FundPortfolio> currentFunds = fundPortfolioRepository
                .findByIrpAccountNumberOrderByCreatedAtDesc(job.getIrpAccountNumber());
        
        FundPortfolio fundToSell = currentFunds.stream()
                .filter(fund -> fund.getFundCode().equals(order.getFundCode()))
                .filter(FundPortfolio::isActive)
                .findFirst()
                .orElse(null);
        
        if (fundToSell == null) {
            order.fail("매도할 펀드를 찾을 수 없습니다: " + order.getFundCode());
            rebalancingOrderRepository.save(order);
            return;
        }

        HanaBankClient.FundRedemptionResponse response = hanaBankClient.redeemFund(
                customerCi, fundToSell.getSubscriptionId(), order.getOrderUnits(), false);
        
        if (response.isSuccess()) {
            log.info("펀드 매도 성공 - Order ID: {}, 매도좌수: {}, 실수령액: {}원", 
                    order.getOrderId(), response.getSellUnits(), response.getNetAmount());
            
            order.fill(
                BigDecimal.ZERO, // 매도 시 NAV는 0으로 설정
                response.getSellUnits(),
                response.getNetAmount()
            );
            order.submit("REDEMPTION_" + order.getOrderId()); // 임시 거래 ID
        } else {
            log.error("펀드 매도 실패 - Order ID: {}, 오류: {}", order.getOrderId(), response.getErrorMessage());
            order.fail("펀드 매도 실패: " + response.getErrorMessage());
        }
        
        rebalancingOrderRepository.save(order);
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