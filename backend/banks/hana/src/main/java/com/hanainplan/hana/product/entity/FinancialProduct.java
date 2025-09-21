package com.hanainplan.hana.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "hana_financial_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_code", unique = true, nullable = false, length = 20)
    private String productCode; // 상품코드

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName; // 상품명

    @Column(name = "deposit_type", length = 50)
    private String depositType; // 예금종류

    @Column(name = "subscription_period", length = 50)
    private String subscriptionPeriod; // 가입기간

    @Column(name = "subscription_target", length = 100)
    private String subscriptionTarget; // 가입대상

    @Column(name = "subscription_amount", precision = 15, scale = 2)
    private BigDecimal subscriptionAmount; // 가입금액

    @Column(name = "product_category", length = 50)
    private String productCategory; // 상품유형(선택)

    @Column(name = "interest_payment", length = 100)
    private String interestPayment; // 이자지급

    @Column(name = "tax_benefit", length = 200)
    private String taxBenefit; // 세제혜택(선택)

    @Column(name = "partial_withdrawal", length = 100)
    private String partialWithdrawal; // 일부해지

    @Column(name = "depositor_protection", length = 10)
    private String depositorProtection; // 예금자보호여부

    @Column(name = "transaction_method", length = 200)
    private String transactionMethod; // 거래방법

    @Column(name = "precautions", length = 1000)
    private String precautions; // 유의사항

    @Column(name = "base_interest_rate", precision = 5, scale = 4)
    private BigDecimal baseInterestRate; // 기본이자율

    @Column(name = "preferential_interest_rate", precision = 5, scale = 4)
    private BigDecimal preferentialInterestRate; // 우대이자율(선택)

    @Column(name = "preferential_conditions", length = 500)
    private String preferentialConditions; // 우대이자조건(선택)

    @Column(name = "contract_cancellation_right", length = 200)
    private String contractCancellationRight; // 위법계약해지권(선택)

    @Column(name = "cancellation_penalty", length = 200)
    private String cancellationPenalty; // 해지 시 불이익(선택)

    @Column(name = "payment_restrictions", length = 200)
    private String paymentRestrictions; // 지급관련제한(선택)

    @Column(name = "is_active")
    private Boolean isActive; // 활성화여부

    @Column(name = "start_date")
    private LocalDate startDate; // 판매시작일

    @Column(name = "end_date")
    private LocalDate endDate; // 판매종료일

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
}
