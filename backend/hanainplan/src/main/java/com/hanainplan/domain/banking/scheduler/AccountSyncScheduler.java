package com.hanainplan.domain.banking.scheduler;

import com.hanainplan.domain.banking.service.AccountSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountSyncScheduler {

    private final AccountSyncService accountSyncService;

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

}