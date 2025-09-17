package com.hanainplan.domain.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 통계집계 엔터티
 * - 일간/주간/월간 서비스 이용 통계
 * - 관리자 대시보드 데이터 제공
 * - 비즈니스 인사이트 분석 기초 데이터
 */
@Entity
@Table(name = "TB_STATISTICS_SUMMARY")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class StatisticsSummary {

    @Id
    @Column(name = "STATISTICS_ID", length = 20)
    private String statisticsId;

    /**
     * 통계 유형: 일간, 주간, 월간
     */
    @Column(name = "STATISTICS_TYPE", length = 20, nullable = false)
    private String statisticsType;

    /**
     * 통계 기준일자
     */
    @Column(name = "REFERENCE_DATE", nullable = false)
    private LocalDate referenceDate;

    /**
     * 총 사용자 수
     */
    @Column(name = "TOTAL_USERS", nullable = false)
    private Integer totalUsers;

    /**
     * 신규 가입 건수
     */
    @Column(name = "NEW_SUBSCRIPTIONS")
    private Integer newSubscriptions;

    /**
     * 총 자산 규모
     */
    @Column(name = "TOTAL_ASSET_VALUE", precision = 20, scale = 2)
    private BigDecimal totalAssetValue;

    /**
     * 최다 가입 상품 ID
     */
    @Column(name = "TOP_PRODUCT_ID", length = 20)
    private String topProductId;

    @CreatedDate
    @Column(name = "CREATED_DATE", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    /**
     * 신규 가입 전환율 계산
     */
    public double getConversionRate() {
        if (totalUsers == 0) return 0.0;
        return (double) (newSubscriptions != null ? newSubscriptions : 0) / totalUsers * 100;
    }

    /**
     * 사용자당 평균 자산 계산
     */
    public BigDecimal getAverageAssetPerUser() {
        if (totalUsers == 0 || totalAssetValue == null) {
            return BigDecimal.ZERO;
        }
        return totalAssetValue.divide(BigDecimal.valueOf(totalUsers), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 통계 유형 enum
     */
    public enum StatisticsType {
        DAILY("일간"), WEEKLY("주간"), MONTHLY("월간"), YEARLY("연간");

        private final String value;

        StatisticsType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static StatisticsType fromValue(String value) {
            for (StatisticsType type : values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            return DAILY; // 기본값
        }
    }
}
