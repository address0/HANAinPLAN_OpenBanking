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

    @Column(name = "min_contract_period")
    private Integer minContractPeriod; // 가입기간(최소)

    @Column(name = "max_contract_period")
    private Integer maxContractPeriod; // 가입기간(최대)

    @Column(name = "contract_period_unit", length = 10)
    private String contractPeriodUnit; // 가입기간 단위 (개월, 년)

    @Column(name = "subscription_target", length = 100)
    private String subscriptionTarget; // 가입대상

    @Column(name = "subscription_amount")
    private BigDecimal subscriptionAmount; // 가입금액

    @Column(name = "product_category", length = 50)
    private String productCategory; // 상품유형(선택)

    @Column(name = "interest_payment", length = 100)
    private String interestPayment; // 이자지급

    @Column(name = "tax_benefit", length = 200)
    private String taxBenefit; // 세제혜택(선택)

    @Column(name = "partial_withdrawal", length = 100)
    private String partialWithdrawal; // 일부해지

    @Column(name = "cancellation_penalty", length = 200)
    private String cancellationPenalty; // 해지 시 불이익(선택)

    @Column(name = "description", length = 1000)
    private String description; // 상품 설명

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
