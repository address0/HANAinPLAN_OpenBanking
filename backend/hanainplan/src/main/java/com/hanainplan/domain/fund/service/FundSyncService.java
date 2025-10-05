package com.hanainplan.domain.fund.service;

import com.hanainplan.domain.banking.client.HanaBankClient;
import com.hanainplan.domain.fund.entity.*;
import com.hanainplan.domain.fund.repository.FundClassRepository;
import com.hanainplan.domain.fund.repository.FundMasterRepository;
import com.hanainplan.domain.fund.repository.FundNavRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * 펀드 상품 동기화 서비스
 * 하나은행의 복잡한 펀드 구조(FundMaster, FundClass, FundRules, FundFees, FundNav)를
 * 하나인플랜 DB에 동기화
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FundSyncService {

    private final HanaBankClient hanaBankClient;
    private final FundMasterRepository fundMasterRepository;
    private final FundClassRepository fundClassRepository;
    private final FundNavRepository fundNavRepository;

    /**
     * 하나은행에서 펀드 상품 데이터 동기화
     * FundClass (실제 펀드 구조) 동기화
     */
    @Transactional
    public void syncFundProducts() {
        log.info("===== 펀드 상품 동기화 시작 =====");
        
        try {
            // 하나은행에서 판매중인 펀드 클래스 목록 조회
            List<Map<String, Object>> fundClassesFromBank = hanaBankClient.getAllOnSaleFundClasses();
            log.info("하나은행에서 조회한 펀드 클래스 수: {}건", fundClassesFromBank.size());

            int masterCreated = 0;
            int masterUpdated = 0;
            int classCreated = 0;
            int classUpdated = 0;
            int navCreated = 0;

            // 각 펀드 클래스를 DB에 저장 또는 업데이트
            for (Map<String, Object> fundData : fundClassesFromBank) {
                try {
                    // 1. FundMaster 동기화
                    FundMaster fundMaster = syncFundMaster(fundData);
                    if (fundMaster != null) {
                        if (fundMaster.getSyncedAt() == null) {
                            masterCreated++;
                        } else {
                            masterUpdated++;
                        }
                    }
                    
                    // 2. FundClass 동기화
                    FundClass fundClass = syncFundClass(fundData, fundMaster);
                    if (fundClass != null) {
                        String childFundCd = fundClass.getChildFundCd();
                        boolean existed = fundClassRepository.existsById(childFundCd);
                        if (!existed) {
                            classCreated++;
                        } else {
                            classUpdated++;
                        }
                    }
                    
                    // 3. FundNav 동기화
                    boolean navSynced = syncFundNav(fundData);
                    if (navSynced) {
                        navCreated++;
                    }
                    
                } catch (Exception e) {
                    log.error("펀드 클래스 동기화 실패 - childFundCd: {}", fundData.get("childFundCd"), e);
                }
            }

            log.info("===== 펀드 상품 동기화 완료 =====");
            log.info("FundMaster - 신규: {}건, 업데이트: {}건", masterCreated, masterUpdated);
            log.info("FundClass - 신규: {}건, 업데이트: {}건", classCreated, classUpdated);
            log.info("FundNav - 신규: {}건", navCreated);

        } catch (Exception e) {
            log.error("펀드 상품 동기화 중 오류 발생", e);
            throw new RuntimeException("펀드 상품 동기화에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * FundMaster 동기화
     */
    @SuppressWarnings("unchecked")
    private FundMaster syncFundMaster(Map<String, Object> fundData) {
        Map<String, Object> fundMasterData = (Map<String, Object>) fundData.get("fundMaster");
        if (fundMasterData == null) {
            return null;
        }

        String fundCd = (String) fundMasterData.get("fundCd");
        FundMaster fundMaster = fundMasterRepository.findById(fundCd)
                .orElse(FundMaster.builder()
                        .fundCd(fundCd)
                        .build());

        // 데이터 매핑
        fundMaster.setFundName((String) fundMasterData.get("fundName"));
        fundMaster.setFundGb((Integer) fundMasterData.get("fundGb"));
        fundMaster.setAssetType((String) fundMasterData.get("assetType"));
        fundMaster.setRiskGrade((String) fundMasterData.get("riskGrade"));
        fundMaster.setCurrency((String) fundMasterData.get("currency"));
        fundMaster.setIsActive((Boolean) fundMasterData.get("isActive"));
        fundMaster.updateSyncTime();

        return fundMasterRepository.save(fundMaster);
    }

    /**
     * FundClass 동기화 (Rules, Fees 포함)
     */
    @SuppressWarnings("unchecked")
    private FundClass syncFundClass(Map<String, Object> fundData, FundMaster fundMaster) {
        if (fundMaster == null) {
            return null;
        }

        String childFundCd = (String) fundData.get("childFundCd");
        FundClass fundClass = fundClassRepository.findById(childFundCd)
                .orElse(FundClass.builder()
                        .childFundCd(childFundCd)
                        .fundMaster(fundMaster)
                        .build());

        // 기본 정보 매핑
        fundClass.setClassCode((String) fundData.get("classCode"));
        fundClass.setLoadType((String) fundData.get("loadType"));
        fundClass.setTaxCategory((String) fundData.get("taxCategory"));
        fundClass.setSaleStatus((String) fundData.get("saleStatus"));
        fundClass.setSourceUrl((String) fundData.get("sourceUrl"));

        // FundClass 저장
        fundClass = fundClassRepository.save(fundClass);

        // FundRules 동기화
        Map<String, Object> rulesData = (Map<String, Object>) fundData.get("rules");
        if (rulesData != null) {
            syncFundRules(fundClass, rulesData);
        }

        // FundFees 동기화
        Map<String, Object> feesData = (Map<String, Object>) fundData.get("fees");
        if (feesData != null) {
            syncFundFees(fundClass, feesData);
        }

        return fundClass;
    }

    /**
     * FundRules 동기화
     */
    private void syncFundRules(FundClass fundClass, Map<String, Object> rulesData) {
        FundRules rules = fundClass.getFundRules();
        if (rules == null) {
            rules = FundRules.builder()
                    .childFundCd(fundClass.getChildFundCd())
                    .build();
        }

        // LocalTime 파싱
        rules.setCutoffTime(parseLocalTime(rulesData.get("cutoffTime")));
        rules.setNavPublishTime(parseLocalTime(rulesData.get("navPublishTime")));
        rules.setBuySettleDays((Integer) rulesData.get("buySettleDays"));
        rules.setRedeemSettleDays((Integer) rulesData.get("redeemSettleDays"));
        rules.setUnitType((String) rulesData.get("unitType"));
        rules.setMinInitialAmount(toBigDecimal(rulesData.get("minInitialAmount")));
        rules.setMinAdditional(toBigDecimal(rulesData.get("minAdditional")));
        rules.setIncrementAmount(toBigDecimal(rulesData.get("incrementAmount")));
        rules.setAllowSip((Boolean) rulesData.get("allowSip"));
        rules.setAllowSwitch((Boolean) rulesData.get("allowSwitch"));
        rules.setRedemptionFeeRate(toBigDecimal(rulesData.get("redemptionFeeRate")));
        rules.setRedemptionFeeDays((Integer) rulesData.get("redemptionFeeDays"));

        fundClass.setFundRules(rules);
        rules.setFundClass(fundClass);
    }

    /**
     * FundFees 동기화
     */
    private void syncFundFees(FundClass fundClass, Map<String, Object> feesData) {
        FundFees fees = fundClass.getFundFees();
        if (fees == null) {
            fees = FundFees.builder()
                    .childFundCd(fundClass.getChildFundCd())
                    .build();
        }

        fees.setMgmtFeeBps((Integer) feesData.get("mgmtFeeBps"));
        fees.setSalesFeeBps((Integer) feesData.get("salesFeeBps"));
        fees.setTrusteeFeeBps((Integer) feesData.get("trusteeFeeBps"));
        fees.setAdminFeeBps((Integer) feesData.get("adminFeeBps"));
        fees.setFrontLoadPct(toBigDecimal(feesData.get("frontLoadPct")));
        fees.setTotalFeeBps((Integer) feesData.get("totalFeeBps"));

        fundClass.setFundFees(fees);
        fees.setFundClass(fundClass);
    }

    /**
     * FundNav 동기화
     */
    private boolean syncFundNav(Map<String, Object> fundData) {
        String childFundCd = (String) fundData.get("childFundCd");
        BigDecimal latestNav = toBigDecimal(fundData.get("latestNav"));
        Object navDateObj = fundData.get("latestNavDate");
        
        if (latestNav == null || navDateObj == null) {
            return false;
        }

        LocalDate navDate = parseLocalDate(navDateObj);
        if (navDate == null) {
            return false;
        }

        // 이미 존재하는 기준가인지 확인
        if (fundNavRepository.existsByChildFundCdAndNavDate(childFundCd, navDate)) {
            return false;
        }

        // 새 기준가 저장
        FundNav fundNav = FundNav.builder()
                .childFundCd(childFundCd)
                .navDate(navDate)
                .nav(latestNav)
                .publishedAt(LocalDateTime.now())
                .build();

        fundNavRepository.save(fundNav);
        return true;
    }

    /**
     * LocalTime 파싱
     */
    private LocalTime parseLocalTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalTime) {
            return (LocalTime) value;
        }
        if (value instanceof String) {
            try {
                return LocalTime.parse((String) value);
            } catch (Exception e) {
                log.warn("LocalTime 파싱 실패: {}", value);
                return null;
            }
        }
        if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<Integer> parts = (List<Integer>) value;
            if (parts.size() >= 2) {
                return LocalTime.of(parts.get(0), parts.get(1), parts.size() > 2 ? parts.get(2) : 0);
            }
        }
        return null;
    }

    /**
     * LocalDate 파싱
     */
    private LocalDate parseLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate) {
            return (LocalDate) value;
        }
        if (value instanceof String) {
            try {
                return LocalDate.parse((String) value);
            } catch (Exception e) {
                log.warn("LocalDate 파싱 실패: {}", value);
                return null;
            }
        }
        if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<Integer> parts = (List<Integer>) value;
            if (parts.size() >= 3) {
                return LocalDate.of(parts.get(0), parts.get(1), parts.get(2));
            }
        }
        return null;
    }

    /**
     * Object를 BigDecimal로 변환
     */
    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                log.warn("BigDecimal 변환 실패: {}", value);
                return null;
            }
        }
        return null;
    }
}
