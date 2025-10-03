package com.hanainplan.hana.product.util;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 하나은행 정기예금 금리 계산 유틸리티
 */
@Slf4j
public class InterestRateCalculator {

    /**
     * 상품 유형별 기본 금리 조회
     * 
     * @param productType 상품 유형 (0:일반, 1:디폴트옵션, 2:일단위)
     * @param contractPeriod 계약 기간 (개월 또는 일)
     * @return 연 금리 (소수)
     */
    public static BigDecimal getBaseRate(int productType, int contractPeriod) {
        switch (productType) {
            case 0: // 일반 상품
                return getGeneralProductRate(contractPeriod);
            case 1: // 디폴트옵션
                return BigDecimal.valueOf(0.0220); // 2.20%
            case 2: // 일단위
                return getDailyProductRate(contractPeriod);
            default:
                throw new IllegalArgumentException("지원하지 않는 상품 유형입니다: " + productType);
        }
    }

    /**
     * 일반 상품 금리 (개월 단위)
     */
    private static BigDecimal getGeneralProductRate(int months) {
        switch (months) {
            case 6:
                return BigDecimal.valueOf(0.0207); // 2.07%
            case 12:
                return BigDecimal.valueOf(0.0240); // 2.40%
            case 24:
                return BigDecimal.valueOf(0.0200); // 2.00%
            case 36:
                return BigDecimal.valueOf(0.0210); // 2.10%
            case 48:
                return BigDecimal.valueOf(0.0200); // 2.00%
            case 60:
                return BigDecimal.valueOf(0.0202); // 2.02%
            default:
                throw new IllegalArgumentException("일반 상품은 6개월, 12~60개월(연 단위)만 가능합니다: " + months + "개월");
        }
    }

    /**
     * 일단위 상품 금리 (일 단위)
     */
    private static BigDecimal getDailyProductRate(int days) {
        if (days >= 1825) { // 60개월(5년) 이상
            return BigDecimal.valueOf(0.0200); // 2.00%
        } else if (days >= 1461) { // 48개월(4년) 이상
            return BigDecimal.valueOf(0.0200); // 2.00%
        } else if (days >= 1096) { // 36개월(3년) 이상
            return BigDecimal.valueOf(0.0210); // 2.10%
        } else if (days >= 913) { // 30개월 이상
            return BigDecimal.valueOf(0.0198); // 1.98%
        } else if (days >= 730) { // 24개월(2년) 이상
            return BigDecimal.valueOf(0.0200); // 2.00%
        } else if (days >= 548) { // 18개월 이상
            return BigDecimal.valueOf(0.0225); // 2.25%
        } else if (days >= 365) { // 12개월(1년) 이상
            return BigDecimal.valueOf(0.0240); // 2.40%
        } else if (days >= 270) { // 9개월 이상
            return BigDecimal.valueOf(0.0211); // 2.11%
        } else if (days >= 180) { // 6개월 이상
            return BigDecimal.valueOf(0.0207); // 2.07%
        } else if (days >= 90) { // 3개월 이상
            return BigDecimal.valueOf(0.0202); // 2.02%
        } else if (days >= 30) { // 1개월 이상
            return BigDecimal.valueOf(0.0192); // 1.92%
        } else {
            throw new IllegalArgumentException("일단위 상품은 31일 이상만 가입 가능합니다: " + days + "일");
        }
    }

    /**
     * 만기 이자 계산
     * 산식: 신규금액 * 이자율 * 약정개월수/12
     * 
     * @param principal 원금
     * @param rate 연 이자율
     * @param months 약정 개월수
     * @return 만기 이자 금액
     */
    public static BigDecimal calculateMaturityInterest(BigDecimal principal, BigDecimal rate, int months) {
        BigDecimal interest = principal
                .multiply(rate)
                .multiply(BigDecimal.valueOf(months))
                .divide(BigDecimal.valueOf(12), 2, RoundingMode.DOWN);
        
        log.debug("만기 이자 계산: 원금={}, 이자율={}, 개월수={}, 이자={}", 
                principal, rate, months, interest);
        
        return interest;
    }

    /**
     * 중도해지 이자율 계산
     * 
     * @param productType 상품 유형
     * @param baseRate 기본 이자율
     * @param elapsedDays 경과 일수
     * @param contractDays 계약 일수
     * @return 중도해지 이자율 (소수점 넷째자리 절삭)
     */
    public static BigDecimal calculateEarlyTerminationRate(int productType, BigDecimal baseRate, 
                                                          long elapsedDays, long contractDays) {
        BigDecimal rate;
        
        if (productType == 1) {
            // 디폴트옵션: 특별 규정
            rate = calculateDefaultOptionEarlyRate(baseRate, elapsedDays);
        } else {
            // 일반 상품 및 일단위 상품
            rate = calculateGeneralEarlyRate(baseRate, elapsedDays, contractDays);
        }
        
        // 소수점 넷째자리에서 절삭 (셋째자리까지)
        BigDecimal truncatedRate = rate.setScale(4, RoundingMode.DOWN);
        
        log.debug("중도해지 이자율 계산: 상품유형={}, 기본이자율={}, 경과일={}, 계약일={}, 적용이자율={}", 
                productType, baseRate, elapsedDays, contractDays, truncatedRate);
        
        return truncatedRate;
    }

