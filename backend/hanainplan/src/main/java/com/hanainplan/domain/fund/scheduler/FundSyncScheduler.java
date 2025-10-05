package com.hanainplan.domain.fund.scheduler;

import com.hanainplan.domain.fund.service.FundSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 펀드 상품 동기화 스케줄러
 * 하루 한 번 하나은행에서 펀드 상품 정보를 동기화
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FundSyncScheduler {

    private final FundSyncService fundSyncService;

    /**
     * 매일 오후 6시에 펀드 상품 동기화
     * - 장 마감 후 데이터 업데이트
     * - cron 표현식: "초 분 시 일 월 요일"
     */
    @Scheduled(cron = "0 0 18 * * ?")
    public void scheduleFundProductSync() {
        log.info("====================================================");
        log.info("펀드 상품 동기화 스케줄러 실행 시작 (매일 오후 6시)");
        log.info("====================================================");
        
        try {
            fundSyncService.syncFundProducts();
            log.info("펀드 상품 동기화 스케줄러 실행 완료");
        } catch (Exception e) {
            log.error("펀드 상품 동기화 스케줄러 실행 중 오류 발생", e);
        }
        
        log.info("====================================================");
    }

    /**
     * 애플리케이션 시작 시 초기 동기화 (선택적)
     * - 개발 환경에서 테스트 시 유용
     * - 운영 환경에서는 주석 처리하거나 프로파일로 제어
     */
    @Scheduled(initialDelay = 10000) // 10초 후 최초 1회 실행
    public void initialSync() {
        log.info("====================================================");
        log.info("펀드 상품 초기 동기화 시작");
        log.info("====================================================");
        
        try {
            fundSyncService.syncFundProducts();
            log.info("펀드 상품 초기 동기화 완료");
        } catch (Exception e) {
            log.error("펀드 상품 초기 동기화 중 오류 발생", e);
        }
        
        log.info("====================================================");
    }
}

