package com.hanainplan.domain.fund.controller;

import com.hanainplan.domain.fund.dto.FundNavCrawlResult;
import com.hanainplan.domain.fund.service.FundNavCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 펀드 크롤링 컨트롤러
 * 수동으로 펀드 기준가 크롤링을 실행하기 위한 API
 */
@RestController
@RequestMapping("/api/funds/crawler")
@RequiredArgsConstructor
@Slf4j
public class FundCrawlerController {

    private final FundNavCrawlerService fundNavCrawlerService;

    /**
     * 모든 펀드 기준가 크롤링 실행
     *
     * POST /api/funds/crawler/crawl-all
     *
     * @return 크롤링 결과
     */
    @PostMapping("/crawl-all")
    public ResponseEntity<FundNavCrawlResult> crawlAllFundNav() {
        log.info("펀드 기준가 전체 크롤링 수동 실행 요청");

        try {
            FundNavCrawlResult result = fundNavCrawlerService.crawlAndUpdateAllFundNav();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("펀드 기준가 크롤링 실행 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 펀드 기준가 크롤링 실행
     *
     * POST /api/funds/crawler/crawl/{childFundCd}
     *
     * @param childFundCd 자펀드 코드
     * @return 크롤링된 기준가
     */
    @PostMapping("/crawl/{childFundCd}")
    public ResponseEntity<?> crawlSingleFundNav(@PathVariable String childFundCd) {
        log.info("펀드 기준가 단일 크롤링 수동 실행 요청: {}", childFundCd);

        try {
            BigDecimal nav = fundNavCrawlerService.crawlSingleFundNav(childFundCd);
            return ResponseEntity.ok().body(
                    new CrawlSingleResponse(childFundCd, nav, "크롤링 성공")
            );
        } catch (IllegalArgumentException e) {
            log.warn("펀드를 찾을 수 없음: {}", childFundCd);
            return ResponseEntity.badRequest().body(
                    new CrawlSingleResponse(childFundCd, null, e.getMessage())
            );
        } catch (Exception e) {
            log.error("펀드 기준가 크롤링 실행 중 오류 발생: {}", childFundCd, e);
            return ResponseEntity.internalServerError().body(
                    new CrawlSingleResponse(childFundCd, null, "크롤링 실패: " + e.getMessage())
            );
        }
    }

    /**
     * 단일 크롤링 응답
     */
    private record CrawlSingleResponse(
            String childFundCd,
            BigDecimal nav,
            String message
    ) {}
}

