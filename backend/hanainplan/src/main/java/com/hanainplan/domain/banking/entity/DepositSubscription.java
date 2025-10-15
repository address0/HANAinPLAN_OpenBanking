package com.hanainplan.domain.banking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_deposit_subscription")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long subscriptionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "customer_ci", nullable = false, length = 100)
    private String customerCi;

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

    @Column(name = "product_type", nullable = false)
    @Builder.Default
    private Integer productType = 0;

    @Column(name = "bank_name", nullable = false, length = 50)
    private String bankName;

    @Column(name = "bank_code", nullable = false, length = 10)
    private String bankCode;

    @Column(name = "deposit_code", nullable = false, length = 20)
    private String depositCode;

    @Column(name = "rate", precision = 5, scale = 4)
    private BigDecimal rate;

    @Column(name = "current_balance", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(name = "unpaid_interest", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal unpaidInterest = BigDecimal.ZERO;

    @Column(name = "gross_interest", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal grossInterest = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "net_interest", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal netInterest = BigDecimal.ZERO;

    @Column(name = "last_interest_calculation_date")
    private LocalDate lastInterestCalculationDate;

    @Column(name = "next_interest_payment_date")
    private LocalDate nextInterestPaymentDate;

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

    public void processInterestPayment(BigDecimal interestAmount) {
        this.currentBalance = this.currentBalance.add(interestAmount);
        this.unpaidInterest = BigDecimal.ZERO;
        this.lastInterestCalculationDate = LocalDate.now();
    }

    public void calculateInterest(BigDecimal interestAmount) {
        this.unpaidInterest = this.unpaidInterest.add(interestAmount);
        this.lastInterestCalculationDate = LocalDate.now();
    }

    public boolean isMatured() {
        return maturityDate != null && !maturityDate.isAfter(LocalDate.now());
    }

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
}