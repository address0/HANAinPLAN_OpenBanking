package com.hanainplan.domain.portfolio.client;

import com.hanainplan.domain.portfolio.dto.PortfolioRecommendationRequest;
import com.hanainplan.domain.portfolio.dto.PortfolioRecommendationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class FastApiClient {

    private final RestTemplate restTemplate;

    @Value("${fastapi.base-url:http://localhost:8090}")
    private String fastApiBaseUrl;

    /**
     * FastAPI 서버에서 유사 사용자 기반 포트폴리오 추천 요청
     */
    public PortfolioRecommendationResponse.SimilarUserPortfolio getSimilarUserPortfolio(
            PortfolioRecommendationRequest request) {
        
        log.info("FastAPI 유사 사용자 포트폴리오 추천 요청 - 고객 ID: {}", request.getCustomerId());

        try {
            String url = fastApiBaseUrl + "/api/recommendation/similar-users";
            
            // 요청 데이터 구성
            Map<String, Object> requestData = Map.of(
                "customer_id", request.getCustomerId(),
                "birth_year", request.getBirthYear(),
                "industry_code", request.getIndustryCode(),
                "asset_level", request.getAssetLevel(),
                "risk_profile_score", request.getRiskProfileScore().doubleValue(),
                "has_disease", request.getHasDisease()
            );

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(requestData, headers);

            // FastAPI 호출
            ResponseEntity<Map> response = restTemplate.postForEntity(url, httpEntity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                
                if (Boolean.TRUE.equals(responseBody.get("success"))) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    Map<String, Object> avgPortfolio = (Map<String, Object>) data.get("avg_portfolio");
                    
                    // 유사 사용자 정보 추출
                    Integer similarUserCount = ((java.util.List<?>) data.get("similar_users")).size();
                    Double averageSimilarity = calculateAverageSimilarity((java.util.List<Map<String, Object>>) data.get("similar_users"));
                    
                    return PortfolioRecommendationResponse.SimilarUserPortfolio.builder()
                            .cashWeight(java.math.BigDecimal.valueOf((Double) avgPortfolio.get("cash_weight")))
                            .depositWeight(java.math.BigDecimal.valueOf((Double) avgPortfolio.get("deposit_weight")))
                            .fundWeight(java.math.BigDecimal.valueOf((Double) avgPortfolio.get("fund_weight")))
                            .similarUserCount(similarUserCount)
                            .averageSimilarity(averageSimilarity)
                            .description(String.format("유사 사용자 %d명의 평균 포트폴리오 (평균 유사도: %.2f)", similarUserCount, averageSimilarity))
                            .basis("SIMILAR_USERS")
                            .build();
                } else {
                    log.warn("FastAPI 응답 실패: {}", responseBody.get("message"));
                    return createDefaultSimilarUserPortfolio();
                }
            } else {
                log.error("FastAPI 호출 실패 - 상태코드: {}", response.getStatusCode());
                return createDefaultSimilarUserPortfolio();
            }

        } catch (Exception e) {
            log.error("FastAPI 유사 사용자 포트폴리오 추천 요청 실패", e);
            return createDefaultSimilarUserPortfolio();
        }
    }

    /**
     * 유사 사용자들의 평균 유사도 계산
     */
    private Double calculateAverageSimilarity(java.util.List<Map<String, Object>> similarUsers) {
        if (similarUsers == null || similarUsers.isEmpty()) {
            return 0.0;
        }
        
        return similarUsers.stream()
                .mapToDouble(user -> (Double) user.get("similarity"))
                .average()
                .orElse(0.0);
    }

    /**
     * 기본 유사 사용자 포트폴리오 생성 (FastAPI 호출 실패 시)
     */
    private PortfolioRecommendationResponse.SimilarUserPortfolio createDefaultSimilarUserPortfolio() {
        return PortfolioRecommendationResponse.SimilarUserPortfolio.builder()
                .cashWeight(java.math.BigDecimal.valueOf(5.0))
                .depositWeight(java.math.BigDecimal.valueOf(40.0))
                .fundWeight(java.math.BigDecimal.valueOf(55.0))
                .similarUserCount(0)
                .averageSimilarity(0.0)
                .description("유사 사용자 데이터 없음 - 기본 포트폴리오 적용")
                .basis("SIMILAR_USERS")
                .build();
    }

    /**
     * FastAPI 서버 상태 확인
     */
    public boolean isFastApiAvailable() {
        try {
            String url = fastApiBaseUrl + "/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.warn("FastAPI 서버 상태 확인 실패: {}", e.getMessage());
            return false;
        }
    }
}
