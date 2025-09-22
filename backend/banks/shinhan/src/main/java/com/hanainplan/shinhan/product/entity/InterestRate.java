package com.hanainplan.shinhan.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "shinhan_interest_rates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterestRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interest_rate_id")
    private Long interestRateId; // 금리조건pk

    @Column(name = "product_code", nullable = false, length = 20)
    private String productCode; // 상품코드(논리적fk. 실제 외래키 설정X)

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_type", nullable = false, length = 20)
    private InterestType interestType; // 금리종류(기본/우대/만기후/중도해지)

    @Column(name = "maturity_period", length = 50)
    private String maturityPeriod; // 만기기간

    @Column(name = "interest_rate", precision = 5, scale = 4, nullable = false)
    private BigDecimal interestRate; // 금리(%)

    @Column(name = "is_irp")
    private Boolean isIrp; // IRP여부

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate; // 적용일자

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 금리종류 enum
    public enum InterestType {
        BASIC("기본"),
        PREFERENTIAL("우대"),
        AFTER_MATURITY("만기후"),
        EARLY_WITHDRAWAL("중도해지");

        private final String description;

        InterestType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
