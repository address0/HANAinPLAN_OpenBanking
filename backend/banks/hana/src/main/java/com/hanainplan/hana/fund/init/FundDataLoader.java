package com.hanainplan.hana.fund.init;

import com.hanainplan.hana.fund.entity.*;
import com.hanainplan.hana.fund.repository.FundClassRepository;
import com.hanainplan.hana.fund.repository.FundMasterRepository;
import com.hanainplan.hana.fund.repository.FundNavRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 실제 하나은행 펀드 상품 데이터 초기화
 * - 미래에셋 퇴직플랜 2종
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Profile({"dev", "local"}) // 개발/로컬 환경에서만 실행
public class FundDataLoader implements CommandLineRunner {

    private final FundMasterRepository fundMasterRepository;
    private final FundClassRepository fundClassRepository;
    private final FundNavRepository fundNavRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("===== 실제 펀드 상품 데이터 초기화 시작 =====");

        // 이미 데이터가 있으면 스킵
        if (fundMasterRepository.count() > 0) {
            log.info("펀드 데이터가 이미 존재합니다. 초기화를 스킵합니다.");
            return;
        }

        // 1. 미래에셋 퇴직플랜 20 (채권혼합)
        createMiraeAssetPlan20();

        // 2. 미래에셋 퇴직연금 고배당포커스 40 (채권혼합)
        createMiraeAssetDividend40();

        log.info("===== 실제 펀드 상품 데이터 초기화 완료 =====");
    }

    /**
     * 미래에셋 퇴직플랜 20 펀드 생성
     */
    private void createMiraeAssetPlan20() {
        log.info("미래에셋 퇴직플랜 20 생성 중...");

        // 모펀드 생성
        FundMaster master = FundMaster.builder()
                .fundCd("513061")
                .fundName("미래에셋퇴직플랜20증권자투자신탁1호(채권혼합)")
                .fundGb(2) // 사모
                .assetType("채권혼합")
                .riskGrade(null)
                .currency("KRW")
                .isActive(true)
                .build();

        fundMasterRepository.save(master);

        // 클래스 P 생성
        FundClass classP = FundClass.builder()
                .childFundCd("51306P")
                .fundMaster(master)
                .classCode("P")
                .loadType("UNKNOWN")
                .taxCategory(null)
                .saleStatus("ON")
                .build();

        // 거래 규칙
        FundRules rules = FundRules.builder()
                .childFundCd("51306P")
                .fundClass(classP)
                .cutoffTime(LocalTime.of(15, 30))
                .navPublishTime(LocalTime.of(10, 0))
                .buySettleDays(2)
                .redeemSettleDays(3)
                .unitType("KRW")
                .minInitialAmount(new BigDecimal("10000.00"))
                .minAdditional(new BigDecimal("1000.00"))
                .incrementAmount(new BigDecimal("1000.00"))
                .allowSip(true)
                .allowSwitch(true)
                .redemptionFeeRate(null)
                .redemptionFeeDays(null)
                .build();

        // 수수료
        FundFees fees = FundFees.builder()
                .childFundCd("51306P")
                .fundClass(classP)
                .mgmtFeeBps(45)    // 0.45%
                .salesFeeBps(25)   // 0.25%
                .trusteeFeeBps(3)  // 0.03%
                .adminFeeBps(2)    // 0.02%
                .frontLoadPct(null)
                .totalFeeBps(75)   // 0.75%
                .build();

        classP.setFundRules(rules);
        classP.setFundFees(fees);

        fundClassRepository.save(classP);

        // 기준가 데이터 (최근 2일)
        LocalDate today = LocalDate.now();
        
        FundNav nav1 = FundNav.builder()
                .childFundCd("51306P")
                .navDate(today.minusDays(2))
                .nav(new BigDecimal("1012.3456"))
                .publishedAt(today.minusDays(1).atTime(10, 5))
                .build();

        FundNav nav2 = FundNav.builder()
                .childFundCd("51306P")
                .navDate(today.minusDays(1))
                .nav(new BigDecimal("1013.2100"))
                .publishedAt(today.atTime(10, 5))
                .build();

        fundNavRepository.save(nav1);
        fundNavRepository.save(nav2);

        log.info("미래에셋 퇴직플랜 20 생성 완료 - 클래스 P");
    }

    /**
     * 미래에셋 퇴직연금 고배당포커스 40 펀드 생성
     */
    private void createMiraeAssetDividend40() {
        log.info("미래에셋 퇴직연금 고배당포커스 40 생성 중...");

        // 모펀드 생성
        FundMaster master = FundMaster.builder()
                .fundCd("308100")
                .fundName("미래에셋퇴직연금고배당포커스40증권자투자신탁1호(채권혼합)")
                .fundGb(2) // 사모
                .assetType("채권혼합")
                .riskGrade(null)
                .currency("KRW")
                .isActive(true)
                .build();

        fundMasterRepository.save(master);

        // 클래스 C 생성
        FundClass classC = FundClass.builder()
                .childFundCd("30810C")
                .fundMaster(master)
                .classCode("C")
                .loadType("UNKNOWN")
                .taxCategory(null)
                .saleStatus("ON")
                .build();

        // 거래 규칙
        FundRules rules = FundRules.builder()
                .childFundCd("30810C")
                .fundClass(classC)
                .cutoffTime(LocalTime.of(15, 30))
                .navPublishTime(LocalTime.of(10, 0))
                .buySettleDays(2)
                .redeemSettleDays(3)
                .unitType("KRW")
                .minInitialAmount(new BigDecimal("10000.00"))
                .minAdditional(new BigDecimal("1000.00"))
                .incrementAmount(new BigDecimal("1000.00"))
                .allowSip(true)
                .allowSwitch(true)
                .redemptionFeeRate(new BigDecimal("0.0070")) // 0.7%
                .redemptionFeeDays(90)
                .build();

        // 수수료
        FundFees fees = FundFees.builder()
                .childFundCd("30810C")
                .fundClass(classC)
                .mgmtFeeBps(55)    // 0.55%
                .salesFeeBps(35)   // 0.35%
                .trusteeFeeBps(3)  // 0.03%
                .adminFeeBps(2)    // 0.02%
                .frontLoadPct(null)
                .totalFeeBps(95)   // 0.95%
                .build();

        classC.setFundRules(rules);
        classC.setFundFees(fees);

        fundClassRepository.save(classC);

        // 기준가 데이터 (최근 2일)
        LocalDate today = LocalDate.now();
        
        FundNav nav1 = FundNav.builder()
                .childFundCd("30810C")
                .navDate(today.minusDays(2))
                .nav(new BigDecimal("987.6543"))
                .publishedAt(today.minusDays(1).atTime(10, 6))
                .build();

        FundNav nav2 = FundNav.builder()
                .childFundCd("30810C")
                .navDate(today.minusDays(1))
                .nav(new BigDecimal("990.1200"))
                .publishedAt(today.atTime(10, 6))
                .build();

        fundNavRepository.save(nav1);
        fundNavRepository.save(nav2);

        log.info("미래에셋 퇴직연금 고배당포커스 40 생성 완료 - 클래스 C");
    }
}

