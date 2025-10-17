package com.hanainplan.domain.portfolio.service;

import com.hanainplan.domain.portfolio.client.FastApiClient;
import com.hanainplan.domain.portfolio.dto.PortfolioRecommendationRequest;
import com.hanainplan.domain.portfolio.dto.PortfolioRecommendationResponse;
import com.hanainplan.domain.user.entity.Customer;
import com.hanainplan.domain.user.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PortfolioRecommendationService {

    private final CustomerRepository customerRepository;
    private final FastApiClient fastApiClient;

    // 모델 포트폴리오 가중치 (60%)
    private static final BigDecimal MODEL_WEIGHT = BigDecimal.valueOf(0.6);
    
    // 유사 사용자 포트폴리오 가중치 (40%)
    private static final BigDecimal SIMILAR_USER_WEIGHT = BigDecimal.valueOf(0.4);

    /**
     * 포트폴리오 추천 생성 (모델 + 유사 사용자 결합)
     */
    public PortfolioRecommendationResponse generateRecommendation(Long customerId) {
        log.info("포트폴리오 추천 생성 요청 - 고객 ID: {}", customerId);

        // 고객 정보 조회
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + customerId));

        if (!customer.hasIrpAccount()) {
            throw new IllegalArgumentException("IRP 계좌가 없는 고객입니다: " + customerId);
        }

        // 1. 모델 포트폴리오 생성 (리스크 프로파일 기반)
        PortfolioRecommendationResponse.ModelPortfolio modelPortfolio = createModelPortfolio(customer);

        // 2. 유사 사용자 포트폴리오 생성 (FastAPI 호출)
        PortfolioRecommendationResponse.SimilarUserPortfolio similarUserPortfolio = createSimilarUserPortfolio(customer);

        // 3. 추천 포트폴리오 생성 (가중 평균)
        PortfolioRecommendationResponse.RecommendedPortfolio recommendedPortfolio = 
            createRecommendedPortfolio(modelPortfolio, similarUserPortfolio);

        // 4. 메타데이터 생성
        PortfolioRecommendationResponse.RecommendationMetadata metadata = createMetadata(
            customer, similarUserPortfolio.getSimilarUserCount());

        log.info("포트폴리오 추천 생성 완료 - 고객 ID: {}, 리스크 타입: {}", 
                customerId, customer.getRiskProfileType());

        return PortfolioRecommendationResponse.create(
            customerId,
            customer.getIrpAccountNumber(),
            customer.getRiskProfileType(),
            modelPortfolio,
            similarUserPortfolio,
            recommendedPortfolio,
            metadata
        );
    }

    /**
     * 모델 포트폴리오 생성 (리스크 프로파일 기반)
     */
    private PortfolioRecommendationResponse.ModelPortfolio createModelPortfolio(Customer customer) {
        Customer.RiskProfileType riskType = customer.getRiskProfileType();
        
        if (riskType == null) {
            riskType = Customer.RiskProfileType.STABLE_PLUS; // 기본값
        }

        return switch (riskType) {
            case STABLE -> PortfolioRecommendationResponse.ModelPortfolio.builder()
                    .cashWeight(BigDecimal.valueOf(5.0))
                    .depositWeight(BigDecimal.valueOf(80.0))
                    .fundWeight(BigDecimal.valueOf(15.0))
                    .description("안정형: 원금보전 중심")
                    .basis("RISK_PROFILE")
                    .build();
            case STABLE_PLUS -> PortfolioRecommendationResponse.ModelPortfolio.builder()
                    .cashWeight(BigDecimal.valueOf(5.0))
                    .depositWeight(BigDecimal.valueOf(60.0))
                    .fundWeight(BigDecimal.valueOf(35.0))
                    .description("안정추구형: 저위험·저수익")
                    .basis("RISK_PROFILE")
                    .build();
            case NEUTRAL -> PortfolioRecommendationResponse.ModelPortfolio.builder()
                    .cashWeight(BigDecimal.valueOf(5.0))
                    .depositWeight(BigDecimal.valueOf(40.0))
                    .fundWeight(BigDecimal.valueOf(55.0))
                    .description("중립형: 균형투자")
                    .basis("RISK_PROFILE")
                    .build();
            case AGGRESSIVE -> PortfolioRecommendationResponse.ModelPortfolio.builder()
                    .cashWeight(BigDecimal.valueOf(5.0))
                    .depositWeight(BigDecimal.valueOf(25.0))
                    .fundWeight(BigDecimal.valueOf(70.0))
                    .description("적극형: 성장지향")
                    .basis("RISK_PROFILE")
                    .build();
        };
    }

    /**
     * 유사 사용자 포트폴리오 생성 (FastAPI 호출)
     */
    private PortfolioRecommendationResponse.SimilarUserPortfolio createSimilarUserPortfolio(Customer customer) {
        // FastAPI 서버 상태 확인
        if (!fastApiClient.isFastApiAvailable()) {
            log.warn("FastAPI 서버가 사용 불가능합니다. 기본 유사 사용자 포트폴리오를 사용합니다.");
            return createDefaultSimilarUserPortfolio();
        }

        // FastAPI 요청 데이터 구성
        PortfolioRecommendationRequest request = 
            PortfolioRecommendationRequest.builder()
                .customerId(customer.getCustomerId())
                .irpAccountNumber(customer.getIrpAccountNumber())
                .birthYear(calculateBirthYear(customer))
                .industryCode(customer.getIndustryCode() != null ? customer.getIndustryCode() : "UNKNOWN")
                .assetLevel(customer.getAssetLevel() != null ? customer.getAssetLevel().name() : "FROM_1_TO_5")
                .riskProfileScore(customer.getRiskProfileScore() != null ? customer.getRiskProfileScore() : BigDecimal.valueOf(2.5))
                .hasDisease(customer.hasHealthRisk())
                .build();

        return fastApiClient.getSimilarUserPortfolio(request);
    }

    /**
     * 추천 포트폴리오 생성 (모델 60% + 유사 사용자 40% 가중 평균)
     */
    private PortfolioRecommendationResponse.RecommendedPortfolio createRecommendedPortfolio(
            PortfolioRecommendationResponse.ModelPortfolio modelPortfolio,
            PortfolioRecommendationResponse.SimilarUserPortfolio similarUserPortfolio) {

        // 가중 평균 계산
        BigDecimal recommendedCashWeight = modelPortfolio.getCashWeight().multiply(MODEL_WEIGHT)
                .add(similarUserPortfolio.getCashWeight().multiply(SIMILAR_USER_WEIGHT));

        BigDecimal recommendedDepositWeight = modelPortfolio.getDepositWeight().multiply(MODEL_WEIGHT)
                .add(similarUserPortfolio.getDepositWeight().multiply(SIMILAR_USER_WEIGHT));

        BigDecimal recommendedFundWeight = modelPortfolio.getFundWeight().multiply(MODEL_WEIGHT)
                .add(similarUserPortfolio.getFundWeight().multiply(SIMILAR_USER_WEIGHT));

        // 펀드 70% 상한 제약 적용
        if (recommendedFundWeight.compareTo(BigDecimal.valueOf(70.0)) > 0) {
            BigDecimal excessFund = recommendedFundWeight.subtract(BigDecimal.valueOf(70.0));
            recommendedFundWeight = BigDecimal.valueOf(70.0);
            
            // 초과분을 예금에 추가
            recommendedDepositWeight = recommendedDepositWeight.add(excessFund);
        }

        // 정규화 (총합이 100%가 되도록)
        BigDecimal totalWeight = recommendedCashWeight.add(recommendedDepositWeight).add(recommendedFundWeight);
        if (totalWeight.compareTo(BigDecimal.valueOf(100.0)) != 0) {
            BigDecimal normalizationFactor = BigDecimal.valueOf(100.0).divide(totalWeight, 4, BigDecimal.ROUND_HALF_UP);
            recommendedCashWeight = recommendedCashWeight.multiply(normalizationFactor);
            recommendedDepositWeight = recommendedDepositWeight.multiply(normalizationFactor);
            recommendedFundWeight = recommendedFundWeight.multiply(normalizationFactor);
        }

        return PortfolioRecommendationResponse.RecommendedPortfolio.builder()
                .cashWeight(recommendedCashWeight.setScale(2, BigDecimal.ROUND_HALF_UP))
                .depositWeight(recommendedDepositWeight.setScale(2, BigDecimal.ROUND_HALF_UP))
                .fundWeight(recommendedFundWeight.setScale(2, BigDecimal.ROUND_HALF_UP))
                .description(String.format("모델 포트폴리오(%.0f%%) + 유사 사용자 포트폴리오(%.0f%%) 결합", 
                    MODEL_WEIGHT.multiply(BigDecimal.valueOf(100)), 
                    SIMILAR_USER_WEIGHT.multiply(BigDecimal.valueOf(100))))
                .basis("COMBINED")
                .modelWeight(MODEL_WEIGHT)
                .similarUserWeight(SIMILAR_USER_WEIGHT)
                .build();
    }

    /**
     * 메타데이터 생성
     */
    private PortfolioRecommendationResponse.RecommendationMetadata createMetadata(
            Customer customer, Integer similarUserCount) {
        
        return PortfolioRecommendationResponse.RecommendationMetadata.builder()
                .generatedAt(LocalDateTime.now())
                .algorithm("Collaborative Filtering + Risk Profile Model")
                .totalUsersAnalyzed(null) // FastAPI에서 제공하지 않음
                .similarUsersFound(similarUserCount)
                .minSimilarityThreshold(0.3)
                .constraints("펀드 비중 70% 상한, 총 비중 100% 정규화")
                .notes(String.format("고객 리스크 프로파일: %s (%.2f점)", 
                    customer.getRiskProfileType() != null ? customer.getRiskProfileType().getDescription() : "미평가",
                    customer.getRiskProfileScore() != null ? customer.getRiskProfileScore().doubleValue() : 0.0))
                .build();
    }

    /**
     * 기본 유사 사용자 포트폴리오 생성
     */
    private PortfolioRecommendationResponse.SimilarUserPortfolio createDefaultSimilarUserPortfolio() {
        return PortfolioRecommendationResponse.SimilarUserPortfolio.builder()
                .cashWeight(BigDecimal.valueOf(5.0))
                .depositWeight(BigDecimal.valueOf(40.0))
                .fundWeight(BigDecimal.valueOf(55.0))
                .similarUserCount(0)
                .averageSimilarity(0.0)
                .description("유사 사용자 데이터 없음 - 기본 포트폴리오 적용")
                .basis("SIMILAR_USERS")
                .build();
    }

    /**
     * 고객의 출생년도 계산 (간단한 추정)
     */
    private Integer calculateBirthYear(Customer customer) {
        // 실제로는 User 테이블의 birth_date를 사용해야 함
        // 여기서는 간단히 추정 (예: 30-50세 가정)
        return 1985; // 기본값
    }

    /**
     * 특정 고객의 모델 포트폴리오만 조회
     */
    public PortfolioRecommendationResponse.ModelPortfolio getModelPortfolio(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + customerId));
        
        return createModelPortfolio(customer);
    }

    /**
     * 특정 고객의 유사 사용자 포트폴리오만 조회
     */
    public PortfolioRecommendationResponse.SimilarUserPortfolio getSimilarUserPortfolio(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + customerId));
        
        return createSimilarUserPortfolio(customer);
    }
}
