package com.hanainplan.domain.fund.controller;

import com.hanainplan.domain.fund.dto.FundNavCrawlResult;
import com.hanainplan.domain.fund.service.FundNavCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/funds/crawler")
@RequiredArgsConstructor
@Slf4j
public class FundCrawlerController {

    private final FundNavCrawlerService fundNavCrawlerService;

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

    private record CrawlSingleResponse(
            String childFundCd,
            BigDecimal nav,
            String message
    ) {}
}