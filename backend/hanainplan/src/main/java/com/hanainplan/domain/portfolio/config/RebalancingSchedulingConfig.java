package com.hanainplan.domain.portfolio.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@Slf4j
public class RebalancingSchedulingConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(5); // 스레드 풀 크기 설정
        taskScheduler.setThreadNamePrefix("rebalancing-scheduler-");
        taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        taskScheduler.setAwaitTerminationSeconds(60);
        taskScheduler.initialize();
        
        taskRegistrar.setScheduler(taskScheduler);
        
        log.info("리밸런싱 스케줄러 설정 완료 - 스레드 풀 크기: {}", taskScheduler.getPoolSize());
    }
}
