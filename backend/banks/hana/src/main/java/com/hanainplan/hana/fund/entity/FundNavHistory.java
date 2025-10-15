package com.hanainplan.hana.fund.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "hana_fund_nav_history", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"fund_code", "base_date"}),
       indexes = {
           @Index(name = "idx_fund_code", columnList = "fund_code"),
           @Index(name = "idx_base_date", columnList = "base_date")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundNavHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nav_id")
    private Long navId;

    @Column(name = "fund_code", nullable = false, length = 20)
    private String fundCode;

    @Column(name = "base_date", nullable = false)
    private LocalDate baseDate;

    @Column(name = "nav", nullable = false, precision = 15, scale = 4)
    private BigDecimal nav;

    @Column(name = "previous_nav", precision = 15, scale = 4)
    private BigDecimal previousNav;

    @Column(name = "change_amount", precision = 15, scale = 4)
    private BigDecimal changeAmount;

    @Column(name = "change_rate", precision = 10, scale = 4)
    private BigDecimal changeRate;

    @Column(name = "total_net_assets", precision = 20, scale = 2)
    private BigDecimal totalNetAssets;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();

        if (nav != null && previousNav != null && previousNav.compareTo(BigDecimal.ZERO) > 0) {
            changeAmount = nav.subtract(previousNav);

            changeRate = changeAmount
                .divide(previousNav, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }
    }

    public boolean isIncreased() {
        return changeAmount != null && changeAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isDecreased() {
        return changeAmount != null && changeAmount.compareTo(BigDecimal.ZERO) < 0;
    }

    public String getChangeRateString() {
        if (changeRate == null) {
            return "0.00%";
        }
        String sign = changeRate.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
        return String.format("%s%.2f%%", sign, changeRate);
    }
}