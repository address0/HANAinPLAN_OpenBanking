package com.hanainplan.hanhwa.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "hanhwa_pension_insurance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PensionInsurance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_code", unique = true, nullable = false, length = 20)
    private String productCode; // 상품코드

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName; // 상품명

    @Column(name = "maintenance_period", length = 50)
    private String maintenancePeriod; // 유지기간

    @Column(name = "premium_payment", precision = 15, scale = 2)
    private BigDecimal premiumPayment; // 납입보험료

    @Column(name = "contractor_accumulation_male", precision = 15, scale = 2)
    private BigDecimal contractorAccumulationMale; // 계약자적립액(남자)

    @Column(name = "accumulation_rate_male", precision = 5, scale = 4)
    private BigDecimal accumulationRateMale; // 적립률(남자)

    @Column(name = "surrender_value_male", precision = 15, scale = 2)
    private BigDecimal surrenderValueMale; // 해약환급금(남자)

    @Column(name = "contractor_accumulation_female", precision = 15, scale = 2)
    private BigDecimal contractorAccumulationFemale; // 계약자적립액(여자)

    @Column(name = "accumulation_rate_female", precision = 5, scale = 4)
    private BigDecimal accumulationRateFemale; // 적립률(여자)

    @Column(name = "surrender_value_female", precision = 15, scale = 2)
    private BigDecimal surrenderValueFemale; // 해약환급금(여자)

    @Column(name = "expected_return_rate_minimum", precision = 5, scale = 4)
    private BigDecimal expectedReturnRateMinimum; // 예상수익률(최저보증이율, 선택)

    @Column(name = "expected_return_rate_current", precision = 5, scale = 4)
    private BigDecimal expectedReturnRateCurrent; // 예상수익률(현재공시이율, 선택)

    @Column(name = "expected_return_rate_average", precision = 5, scale = 4)
    private BigDecimal expectedReturnRateAverage; // 예상수익률(평균공시이율, 선택)

    @Column(name = "business_expense_ratio", precision = 5, scale = 4)
    private BigDecimal businessExpenseRatio; // 사업비율

    @Column(name = "risk_coverage", length = 200)
    private String riskCoverage; // 위험보장

    @Column(name = "current_announced_rate", precision = 5, scale = 4)
    private BigDecimal currentAnnouncedRate; // 현재공시이율

    @Column(name = "minimum_guaranteed_rate", length = 20)
    private String minimumGuaranteedRate; // 최저보증이율(varchar)

    @Column(name = "subscription_type", length = 50)
    private String subscriptionType; // 가입유형

    @Column(name = "is_universal", length = 10)
    private String isUniversal; // 유니버셜여부 (Y/N)

    @Column(name = "payment_method", length = 100)
    private String paymentMethod; // 납입방법

    @Column(name = "sales_channel", length = 100)
    private String salesChannel; // 판매채널

    @Column(name = "special_notes", length = 1000)
    private String specialNotes; // 특이사항

    @Column(name = "representative_number", length = 20)
    private String representativeNumber; // 대표번호

    @Column(name = "is_active")
    private Boolean isActive; // 활성화여부

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
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
}
