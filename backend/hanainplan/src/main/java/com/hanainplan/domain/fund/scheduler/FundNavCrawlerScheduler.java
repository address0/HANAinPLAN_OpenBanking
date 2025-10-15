package com.hanainplan.domain.fund.scheduler;

import com.hanainplan.domain.fund.service.FundNavCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FundNavCrawlerScheduler {

    private final FundNavCrawlerService fundNavCrawlerService;

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