package com.hanainplan.domain.fund.scheduler;

import com.hanainplan.domain.fund.service.FundPortfolioSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 펀드 포트폴리오 동기화 스케줄러
 * - 매일 오후 7시에 모든 사용자의 펀드 포트폴리오 및 거래 내역 동기화
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FundPortfolioSyncScheduler {

    private final FundPortfolioSyncService fundPortfolioSyncService;

    /**
     * 매일 오후 7시에 펀드 포트폴리오 동기화
     * - 펀드 상품 동기화(오후 6시) 및 계좌 동기화(오후 6시 30분) 이후 실행
     * - cron 표현식: "초 분 시 일 월 요일"
     */
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

    /**
     * 애플리케이션 시작 시 초기 동기화 (선택적)
     * - 개발 환경에서 테스트 시 유용
     */
    @Scheduled(initialDelay = 20000) // 20초 후 최초 1회 실행
    public void initialPortfolioSync() {
        log.info("====================================================");
        log.info("펀드 포트폴리오 초기 동기화 시작");
        log.info("====================================================");

        try {
            fundPortfolioSyncService.syncAllUserPortfolios();
            log.info("펀드 포트폴리오 초기 동기화 완료");
        } catch (Exception e) {
            log.error("펀드 포트폴리오 초기 동기화 중 오류 발생", e);
        }

        log.info("====================================================");
    }
}

