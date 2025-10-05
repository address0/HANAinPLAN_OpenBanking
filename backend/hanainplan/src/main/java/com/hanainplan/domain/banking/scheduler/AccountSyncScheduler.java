package com.hanainplan.domain.banking.scheduler;

import com.hanainplan.domain.banking.service.AccountSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 계좌 정보 동기화 스케줄러
 * - 매일 오후 6시 30분에 모든 사용자의 계좌 잔액 정보를 은행에서 동기화
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AccountSyncScheduler {

    private final AccountSyncService accountSyncService;

    /**
     * 매일 오후 6시 30분에 계좌 동기화
     * - 펀드 데이터 동기화 후 30분 뒤에 실행
     * - cron 표현식: "초 분 시 일 월 요일"
     */
    @Scheduled(cron = "0 30 18 * * ?")
    public void scheduleAccountSync() {
        log.info("====================================================");
        log.info("계좌 동기화 스케줄러 실행 시작 (매일 오후 6시 30분)");
        log.info("====================================================");
        
        try {
            int syncedCount = accountSyncService.syncAllUserAccounts();
            log.info("계좌 동기화 완료 - {}명의 사용자 계좌 동기화됨", syncedCount);
        } catch (Exception e) {
            log.error("계좌 동기화 스케줄러 실행 중 오류 발생", e);
        }
        
        log.info("====================================================");
    }

    /**
     * 애플리케이션 시작 시 초기 동기화 (선택적)
     * - 개발 환경에서 테스트 시 유용
     */
    @Scheduled(initialDelay = 15000) // 15초 후 최초 1회 실행
    public void initialAccountSync() {
        log.info("====================================================");
        log.info("계좌 초기 동기화 시작");
        log.info("====================================================");

        try {
            int syncedCount = accountSyncService.syncAllUserAccounts();
            log.info("계좌 초기 동기화 완료 - {}명의 사용자 계좌 동기화됨", syncedCount);
        } catch (Exception e) {
            log.error("계좌 초기 동기화 중 오류 발생", e);
        }

        log.info("====================================================");
    }
}

