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

        // 1. 미래에셋 퇴직플랜 20 (채권혼합)
        createFundIfNotExists("513061", this::createMiraeAssetPlan20);

        // 2. 미래에셋 퇴직연금 고배당포커스 40 (채권혼합)
        createFundIfNotExists("308100", this::createMiraeAssetDividend40);

        // 3. 미래에셋퇴직플랜KRX100인덱스안정형40 (채권혼합)
        createFundIfNotExists("480214", this::createMiraeAssetKRX100);

        // 4. 미래에셋퇴직플랜40 (채권혼합)
        createFundIfNotExists("513041", this::createMiraeAssetPlan40);

        // 5. 미래에셋퇴직플랜 (채권혼합)
        createFundIfNotExists("513031", this::createMiraeAssetPlan);

        // 6. 미래에셋퇴직플랜30 (채권혼합)
        createFundIfNotExists("513051", this::createMiraeAssetPlan30);

        // 7. 미래에셋퇴직플랜목돈분할투자3/10 (채권혼합)
        createFundIfNotExists("513920", this::createMiraeAssetLumpSum);

        log.info("===== 실제 펀드 상품 데이터 초기화 완료 =====");
    }

    /**
     * 펀드 마스터와 클래스 확인 후 생성
     * - FundMaster가 있으면 조회해서 재사용
     * - 각 클래스가 있는지 확인하고 없으면 생성
     */
    private void createFundIfNotExists(String fundCd, Runnable creator) {
        log.info("펀드 {} 확인 및 생성", fundCd);
        // 항상 creator를 실행하되, creator 내부에서 FundMaster와 FundClass를 각각 체크
        creator.run();
    }

    /**
     * 미래에셋 퇴직플랜 20 펀드 생성
     */
    private void createMiraeAssetPlan20() {
        log.info("미래에셋 퇴직플랜 20 생성 중...");

        // 모펀드 조회 또는 생성
        FundMaster master = fundMasterRepository.findById("513061")
                .orElseGet(() -> {
                    log.info("FundMaster 513061 생성");
                    FundMaster newMaster = FundMaster.builder()
                            .fundCd("513061")
                            .fundName("미래에셋퇴직플랜20증권자투자신탁1호(채권혼합)")
                            .fundGb(2) // 사모
                            .assetType("채권혼합")
                            .riskGrade(null)
                            .currency("KRW")
                            .isActive(true)
                            .build();
                    return fundMasterRepository.save(newMaster);
                });

        // 클래스 P가 이미 있는지 확인
        if (fundClassRepository.existsById("51306P")) {
            log.info("클래스 51306P는 이미 존재합니다. 스킵합니다.");
            return;
        }

        log.info("클래스 51306P 생성");

        // 클래스 P 생성
        FundClass classP = FundClass.builder()
                .childFundCd("51306P")
                .fundMaster(master)
                .classCode("P")
                .loadType("UNKNOWN")
                .taxCategory(null)
                .saleStatus("ON")
                .sourceUrl("https://investments.miraeasset.com/magi/fund/view.do?fundGb=2&fundCd=513061&childFundCd=51306P&childFundGb=2")
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

        // 모펀드 조회 또는 생성
        FundMaster master = fundMasterRepository.findById("308100")
                .orElseGet(() -> {
                    log.info("FundMaster 308100 생성");
                    FundMaster newMaster = FundMaster.builder()
                            .fundCd("308100")
                            .fundName("미래에셋퇴직연금고배당포커스40증권자투자신탁1호(채권혼합)")
                            .fundGb(2) // 사모
                            .assetType("채권혼합")
                            .riskGrade(null)
                            .currency("KRW")
                            .isActive(true)
                            .build();
                    return fundMasterRepository.save(newMaster);
                });

        // 클래스 C가 이미 있는지 확인
        if (fundClassRepository.existsById("30810C")) {
            log.info("클래스 30810C는 이미 존재합니다. 스킵합니다.");
            return;
        }

        log.info("클래스 30810C 생성");

        // 클래스 C 생성
        FundClass classC = FundClass.builder()
                .childFundCd("30810C")
                .fundMaster(master)
                .classCode("C")
                .loadType("UNKNOWN")
                .taxCategory(null)
                .saleStatus("ON")
                .sourceUrl("https://investments.miraeasset.com/magi/fund/view.do?fundGb=2&fundCd=308100&childFundCd=30810C&childFundGb=2")
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

    /**
     * 미래에셋퇴직플랜KRX100인덱스안정형40 펀드 생성
     */
    private void createMiraeAssetKRX100() {
        log.info("미래에셋퇴직플랜KRX100인덱스안정형40 생성 중...");

        FundMaster master = fundMasterRepository.findById("480214")
                .orElseGet(() -> {
                    log.info("FundMaster 480214 생성");
                    return fundMasterRepository.save(FundMaster.builder()
                            .fundCd("480214")
                            .fundName("미래에셋퇴직플랜KRX100인덱스안정형40증권자투자신탁1호(채권혼합)")
                            .fundGb(2)
                            .assetType("채권혼합")
                            .riskGrade(null)
                            .currency("KRW")
                            .isActive(true)
                            .build());
                });

        if (fundClassRepository.existsById("48021E")) {
            log.info("클래스 48021E는 이미 존재합니다. 스킵합니다.");
            return;
        }

        log.info("클래스 48021E 생성");

        FundClass classE = FundClass.builder()
                .childFundCd("48021E")
                .fundMaster(master)
                .classCode("E")
                .loadType("UNKNOWN")
                .taxCategory(null)
                .saleStatus("ON")
                .sourceUrl("https://investments.miraeasset.com/magi/fund/view.do?fundGb=2&fundCd=480214&childFundCd=48021E&childFundGb=2")
                .build();

        FundRules rules = FundRules.builder()
                .childFundCd("48021E")
                .fundClass(classE)
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

        FundFees fees = FundFees.builder()
                .childFundCd("48021E")
                .fundClass(classE)
                .mgmtFeeBps(45)
                .salesFeeBps(25)
                .trusteeFeeBps(3)
                .adminFeeBps(2)
                .frontLoadPct(null)
                .totalFeeBps(75)
                .build();

        classE.setFundRules(rules);
        classE.setFundFees(fees);
        fundClassRepository.save(classE);

        LocalDate today = LocalDate.now();
        fundNavRepository.save(FundNav.builder()
                .childFundCd("48021E")
                .navDate(today.minusDays(2))
                .nav(new BigDecimal("1012.3456"))
                .publishedAt(today.minusDays(1).atTime(10, 5))
                .build());
        fundNavRepository.save(FundNav.builder()
                .childFundCd("48021E")
                .navDate(today.minusDays(1))
                .nav(new BigDecimal("1013.2100"))
                .publishedAt(today.atTime(10, 5))
                .build());

        log.info("미래에셋퇴직플랜KRX100인덱스안정형40 생성 완료 - 클래스 E");
    }

    /**
     * 미래에셋퇴직플랜40 펀드 생성
     */
    private void createMiraeAssetPlan40() {
        log.info("미래에셋퇴직플랜40 생성 중...");

        FundMaster master = fundMasterRepository.findById("513041")
                .orElseGet(() -> {
                    log.info("FundMaster 513041 생성");
                    return fundMasterRepository.save(FundMaster.builder()
                            .fundCd("513041")
                            .fundName("미래에셋퇴직플랜40증권자투자신탁1호(채권혼합)")
                            .fundGb(2)
                            .assetType("채권혼합")
                            .riskGrade(null)
                            .currency("KRW")
                            .isActive(true)
                            .build());
                });

        if (fundClassRepository.existsById("51304P")) {
            log.info("클래스 51304P는 이미 존재합니다. 스킵합니다.");
            return;
        }

        log.info("클래스 51304P 생성");

        FundClass classP = FundClass.builder()
                .childFundCd("51304P")
                .fundMaster(master)
                .classCode("P")
                .loadType("UNKNOWN")
                .taxCategory(null)
                .saleStatus("ON")
                .sourceUrl("https://investments.miraeasset.com/magi/fund/view.do?fundGb=2&fundCd=513041&childFundCd=51304P&childFundGb=2")
                .build();

        FundRules rules = FundRules.builder()
                .childFundCd("51304P")
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

        FundFees fees = FundFees.builder()
                .childFundCd("51304P")
                .fundClass(classP)
                .mgmtFeeBps(50)
                .salesFeeBps(30)
                .trusteeFeeBps(3)
                .adminFeeBps(2)
                .frontLoadPct(null)
                .totalFeeBps(85)
                .build();

        classP.setFundRules(rules);
        classP.setFundFees(fees);
        fundClassRepository.save(classP);

        LocalDate today = LocalDate.now();
        fundNavRepository.save(FundNav.builder()
                .childFundCd("51304P")
                .navDate(today.minusDays(2))
                .nav(new BigDecimal("1008.1200"))
                .publishedAt(today.minusDays(1).atTime(10, 5))
                .build());
        fundNavRepository.save(FundNav.builder()
                .childFundCd("51304P")
                .navDate(today.minusDays(1))
                .nav(new BigDecimal("1010.3300"))
                .publishedAt(today.atTime(10, 5))
                .build());

        log.info("미래에셋퇴직플랜40 생성 완료 - 클래스 P");
    }

    /**
     * 미래에셋퇴직플랜 펀드 생성
     */
    private void createMiraeAssetPlan() {
        log.info("미래에셋퇴직플랜 생성 중...");

        FundMaster master = fundMasterRepository.findById("513031")
                .orElseGet(() -> {
                    log.info("FundMaster 513031 생성");
                    return fundMasterRepository.save(FundMaster.builder()
                            .fundCd("513031")
                            .fundName("미래에셋퇴직플랜증권자투자신탁1호(채권혼합)")
                            .fundGb(2)
                            .assetType("채권혼합")
                            .riskGrade(null)
                            .currency("KRW")
                            .isActive(true)
                            .build());
                });

        if (fundClassRepository.existsById("51303C")) {
            log.info("클래스 51303C는 이미 존재합니다. 스킵합니다.");
            return;
        }

        log.info("클래스 51303C 생성");

        FundClass classC = FundClass.builder()
                .childFundCd("51303C")
                .fundMaster(master)
                .classCode("C")
                .loadType("UNKNOWN")
                .taxCategory(null)
                .saleStatus("ON")
                .sourceUrl("https://investments.miraeasset.com/magi/fund/view.do?fundGb=2&fundCd=513031&childFundCd=51303C&childFundGb=2")
                .build();

        FundRules rules = FundRules.builder()
                .childFundCd("51303C")
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
                .redemptionFeeRate(new BigDecimal("0.0070"))
                .redemptionFeeDays(90)
                .build();

        FundFees fees = FundFees.builder()
                .childFundCd("51303C")
                .fundClass(classC)
                .mgmtFeeBps(55)
                .salesFeeBps(35)
                .trusteeFeeBps(3)
                .adminFeeBps(2)
                .frontLoadPct(null)
                .totalFeeBps(95)
                .build();

        classC.setFundRules(rules);
        classC.setFundFees(fees);
        fundClassRepository.save(classC);

        LocalDate today = LocalDate.now();
        fundNavRepository.save(FundNav.builder()
                .childFundCd("51303C")
                .navDate(today.minusDays(2))
                .nav(new BigDecimal("995.4300"))
                .publishedAt(today.minusDays(1).atTime(10, 5))
                .build());
        fundNavRepository.save(FundNav.builder()
                .childFundCd("51303C")
                .navDate(today.minusDays(1))
                .nav(new BigDecimal("996.2200"))
                .publishedAt(today.atTime(10, 5))
                .build());

        log.info("미래에셋퇴직플랜 생성 완료 - 클래스 C");
    }

    /**
     * 미래에셋퇴직플랜30 펀드 생성
     */
    private void createMiraeAssetPlan30() {
        log.info("미래에셋퇴직플랜30 생성 중...");

        FundMaster master = fundMasterRepository.findById("513051")
                .orElseGet(() -> {
                    log.info("FundMaster 513051 생성");
                    return fundMasterRepository.save(FundMaster.builder()
                            .fundCd("513051")
                            .fundName("미래에셋퇴직플랜30증권자투자신탁1호(채권혼합)")
                            .fundGb(2)
                            .assetType("채권혼합")
                            .riskGrade(null)
                            .currency("KRW")
                            .isActive(true)
                            .build());
                });

        if (fundClassRepository.existsById("51305P")) {
            log.info("클래스 51305P는 이미 존재합니다. 스킵합니다.");
            return;
        }

        log.info("클래스 51305P 생성");

        FundClass classP = FundClass.builder()
                .childFundCd("51305P")
                .fundMaster(master)
                .classCode("P")
                .loadType("UNKNOWN")
                .taxCategory(null)
                .saleStatus("ON")
                .sourceUrl("https://investments.miraeasset.com/magi/fund/view.do?fundGb=2&fundCd=513051&childFundCd=51305P&childFundGb=2")
                .build();

        FundRules rules = FundRules.builder()
                .childFundCd("51305P")
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

        FundFees fees = FundFees.builder()
                .childFundCd("51305P")
                .fundClass(classP)
                .mgmtFeeBps(45)
                .salesFeeBps(25)
                .trusteeFeeBps(3)
                .adminFeeBps(2)
                .frontLoadPct(null)
                .totalFeeBps(75)
                .build();

        classP.setFundRules(rules);
        classP.setFundFees(fees);
        fundClassRepository.save(classP);

        LocalDate today = LocalDate.now();
        fundNavRepository.save(FundNav.builder()
                .childFundCd("51305P")
                .navDate(today.minusDays(2))
                .nav(new BigDecimal("1005.0100"))
                .publishedAt(today.minusDays(1).atTime(10, 5))
                .build());
        fundNavRepository.save(FundNav.builder()
                .childFundCd("51305P")
                .navDate(today.minusDays(1))
                .nav(new BigDecimal("1006.4800"))
                .publishedAt(today.atTime(10, 5))
                .build());

        log.info("미래에셋퇴직플랜30 생성 완료 - 클래스 P");
    }

    /**
     * 미래에셋퇴직플랜목돈분할투자3/10 펀드 생성
     */
    private void createMiraeAssetLumpSum() {
        log.info("미래에셋퇴직플랜목돈분할투자3/10 생성 중...");

        FundMaster master = fundMasterRepository.findById("513920")
                .orElseGet(() -> {
                    log.info("FundMaster 513920 생성");
                    return fundMasterRepository.save(FundMaster.builder()
                            .fundCd("513920")
                            .fundName("미래에셋퇴직플랜목돈분할투자3/10증권자투자신탁4호(채권혼합)")
                            .fundGb(2)
                            .assetType("채권혼합")
                            .riskGrade(null)
                            .currency("KRW")
                            .isActive(true)
                            .build());
                });

        if (fundClassRepository.existsById("513921")) {
            log.info("클래스 513921는 이미 존재합니다. 스킵합니다.");
            return;
        }

        log.info("클래스 513921 생성");

        FundClass fundClass = FundClass.builder()
                .childFundCd("513921")
                .fundMaster(master)
                .classCode(null)
                .loadType("UNKNOWN")
                .taxCategory(null)
                .saleStatus("ON")
                .sourceUrl("https://investments.miraeasset.com/magi/fund/view.do?fundGb=2&fundCd=513920&childFundCd=513921&childFundGb=2")
                .build();

        FundRules rules = FundRules.builder()
                .childFundCd("513921")
                .fundClass(fundClass)
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
                .redemptionFeeRate(new BigDecimal("0.0070"))
                .redemptionFeeDays(90)
                .build();

        FundFees fees = FundFees.builder()
                .childFundCd("513921")
                .fundClass(fundClass)
                .mgmtFeeBps(55)
                .salesFeeBps(35)
                .trusteeFeeBps(3)
                .adminFeeBps(2)
                .frontLoadPct(null)
                .totalFeeBps(95)
                .build();

        fundClass.setFundRules(rules);
        fundClass.setFundFees(fees);
        fundClassRepository.save(fundClass);

        LocalDate today = LocalDate.now();
        fundNavRepository.save(FundNav.builder()
                .childFundCd("513921")
                .navDate(today.minusDays(2))
                .nav(new BigDecimal("998.7600"))
                .publishedAt(today.minusDays(1).atTime(10, 6))
                .build());
        fundNavRepository.save(FundNav.builder()
                .childFundCd("513921")
                .navDate(today.minusDays(1))
                .nav(new BigDecimal("1001.1500"))
                .publishedAt(today.atTime(10, 6))
                .build());

        log.info("미래에셋퇴직플랜목돈분할투자3/10 생성 완료");
    }
}

