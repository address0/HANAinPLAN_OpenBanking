package com.hanainplan.hana.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 하나은행 예금 가입 정보 엔티티
 */
@Entity
@Table(name = "hana_deposit_subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long subscriptionId; // 가입 ID

    @Column(name = "customer_ci", nullable = false, length = 64)
    private String customerCi; // 고객 CI

    @Column(name = "account_number", nullable = false, length = 20)
    private String accountNumber; // 계좌번호

    @Column(name = "status", nullable = false, length = 20)
    private String status; // 상태 (ACTIVE, MATURED, CLOSED)

    @Column(name = "subscription_date", nullable = false)
    private LocalDate subscriptionDate; // 가입일

    @Column(name = "maturity_date")
    private LocalDate maturityDate; // 만기일

    @Column(name = "contract_period")
    private Integer contractPeriod; // 계약기간 (product_type=2: 일단위, 그외: 개월단위)

    @Column(name = "product_type", nullable = false)
    @Builder.Default
    private Integer productType = 0; // 상품유형 (0:일반, 1:디폴트옵션, 2:일단위)

    @Column(name = "bank_name", nullable = false, length = 50)
    @Builder.Default
    private String bankName = "하나은행"; // 은행명

    @Column(name = "bank_code", nullable = false, length = 10)
    @Builder.Default
    private String bankCode = "HANA"; // 은행 코드

    @Column(name = "deposit_code", nullable = false, length = 20)
    private String depositCode; // 예금 상품 코드

    @Column(name = "rate", precision = 5, scale = 4)
    private BigDecimal rate; // 적용 금리

    @Column(name = "current_balance", precision = 15, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal currentBalance = BigDecimal.ZERO; // 현재 잔액

    @Column(name = "unpaid_interest", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal unpaidInterest = BigDecimal.ZERO; // 미지급 이자

    @Column(name = "last_interest_calculation_date")
    private LocalDate lastInterestCalculationDate; // 최종 이자 계산일

    @Column(name = "next_interest_payment_date")
    private LocalDate nextInterestPaymentDate; // 다음 이자 지급일

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

    /**
     * 이자 지급 처리
     */
    public void processInterestPayment(BigDecimal interestAmount) {
        this.currentBalance = this.currentBalance.add(interestAmount);
        this.unpaidInterest = BigDecimal.ZERO;
        this.lastInterestCalculationDate = LocalDate.now();
    }

    /**
     * 이자 계산
     */
    public void calculateInterest(BigDecimal interestAmount) {
        this.unpaidInterest = this.unpaidInterest.add(interestAmount);
        this.lastInterestCalculationDate = LocalDate.now();
    }

    /**
     * 만기 여부 확인
     */
    public boolean isMatured() {
        return maturityDate != null && !maturityDate.isAfter(LocalDate.now());
    }

    /**
     * 활성 상태 확인
     */
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
}