    /**
     * 일반/일단위 상품 중도해지 이자율
     */
    private static BigDecimal calculateGeneralEarlyRate(BigDecimal baseRate, long elapsedDays, long contractDays) {
        if (elapsedDays < 30) {
            // 1개월 미만: 연 0.10%
            return BigDecimal.valueOf(0.0010);
        } else if (elapsedDays < 90) {
            // 1개월 이상 ~ 3개월 미만: 연 0.15%
            return BigDecimal.valueOf(0.0015);
        } else if (elapsedDays < 180) {
            // 3개월 이상 ~ 6개월 미만: 연 0.20%
            return BigDecimal.valueOf(0.0020);
        } else {
            // 6개월 이상: 이자율 * 차등률 * 경과율
            BigDecimal differentialRate = getDifferentialRate(elapsedDays);
            BigDecimal elapsedRate = BigDecimal.valueOf(Math.min(elapsedDays, contractDays))
                    .divide(BigDecimal.valueOf(contractDays), 10, RoundingMode.HALF_UP);
            
            BigDecimal calculatedRate = baseRate
                    .multiply(differentialRate)
                    .multiply(elapsedRate);
            
            // 계산 결과가 0.20% 미만이면 연 0.20% 적용
            BigDecimal minRate = BigDecimal.valueOf(0.0020);
            return calculatedRate.compareTo(minRate) < 0 ? minRate : calculatedRate;
        }
    }

    /**
     * 디폴트옵션 중도해지 이자율
     */
    private static BigDecimal calculateDefaultOptionEarlyRate(BigDecimal baseRate, long elapsedDays) {
        if (elapsedDays < 960) { // 32개월 미만 (32개월 = 960일 기준)
            // 이자율 * 80%
            return baseRate.multiply(BigDecimal.valueOf(0.80));
        } else {
            // 32개월 이상: 이자율 * 90%
            return baseRate.multiply(BigDecimal.valueOf(0.90));
        }
    }

    /**
     * 차등률 계산
     */
    private static BigDecimal getDifferentialRate(long elapsedDays) {
        if (elapsedDays < 270) { // 9개월 미만
            return BigDecimal.valueOf(0.60); // 60%
        } else if (elapsedDays < 330) { // 11개월 미만
            return BigDecimal.valueOf(0.70); // 70%
        } else {
            return BigDecimal.valueOf(0.90); // 90%
        }
    }

    /**
     * 중도해지 이자 계산
     * 
     * @param principal 원금
     * @param earlyRate 중도해지 이자율
     * @param elapsedDays 경과 일수
     * @return 중도해지 이자 금액
     */
    public static BigDecimal calculateEarlyTerminationInterest(BigDecimal principal, 
                                                               BigDecimal earlyRate, 
                                                               long elapsedDays) {
        BigDecimal interest = principal
                .multiply(earlyRate)
                .multiply(BigDecimal.valueOf(elapsedDays))
                .divide(BigDecimal.valueOf(365), 2, RoundingMode.DOWN);
        
        log.debug("중도해지 이자 계산: 원금={}, 중도해지율={}, 경과일={}, 이자={}", 
                principal, earlyRate, elapsedDays, interest);
        
        return interest;
    }

    /**
     * 만기일 계산
     * 
     * @param subscriptionDate 가입일
     * @param productType 상품 유형
     * @param contractPeriod 계약 기간
     * @return 만기일
     */
    public static LocalDate calculateMaturityDate(LocalDate subscriptionDate, int productType, int contractPeriod) {
        if (productType == 2) {
            // 일단위: 일 더하기
            return subscriptionDate.plusDays(contractPeriod);
        } else {
            // 일반, 디폴트옵션: 개월 더하기
            return subscriptionDate.plusMonths(contractPeriod);
        }
    }

    /**
     * 경과 일수 계산
     * 
     * @param startDate 시작일
     * @param endDate 종료일
     * @return 경과 일수
     */
    public static long calculateElapsedDays(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * 계약 일수 계산
     * 
     * @param subscriptionDate 가입일
     * @param maturityDate 만기일
     * @return 계약 일수
     */
    public static long calculateContractDays(LocalDate subscriptionDate, LocalDate maturityDate) {
        return ChronoUnit.DAYS.between(subscriptionDate, maturityDate);
    }
}



