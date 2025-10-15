package com.hanainplan.domain.fund.scheduler;

import com.hanainplan.domain.fund.service.FundPortfolioSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FundPortfolioSyncScheduler {

    private final FundPortfolioSyncService fundPortfolioSyncService;

    @Scheduled(cron = "0 0 19 * * ?")
    public void scheduleFundPortfolioSync() {
        log.info("====================================================");
        log.info("펀드 포트폴리오 동기화 스케줄러 실행 시작 (매일 오후 7시)");
        log.info("====================================================");

        try {
            fundPortfolioSyncService.syncAllUserPortfolios();
            log.info("펀드 포트폴리오 동기화 스케줄러 실행 완료");
        } catch (Exception e) {
            log.error("펀드 포트폴리오 동기화 스케줄러 실행 중 오류 발생", e);
        }

        log.info("====================================================");
    }

}