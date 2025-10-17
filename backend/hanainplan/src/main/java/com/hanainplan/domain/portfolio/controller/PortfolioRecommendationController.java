package com.hanainplan.domain.portfolio.controller;

import com.hanainplan.domain.portfolio.dto.PortfolioRecommendationRequest;
import com.hanainplan.domain.portfolio.dto.PortfolioRecommendationResponse;
import com.hanainplan.domain.portfolio.service.PortfolioRecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/irp/portfolio")
@RequiredArgsConstructor
@Slf4j
public class PortfolioRecommendationController {

    private final PortfolioRecommendationService portfolioRecommendationService;

    /**
     * 포트폴리오 추천 생성 (모델 + 유사 사용자 결합)
     */
    @GetMapping("/{customerId}/recommendation")
    public ResponseEntity<PortfolioRecommendationResponse> getPortfolioRecommendation(
            @PathVariable Long customerId) {
        
        log.info("포트폴리오 추천 요청 - 고객 ID: {}", customerId);

        try {
            PortfolioRecommendationResponse response = 
                portfolioRecommendationService.generateRecommendation(customerId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("포트폴리오 추천 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("포트폴리오 추천 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 모델 포트폴리오 조회 (리스크 프로파일 기반)
     */
    @GetMapping("/{customerId}/model-portfolio")
    public ResponseEntity<PortfolioRecommendationResponse.ModelPortfolio> getModelPortfolio(
            @PathVariable Long customerId) {
        
        log.debug("모델 포트폴리오 조회 요청 - 고객 ID: {}", customerId);

        try {
            PortfolioRecommendationResponse.ModelPortfolio modelPortfolio = 
                portfolioRecommendationService.getModelPortfolio(customerId);
            return ResponseEntity.ok(modelPortfolio);
        } catch (IllegalArgumentException e) {
            log.error("모델 포트폴리오 조회 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("모델 포트폴리오 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 유사 사용자 포트폴리오 조회 (FastAPI 기반)
     */
    @GetMapping("/{customerId}/similar-user-portfolio")
    public ResponseEntity<PortfolioRecommendationResponse.SimilarUserPortfolio> getSimilarUserPortfolio(
            @PathVariable Long customerId) {
        
        log.debug("유사 사용자 포트폴리오 조회 요청 - 고객 ID: {}", customerId);

        try {
            PortfolioRecommendationResponse.SimilarUserPortfolio similarUserPortfolio = 
                portfolioRecommendationService.getSimilarUserPortfolio(customerId);
            return ResponseEntity.ok(similarUserPortfolio);
        } catch (IllegalArgumentException e) {
            log.error("유사 사용자 포트폴리오 조회 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("유사 사용자 포트폴리오 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
