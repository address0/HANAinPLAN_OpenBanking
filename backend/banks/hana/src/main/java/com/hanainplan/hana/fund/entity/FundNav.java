package com.hanainplan.hana.fund.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 펀드 기준가 (NAV - Net Asset Value)
 * - 클래스별 일별 기준가
 */
@Entity
@Table(name = "fund_nav")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(FundNav.FundNavId.class)
public class FundNav {

    @Id
    @Column(name = "child_fund_cd", length = 16)
    private String childFundCd;

    @Id
    @Column(name = "nav_date")
    private LocalDate navDate; // 기준일

    @Column(name = "nav", nullable = false, precision = 18, scale = 4)
    private BigDecimal nav; // 기준가

    @Column(name = "published_at")
    private LocalDateTime publishedAt; // 기준가 발표 시각

    /**
     * 복합 키 클래스
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FundNavId implements Serializable {
        private String childFundCd;
        private LocalDate navDate;
    }

    /**
     * 기준가 문자열 반환 (예: "1,013.21원")
     */
    public String getNavString() {
        if (nav == null) {
            return "0원";
        }
        return String.format("%,.2f원", nav);
    }

    /**
     * 전일 대비 변동 계산
     */
    public NavChange calculateChange(BigDecimal previousNav) {
        if (previousNav == null || previousNav.compareTo(BigDecimal.ZERO) == 0) {
            return new NavChange(BigDecimal.ZERO, BigDecimal.ZERO);
        }

        BigDecimal changeAmount = nav.subtract(previousNav);
        BigDecimal changeRate = changeAmount
                .divide(previousNav, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        return new NavChange(changeAmount, changeRate);
    }

    /**
     * 변동 정보 클래스
     */
    @Data
    @AllArgsConstructor
    public static class NavChange {
        private BigDecimal changeAmount; // 변동액
        private BigDecimal changeRate;   // 변동률 (%)
    }
}

