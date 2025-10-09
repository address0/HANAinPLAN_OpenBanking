package com.hanainplan.domain.banking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 정기예금 포트폴리오 엔티티
 * - 하나인플랜에서 사용자의 정기예금 가입 내역을 포트폴리오 형식으로 관리
 */
@Entity
@Table(name = "deposit_portfolio")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositPortfolio {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_id")
    private Long portfolioId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId; // 사용자 ID
    
    @Column(name = "customer_ci", length = 64)
    private String customerCi; // 고객 CI
    
    @Column(name = "bank_code", nullable = false, length = 20)
    private String bankCode; // 은행 코드 (HANA, KOOKMIN, SHINHAN)
    
    @Column(name = "bank_name", length = 50)
    private String bankName; // 은행명
    
    @Column(name = "product_code", nullable = false, length = 50)
    private String productCode; // 상품코드
    
    @Column(name = "product_name", length = 200)
    private String productName; // 상품명
    
    @Column(name = "subscription_id")
    private Long subscriptionId; // 은행사의 가입 ID (외부 참조)
    
    @Column(name = "subscription_date")
    private LocalDate subscriptionDate; // 가입일자
    
    @Column(name = "maturity_date")
    private LocalDate maturityDate; // 만기일자
    
    @Column(name = "contract_period")
    private Integer contractPeriod; // 계약기간 (개월)
    
    @Column(name = "maturity_period", length = 50)
    private String maturityPeriod; // 만기기간 문자열 (예: "12개월")
    
    @Column(name = "principal_amount", precision = 15, scale = 2)
    private BigDecimal principalAmount; // 원금
    
    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate; // 적용금리 (%)
    
    @Column(name = "expected_interest", precision = 15, scale = 2)
    private BigDecimal expectedInterest; // 예상 이자
    
    @Column(name = "maturity_amount", precision = 15, scale = 2)
    private BigDecimal maturityAmount; // 만기 예상 금액
    
    @Column(name = "status", length = 20)
    private String status; // 상태 (ACTIVE, MATURED, CANCELLED)
    
    @Column(name = "irp_account_number", length = 50)
    private String irpAccountNumber; // 출금한 IRP 계좌번호
    
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








