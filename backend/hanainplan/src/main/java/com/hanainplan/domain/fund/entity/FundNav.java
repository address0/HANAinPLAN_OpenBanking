package com.hanainplan.domain.fund.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fund_nav")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundNav {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nav_id")
    private Long navId;

    @Column(name = "child_fund_cd", length = 16, nullable = false)
    private String childFundCd;

    @Column(name = "nav_date", nullable = false)
    private LocalDate navDate;

    @Column(name = "nav", nullable = false, precision = 15, scale = 4)
    private BigDecimal nav;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public String getNavString() {
        return String.format("%,.2f", nav);
    }
}