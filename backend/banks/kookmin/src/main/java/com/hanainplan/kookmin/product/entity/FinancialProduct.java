package com.hanainplan.kookmin.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "kookmin_financial_products")
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
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "deposit_type", length = 50)
    private String depositType;

    @Column(name = "min_contract_period")
    private Integer minContractPeriod;

    @Column(name = "max_contract_period")
    private Integer maxContractPeriod;

    @Column(name = "contract_period_unit", length = 10)
    private String contractPeriodUnit;

    @Column(name = "subscription_target", length = 100)
    private String subscriptionTarget;

    @Column(name = "subscription_amount", length = 50)
    private String subscriptionAmount;

    @Column(name = "product_category", length = 50)
    private String productCategory;

    @Column(name = "interest_payment", length = 100)
    private String interestPayment;

    @Column(name = "tax_benefit", length = 200)
    private String taxBenefit;

    @Column(name = "partial_withdrawal", length = 100)
    private String partialWithdrawal;

    @Column(name = "depositor_protection", length = 10)
    private String depositorProtection;

    @Column(name = "transaction_method", length = 200)
    private String transactionMethod;

    @Column(name = "precautions", length = 1000)
    private String precautions;

    @Column(name = "contract_cancellation_right", length = 200)
    private String contractCancellationRight;

    @Column(name = "cancellation_penalty", length = 200)
    private String cancellationPenalty;

    @Column(name = "payment_restrictions", length = 200)
    private String paymentRestrictions;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

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