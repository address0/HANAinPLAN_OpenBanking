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

    @Column(name = "STATISTICS_TYPE", length = 20, nullable = false)
    private String statisticsType;

    @Column(name = "REFERENCE_DATE", nullable = false)
    private LocalDate referenceDate;

    @Column(name = "TOTAL_USERS", nullable = false)
    private Integer totalUsers;

    @Column(name = "NEW_SUBSCRIPTIONS")
    private Integer newSubscriptions;

    @Column(name = "TOTAL_ASSET_VALUE", precision = 20, scale = 2)
    private BigDecimal totalAssetValue;

    @Column(name = "TOP_PRODUCT_ID", length = 20)
    private String topProductId;

    @CreatedDate
    @Column(name = "CREATED_DATE", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    public double getConversionRate() {
        if (totalUsers == 0) return 0.0;
        return (double) (newSubscriptions != null ? newSubscriptions : 0) / totalUsers * 100;
    }

    public BigDecimal getAverageAssetPerUser() {
        if (totalUsers == 0 || totalAssetValue == null) {
            return BigDecimal.ZERO;
        }
        return totalAssetValue.divide(BigDecimal.valueOf(totalUsers), 2, BigDecimal.ROUND_HALF_UP);
    }

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
            return DAILY;
        }
    }
}