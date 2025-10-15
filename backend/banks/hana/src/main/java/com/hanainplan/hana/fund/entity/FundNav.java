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
    private LocalDate navDate;

    @Column(name = "nav", nullable = false, precision = 18, scale = 4)
    private BigDecimal nav;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FundNavId implements Serializable {
        private String childFundCd;
        private LocalDate navDate;
    }

    public String getNavString() {
        if (nav == null) {
            return "0원";
        }
        return String.format("%,.2f원", nav);
    }

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

    @Data
    @AllArgsConstructor
    public static class NavChange {
        private BigDecimal changeAmount;
        private BigDecimal changeRate;
    }
}