package com.hanainplan.hana.fund.batch;

import com.hanainplan.hana.fund.entity.FundClass;
import com.hanainplan.hana.fund.entity.FundNav;
import com.hanainplan.hana.fund.entity.FundSubscription;
import com.hanainplan.hana.fund.repository.FundClassRepository;
import com.hanainplan.hana.fund.repository.FundNavRepository;
import com.hanainplan.hana.fund.repository.FundSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

/**
 * 펀드 기준가 업데이트 배치 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FundNavBatchService {

    private final FundNavRepository fundNavRepository;
    private final FundClassRepository fundClassRepository;
    private final FundSubscriptionRepository fundSubscriptionRepository;
    private final Random random = new Random();

    /**
     * 일일 기준가 업데이트 배치
     * 매일 오후 6시 실행 (실제 펀드는 오후 5~6시경 기준가 발표)
     */
    @Scheduled(cron = "0 0 18 * * *")  // 매일 18:00
    @Transactional
    public void updateDailyNav() {
        log.info("========== 일일 기준가 업데이트 배치 시작 ==========");
        
        LocalDate today = LocalDate.now();
        
        try {
            // 모든 판매중인 펀드 클래스 조회
            List<FundClass> fundClasses = fundClassRepository.findAll().stream()
                    .filter(fc -> "ON".equals(fc.getSaleStatus()))
                    .toList();
            
            log.info("업데이트 대상 펀드 클래스: {}개", fundClasses.size());
            
            int successCount = 0;
            int skipCount = 0;
            
            for (FundClass fundClass : fundClasses) {
                try {
                    // 당일 기준가가 이미 있는지 확인
                    boolean exists = fundNavRepository.existsByChildFundCdAndNavDate(
                            fundClass.getChildFundCd(), today);
                    
                    if (exists) {
                        log.debug("이미 당일 기준가 존재 - {}", fundClass.getChildFundCd());
                        skipCount++;
                        continue;
                    }
                    
                    // 최신 기준가 조회
                    FundNav latestNav = fundNavRepository.findLatestByChildFundCd(fundClass.getChildFundCd())
                            .orElse(null);
                    
                    BigDecimal newNav;
                    if (latestNav != null) {
                        // 전일 기준가 기반으로 랜덤 변동 (-2% ~ +2%)
                        newNav = calculateNewNav(latestNav.getNav());
                    } else {
                        // 최초 기준가 (1,000원)
                        newNav = BigDecimal.valueOf(1000.0000);
                    }
                    
                    // 새 기준가 저장
                    FundNav fundNav = FundNav.builder()
                            .childFundCd(fundClass.getChildFundCd())
                            .navDate(today)
                            .nav(newNav)
                            .build();
                    
                    fundNavRepository.save(fundNav);
                    successCount++;
                    
                    log.debug("기준가 업데이트 완료 - {} : {}", 
                            fundClass.getChildFundCd(), newNav);
                    
                } catch (Exception e) {
                    log.error("기준가 업데이트 실패 - {}: {}", 
                            fundClass.getChildFundCd(), e.getMessage());
                }
            }
            
            log.info("일일 기준가 업데이트 완료 - 성공: {}개, 스킵: {}개", successCount, skipCount);
            
            // 기준가 업데이트 후 평가금액 재계산
            updateSubscriptionValuations(today);
            
        } catch (Exception e) {
            log.error("일일 기준가 업데이트 배치 실패", e);
        }
        
        log.info("========== 일일 기준가 업데이트 배치 종료 ==========");
    }

    /**
     * 새로운 기준가 계산 (전일 대비 -2% ~ +2% 랜덤 변동)
     */
    private BigDecimal calculateNewNav(BigDecimal previousNav) {
        // -2.0% ~ +2.0% 사이의 랜덤 변동률
        double changeRate = (random.nextDouble() * 4.0 - 2.0) / 100.0;
        
        BigDecimal change = previousNav.multiply(BigDecimal.valueOf(changeRate));
        BigDecimal newNav = previousNav.add(change);
        
        // 소수점 4자리로 반올림
        return newNav.setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * 모든 활성 펀드 가입의 평가금액 업데이트
     */
    @Transactional
    public void updateSubscriptionValuations(LocalDate navDate) {
        log.info("========== 펀드 평가금액 재계산 시작 ==========");
        
        try {
            // 모든 활성 펀드 가입 조회
            List<FundSubscription> activeSubscriptions = fundSubscriptionRepository
                    .findByStatusIn(List.of("ACTIVE", "PARTIAL_SOLD"));
            
            log.info("평가금액 업데이트 대상: {}건", activeSubscriptions.size());
            
            int successCount = 0;
            int failCount = 0;
            
            for (FundSubscription subscription : activeSubscriptions) {
                try {
                    // 최신 기준가 조회
                    FundNav nav = fundNavRepository.findByChildFundCdAndNavDate(
                            subscription.getChildFundCd(), navDate)
                            .or(() -> fundNavRepository.findLatestByChildFundCd(
                                    subscription.getChildFundCd()))
                            .orElse(null);
                    
                    if (nav == null) {
                        log.warn("기준가 없음 - subscriptionId: {}, fundCd: {}", 
                                subscription.getSubscriptionId(), subscription.getChildFundCd());
                        failCount++;
                        continue;
                    }
                    
                    // 평가금액 업데이트
                    subscription.updateValuation(nav.getNav());
                    fundSubscriptionRepository.save(subscription);
                    
                    successCount++;
                    
                } catch (Exception e) {
                    log.error("평가금액 업데이트 실패 - subscriptionId: {}", 
                            subscription.getSubscriptionId(), e);
                    failCount++;
                }
            }
            
            log.info("펀드 평가금액 재계산 완료 - 성공: {}건, 실패: {}건", successCount, failCount);
            
        } catch (Exception e) {
            log.error("펀드 평가금액 재계산 실패", e);
        }
        
        log.info("========== 펀드 평가금액 재계산 종료 ==========");
    }

    /**
     * 수동 기준가 업데이트 (테스트용)
     */
    @Transactional
    public void manualUpdateNav() {
        log.info("수동 기준가 업데이트 실행");
        updateDailyNav();
    }
}

