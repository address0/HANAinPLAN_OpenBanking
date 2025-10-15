package com.hanainplan.domain.banking.util;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Slf4j
public class InterestRateCalculator {

    public static BigDecimal getBaseRate(int productType, int contractPeriod) {
        switch (productType) {
            case 0:
                return getGeneralProductRate(contractPeriod);
            case 1:
                return BigDecimal.valueOf(0.0220);
            case 2:
                return getDailyProductRate(contractPeriod);
            default:
                throw new IllegalArgumentException("지원하지 않는 상품 유형입니다: " + productType);
        }
    }

    private static BigDecimal getGeneralProductRate(int months) {
        switch (months) {
            case 6:
                return BigDecimal.valueOf(0.0207);
            case 12:
                return BigDecimal.valueOf(0.0240);
            case 24:
                return BigDecimal.valueOf(0.0200);
            case 36:
                return BigDecimal.valueOf(0.0210);
            case 48:
                return BigDecimal.valueOf(0.0200);
            case 60:
                return BigDecimal.valueOf(0.0202);
            default:
                throw new IllegalArgumentException("일반 상품은 6개월, 12~60개월(연 단위)만 가능합니다");
        }
    }

    private static BigDecimal getDailyProductRate(int days) {
        if (days >= 1825) return BigDecimal.valueOf(0.0200);
        else if (days >= 1461) return BigDecimal.valueOf(0.0200);
        else if (days >= 1096) return BigDecimal.valueOf(0.0210);
        else if (days >= 913) return BigDecimal.valueOf(0.0198);
        else if (days >= 730) return BigDecimal.valueOf(0.0200);
        else if (days >= 548) return BigDecimal.valueOf(0.0225);
        else if (days >= 365) return BigDecimal.valueOf(0.0240);
        else if (days >= 270) return BigDecimal.valueOf(0.0211);
        else if (days >= 180) return BigDecimal.valueOf(0.0207);
        else if (days >= 90) return BigDecimal.valueOf(0.0202);
        else if (days >= 30) return BigDecimal.valueOf(0.0192);
        else throw new IllegalArgumentException("일단위 상품은 31일 이상만 가입 가능합니다");
    }

    public static BigDecimal calculateMaturityInterest(BigDecimal principal, BigDecimal rate, int months) {
        return principal
                .multiply(rate)
                .multiply(BigDecimal.valueOf(months))
                .divide(BigDecimal.valueOf(12), 2, RoundingMode.DOWN);
    }

    public static BigDecimal calculateEarlyTerminationRate(int productType, BigDecimal baseRate, 
                                                          long elapsedDays, long contractDays) {
        BigDecimal rate;

        if (productType == 1) {
            rate = calculateDefaultOptionEarlyRate(baseRate, elapsedDays);
        } else {
            rate = calculateGeneralEarlyRate(baseRate, elapsedDays, contractDays);
        }

        return rate.setScale(4, RoundingMode.DOWN);
    }

    private static BigDecimal calculateGeneralEarlyRate(BigDecimal baseRate, long elapsedDays, long contractDays) {
        if (elapsedDays < 30) return BigDecimal.valueOf(0.0010);
        else if (elapsedDays < 90) return BigDecimal.valueOf(0.0015);
        else if (elapsedDays < 180) return BigDecimal.valueOf(0.0020);
        else {
            BigDecimal differentialRate = getDifferentialRate(elapsedDays);
            BigDecimal elapsedRate = BigDecimal.valueOf(Math.min(elapsedDays, contractDays))
                    .divide(BigDecimal.valueOf(contractDays), 10, RoundingMode.HALF_UP);

            BigDecimal calculatedRate = baseRate.multiply(differentialRate).multiply(elapsedRate);
            BigDecimal minRate = BigDecimal.valueOf(0.0020);
            return calculatedRate.compareTo(minRate) < 0 ? minRate : calculatedRate;
        }
    }

    private static BigDecimal calculateDefaultOptionEarlyRate(BigDecimal baseRate, long elapsedDays) {
        if (elapsedDays < 960) {
            return baseRate.multiply(BigDecimal.valueOf(0.80));
        } else {
            return baseRate.multiply(BigDecimal.valueOf(0.90));
        }
    }

    private static BigDecimal getDifferentialRate(long elapsedDays) {
        if (elapsedDays < 270) return BigDecimal.valueOf(0.60);
        else if (elapsedDays < 330) return BigDecimal.valueOf(0.70);
        else return BigDecimal.valueOf(0.90);
    }

    public static BigDecimal calculateEarlyTerminationInterest(BigDecimal principal, 
                                                               BigDecimal earlyRate, 
                                                               long elapsedDays) {
        return principal
                .multiply(earlyRate)
                .multiply(BigDecimal.valueOf(elapsedDays))
                .divide(BigDecimal.valueOf(365), 2, RoundingMode.DOWN);
    }

    public static LocalDate calculateMaturityDate(LocalDate subscriptionDate, int productType, int contractPeriod) {
        if (productType == 2) {
            return subscriptionDate.plusDays(contractPeriod);
        } else {
            return subscriptionDate.plusMonths(contractPeriod);
        }
    }

    public static long calculateElapsedDays(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    public static long calculateContractDays(LocalDate subscriptionDate, LocalDate maturityDate) {
        return ChronoUnit.DAYS.between(subscriptionDate, maturityDate);
    }
}