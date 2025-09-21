package com.hanainplan.kookmin.product.entity;

import com.hanainplan.kookmin.account.entity.Account;
import com.hanainplan.kookmin.user.entity.Customer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "kookmin_product_subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long subscriptionId; // 가입pk

    @Column(name = "customer_ci", nullable = false, length = 64)
    private String customerCi; // 고객CI

    @Column(name = "product_code", nullable = false, length = 20)
    private String productCode; // 상품코드

    @Column(name = "account_number", nullable = false, length = 20)
    private String accountNumber; // 계좌번호

    @Column(name = "status", nullable = false, length = 20)
    private String status; // 상태 (ACTIVE, INACTIVE, MATURED, CANCELLED)

    @Column(name = "subscription_date", nullable = false)
    private LocalDate subscriptionDate; // 가입일자

    @Column(name = "maturity_date")
    private LocalDate maturityDate; // 만기일자

    @Column(name = "contract_period")
    private Integer contractPeriod; // 약정기간 (개월)

    @Column(name = "rate_type", length = 10)
    private String rateType; // 고정/변동여부 (FIXED, VARIABLE)

    @Column(name = "base_rate", precision = 5, scale = 4)
    private BigDecimal baseRate; // 기준금리

    @Column(name = "preferential_rate", precision = 5, scale = 4)
    private BigDecimal preferentialRate; // 우대금리(선택)

    @Column(name = "final_applied_rate", precision = 5, scale = 4)
    private BigDecimal finalAppliedRate; // 최종 적용금리

    @Column(name = "preferential_reason", length = 200)
    private String preferentialReason; // 우대사유(선택)

    @Column(name = "interest_calculation_basis", length = 50)
    private String interestCalculationBasis; // 이자 계산기준

    @Column(name = "interest_payment_method", length = 20)
    private String interestPaymentMethod; // 이자 지급방식 (MATURITY, MONTHLY, QUARTERLY)

    @Column(name = "interest_type", length = 10)
    private String interestType; // 단리/복리여부 (SIMPLE, COMPOUND)

    @Column(name = "contract_principal", precision = 15, scale = 2)
    private BigDecimal contractPrincipal; // 약정 원금

    @Column(name = "current_balance", precision = 15, scale = 2)
    private BigDecimal currentBalance; // 현재 잔액

    @Column(name = "unpaid_interest", precision = 15, scale = 2)
    private BigDecimal unpaidInterest; // 미지급 이자(누계)

    @Column(name = "unpaid_tax", precision = 15, scale = 2)
    private BigDecimal unpaidTax; // 미지급 세액(누계, 선택)

    @Column(name = "last_interest_calculation_date")
    private LocalDate lastInterestCalculationDate; // 마지막 이자계산 일자

    @Column(name = "next_interest_payment_date")
    private LocalDate nextInterestPaymentDate; // 다음 이자지급 예정일

    @Column(name = "branch_name", length = 100)
    private String branchName; // 취급점

    @Column(name = "monthly_payment_amount", precision = 15, scale = 2)
    private BigDecimal monthlyPaymentAmount; // 회차당 납입액(선택)

    @Column(name = "monthly_payment_day")
    private Integer monthlyPaymentDay; // 매월 납입일(선택)

    @Column(name = "total_installments")
    private Integer totalInstallments; // 약정 총 회차(선택)

    @Column(name = "completed_installments")
    private Integer completedInstallments; // 납입 완료 회차(선택)

    @Column(name = "missed_installments")
    private Integer missedInstallments; // 미납 회차(선택)

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 고객과의 관계 (다대일)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_ci", referencedColumnName = "ci", insertable = false, updatable = false)
    private Customer customer;

    // 계좌와의 관계 (다대일)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_number", referencedColumnName = "account_number", insertable = false, updatable = false)
    private Account account;

    // 금융상품과의 관계 (다대일)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_code", referencedColumnName = "product_code", insertable = false, updatable = false)
    private FinancialProduct financialProduct;

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
