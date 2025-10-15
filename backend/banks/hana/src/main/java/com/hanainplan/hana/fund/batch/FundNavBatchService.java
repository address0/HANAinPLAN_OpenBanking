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

@Service
@RequiredArgsConstructor
@Slf4j
public class FundNavBatchService {

    private final FundNavRepository fundNavRepository;
    private final FundClassRepository fundClassRepository;
    private final FundSubscriptionRepository fundSubscriptionRepository;
    private final Random random = new Random();

    @Scheduled(cron = "0 0 18 * * *")
    @Transactional
    public void updateDailyNav() {
        log.info("========== 일일 기준가 업데이트 배치 시작 ==========");

        LocalDate today = LocalDate.now();

        try {
            List<FundClass> fundClasses = fundClassRepository.findAll().stream()
                    .filter(fc -> "ON".equals(fc.getSaleStatus()))
                    .toList();

            log.info("업데이트 대상 펀드 클래스: {}개", fundClasses.size());

            int successCount = 0;
            int skipCount = 0;

            for (FundClass fundClass : fundClasses) {
                try {
                    boolean exists = fundNavRepository.existsByChildFundCdAndNavDate(
                            fundClass.getChildFundCd(), today);

                    if (exists) {
                        log.debug("이미 당일 기준가 존재 - {}", fundClass.getChildFundCd());
                        skipCount++;
                        continue;
                    }

                    FundNav latestNav = fundNavRepository.findLatestByChildFundCd(fundClass.getChildFundCd())
                            .orElse(null);

                    BigDecimal newNav;
                    if (latestNav != null) {
                        newNav = calculateNewNav(latestNav.getNav());
                    } else {
                        newNav = BigDecimal.valueOf(1000.0000);
                    }

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

            updateSubscriptionValuations(today);

        } catch (Exception e) {
            log.error("일일 기준가 업데이트 배치 실패", e);
        }

        log.info("========== 일일 기준가 업데이트 배치 종료 ==========");
    }

    private BigDecimal calculateNewNav(BigDecimal previousNav) {
        double changeRate = (random.nextDouble() * 4.0 - 2.0) / 100.0;

        BigDecimal change = previousNav.multiply(BigDecimal.valueOf(changeRate));
        BigDecimal newNav = previousNav.add(change);

        return newNav.setScale(4, RoundingMode.HALF_UP);
    }

    @Transactional
    public void updateSubscriptionValuations(LocalDate navDate) {
        log.info("========== 펀드 평가금액 재계산 시작 ==========");

        try {
            List<FundSubscription> activeSubscriptions = fundSubscriptionRepository
                    .findByStatusIn(List.of("ACTIVE", "PARTIAL_SOLD"));

            log.info("평가금액 업데이트 대상: {}건", activeSubscriptions.size());

            int successCount = 0;
            int failCount = 0;

            for (FundSubscription subscription : activeSubscriptions) {
                try {
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

    @Transactional
    public void manualUpdateNav() {
        log.info("수동 기준가 업데이트 실행");
        updateDailyNav();
    }
}