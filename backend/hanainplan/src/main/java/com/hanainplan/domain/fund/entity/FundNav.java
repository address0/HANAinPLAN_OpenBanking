package com.hanainplan.domain.fund.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 펀드 기준가 (하나인플랜)
 * - 하나은행 FundNav와 동일한 구조
 */
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
    private String childFundCd; // 클래스 펀드 코드

    @Column(name = "nav_date", nullable = false)
    private LocalDate navDate; // 기준가 기준일

    @Column(name = "nav", nullable = false, precision = 15, scale = 4)
    private BigDecimal nav; // 기준가

    @Column(name = "published_at")
    private LocalDateTime publishedAt; // 공시 시각

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * 기준가 문자열 반환
     */
    public String getNavString() {
        return String.format("%,.2f", nav);
    }
}

