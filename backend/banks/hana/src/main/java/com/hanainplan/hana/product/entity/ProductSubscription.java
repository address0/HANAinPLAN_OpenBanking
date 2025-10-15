package com.hanainplan.hana.product.entity;

import com.hanainplan.hana.account.entity.Account;
import com.hanainplan.hana.user.entity.Customer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "hana_product_subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long subscriptionId;

    @Column(name = "customer_ci", nullable = false, length = 64)
    private String customerCi;

    @Column(name = "product_code", nullable = false, length = 20)
    private String productCode;

    @Column(name = "account_number", nullable = false, length = 20)
    private String accountNumber;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "subscription_date", nullable = false)
    private LocalDate subscriptionDate;

    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    @Column(name = "contract_period")
    private Integer contractPeriod;

    @Column(name = "maturity_period", length = 50)
    private String maturityPeriod;

    @Column(name = "rate_type", length = 10)
    private String rateType;

    @Column(name = "base_rate", precision = 5, scale = 4)
    private BigDecimal baseRate;

    @Column(name = "preferential_rate", precision = 5, scale = 4)
    private BigDecimal preferentialRate;

    @Column(name = "final_applied_rate", precision = 5, scale = 4)
    private BigDecimal finalAppliedRate;

    @Column(name = "preferential_reason", length = 200)
    private String preferentialReason;

    @Column(name = "interest_calculation_basis", length = 50)
    private String interestCalculationBasis;

    @Column(name = "interest_payment_method", length = 20)
    private String interestPaymentMethod;

    @Column(name = "interest_type", length = 10)
    private String interestType;

    @Column(name = "contract_principal", precision = 15, scale = 2)
    private BigDecimal contractPrincipal;

    @Column(name = "current_balance", precision = 15, scale = 2)
    private BigDecimal currentBalance;

    @Column(name = "unpaid_interest", precision = 15, scale = 2)
    private BigDecimal unpaidInterest;

    @Column(name = "unpaid_tax", precision = 15, scale = 2)
    private BigDecimal unpaidTax;

    @Column(name = "last_interest_calculation_date")
    private LocalDate lastInterestCalculationDate;

    @Column(name = "next_interest_payment_date")
    private LocalDate nextInterestPaymentDate;

    @Column(name = "branch_name", length = 100)
    private String branchName;

    @Column(name = "monthly_payment_amount", precision = 15, scale = 2)
    private BigDecimal monthlyPaymentAmount;

    @Column(name = "monthly_payment_day")
    private Integer monthlyPaymentDay;

    @Column(name = "total_installments")
    private Integer totalInstallments;

    @Column(name = "completed_installments")
    private Integer completedInstallments;

    @Column(name = "missed_installments")
    private Integer missedInstallments;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_ci", referencedColumnName = "ci", insertable = false, updatable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_number", referencedColumnName = "account_number", insertable = false, updatable = false)
    private Account account;

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