package com.hanainplan.hanhwa.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "hanhwa_general_insurance")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneralInsurance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_code", unique = true, nullable = false, length = 20)
    private String productCode; // 상품코드

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName; // 상품명

    @Column(name = "category", length = 50)
    private String category; // 구분

    @Column(name = "benefit_name", length = 100)
    private String benefitName; // 급부명칭

    @Column(name = "payment_reason", length = 200)
    private String paymentReason; // 지급사유

    @Column(name = "payment_amount", precision = 15, scale = 2)
    private BigDecimal paymentAmount; // 지급금액

    @Column(name = "subscription_amount_basic", precision = 15, scale = 2)
    private BigDecimal subscriptionAmountBasic; // 가입금액(기본)

    @Column(name = "subscription_amount_male", precision = 15, scale = 2)
    private BigDecimal subscriptionAmountMale; // 가입금액(남자)

    @Column(name = "subscription_amount_female", precision = 15, scale = 2)
    private BigDecimal subscriptionAmountFemale; // 가입금액(여자)

    @Column(name = "interest_rate", precision = 5, scale = 4)
    private BigDecimal interestRate; // 이율

    @Column(name = "insurance_price_index_male", precision = 5, scale = 4)
    private BigDecimal insurancePriceIndexMale; // 보험가격지수(남자)

    @Column(name = "insurance_price_index_female", precision = 5, scale = 4)
    private BigDecimal insurancePriceIndexFemale; // 보험가격지수(여자)

    @Column(name = "product_features", length = 1000)
    private String productFeatures; // 상품특징

    @Column(name = "surrender_value", precision = 15, scale = 2)
    private BigDecimal surrenderValue; // 해약환급금

    @Column(name = "renewal_cycle", length = 50)
    private String renewalCycle; // 갱신주기

    @Column(name = "is_universal", length = 10)
    private String isUniversal; // 유니버셜 (Y/N)

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
