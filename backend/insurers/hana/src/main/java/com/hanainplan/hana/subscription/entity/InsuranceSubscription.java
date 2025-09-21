package com.hanainplan.hana.subscription.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "hana_insurance_subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long subscriptionId; // 가입ID (PK)

    @Column(name = "customer_ci", nullable = false, length = 64)
    private String customerCi; // 고객CI

    @Column(name = "product_code", nullable = false, length = 20)
    private String productCode; // 상품코드

    @Column(name = "product_type", nullable = false, length = 20)
    private String productType; // 상품유형 (GENERAL, PENSION)

    @Column(name = "policy_number", unique = true, nullable = false, length = 30)
    private String policyNumber; // 증권번호

    @Column(name = "subscription_date", nullable = false)
    private LocalDate subscriptionDate; // 가입일자

    @Column(name = "maturity_date")
    private LocalDate maturityDate; // 만기일자

    @Column(name = "premium_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal premiumAmount; // 보험료

    @Column(name = "payment_frequency", length = 20)
    private String paymentFrequency; // 납입주기 (MONTHLY, QUARTERLY, YEARLY, SINGLE)

    @Column(name = "coverage_amount", precision = 15, scale = 2)
    private BigDecimal coverageAmount; // 보장금액

    @Column(name = "beneficiary_name", length = 100)
    private String beneficiaryName; // 수익자명

    @Column(name = "beneficiary_relation", length = 20)
    private String beneficiaryRelation; // 수익자 관계

    @Column(name = "payment_method", length = 20)
    private String paymentMethod; // 납입방법 (BANK_TRANSFER, CREDIT_CARD, CASH)

    @Column(name = "payment_account", length = 50)
    private String paymentAccount; // 납입계좌

    @Column(name = "subscription_status", nullable = false, length = 20)
    private String subscriptionStatus; // 가입상태 (ACTIVE, SUSPENDED, TERMINATED, EXPIRED)

    @Column(name = "risk_assessment", length = 20)
    private String riskAssessment; // 위험평가 (LOW, MEDIUM, HIGH)

    @Column(name = "medical_exam_required")
    private Boolean medicalExamRequired; // 의료검진 필요여부

    @Column(name = "medical_exam_date")
    private LocalDate medicalExamDate; // 의료검진일

    @Column(name = "medical_exam_result", length = 20)
    private String medicalExamResult; // 의료검진 결과 (PASS, FAIL, PENDING)

    @Column(name = "agent_code", length = 20)
    private String agentCode; // 설계사코드

    @Column(name = "branch_code", length = 20)
    private String branchCode; // 지점코드

    @Column(name = "special_conditions", length = 500)
    private String specialConditions; // 특별조건

    @Column(name = "exclusions", length = 500)
    private String exclusions; // 면책사항

    @Column(name = "waiting_period")
    private Integer waitingPeriod; // 대기기간 (일)

    @Column(name = "grace_period")
    private Integer gracePeriod; // 유예기간 (일)

    @Column(name = "renewal_option")
    private Boolean renewalOption; // 갱신옵션

    @Column(name = "automatic_renewal")
    private Boolean automaticRenewal; // 자동갱신

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
