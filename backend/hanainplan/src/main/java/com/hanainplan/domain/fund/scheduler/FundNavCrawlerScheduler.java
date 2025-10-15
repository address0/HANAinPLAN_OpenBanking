package com.hanainplan.domain.fund.scheduler;

import com.hanainplan.domain.fund.service.FundNavCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 펀드 기준가 크롤링 스케줄러
 * 매일 오후 6시에 모든 펀드의 기준가를 크롤링하여 업데이트
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FundNavCrawlerScheduler {

    private final FundNavCrawlerService fundNavCrawlerService;

    /**
     * 매일 오후 6시에 펀드 기준가 크롤링
     * - 장 마감 후 최신 기준가 수집
     * - cron 표현식: "초 분 시 일 월 요일"
     */
    @Scheduled(cron = "0 0 18 * * ?")
    public void scheduleFundNavCrawl() {
        log.info("====================================================");
        log.info("펀드 기준가 크롤링 스케줄러 실행 시작 (매일 오후 6시)");
        log.info("====================================================");

        try {
            fundNavCrawlerService.crawlAndUpdateAllFundNav();
            log.info("펀드 기준가 크롤링 스케줄러 실행 완료");
        } catch (Exception e) {
            log.error("펀드 기준가 크롤링 스케줄러 실행 중 오류 발생", e);
        }

        log.info("====================================================");
    }
}

