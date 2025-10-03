package com.hanainplan.domain.fund.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 펀드 포트폴리오 엔티티
 * - 하나인플랜에서 사용자의 펀드 가입 내역을 통합 포트폴리오 형식으로 관리
 * - 여러 은행의 펀드를 하나의 포트폴리오로 조회 가능
 */
@Entity
@Table(name = "fund_portfolio",
       indexes = {
           @Index(name = "idx_user_id", columnList = "user_id"),
           @Index(name = "idx_customer_ci", columnList = "customer_ci"),
           @Index(name = "idx_fund_code", columnList = "fund_code"),
           @Index(name = "idx_status", columnList = "status")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundPortfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "portfolio_id")
    private Long portfolioId;

    @Column(name = "user_id", nullable = false)
    private Long userId; // 사용자 ID

    @Column(name = "customer_ci", nullable = false, length = 64)
    private String customerCi; // 고객 CI

    // 은행 정보
    @Column(name = "bank_code", nullable = false, length = 20)
    private String bankCode; // 은행 코드 (HANA, KOOKMIN, SHINHAN)

    @Column(name = "bank_name", length = 50)
    private String bankName; // 은행명

    // 펀드 정보
    @Column(name = "fund_code", nullable = false, length = 20)
    private String fundCode; // 펀드 코드

    @Column(name = "fund_name", nullable = false, length = 100)
    private String fundName; // 펀드명

    @Column(name = "fund_type", nullable = false, length = 50)
    private String fundType; // 펀드 유형

    @Column(name = "risk_level", nullable = false, length = 20)
    private String riskLevel; // 위험등급

    // 매수 정보
    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate; // 매수일

    @Column(name = "purchase_nav", nullable = false, precision = 15, scale = 4)
    private BigDecimal purchaseNav; // 매수 기준가

    @Column(name = "purchase_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal purchaseAmount; // 매수 금액 (원금)

    @Column(name = "purchase_fee", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal purchaseFee = BigDecimal.ZERO; // 매수 수수료

    @Column(name = "purchase_units", nullable = false, precision = 15, scale = 6)
    private BigDecimal purchaseUnits; // 매수 좌수

    // 현재 보유 정보
    @Column(name = "current_units", nullable = false, precision = 15, scale = 6)
    private BigDecimal currentUnits; // 현재 보유 좌수

    @Column(name = "current_nav", precision = 15, scale = 4)
    private BigDecimal currentNav; // 현재 기준가

    @Column(name = "current_value", precision = 15, scale = 2)
    private BigDecimal currentValue; // 현재 평가금액 (보유좌수 × 현재기준가)

    // 수익 정보
    @Column(name = "total_return", precision = 15, scale = 2)
    private BigDecimal totalReturn; // 평가손익 (평가금액 - 원금)

    @Column(name = "return_rate", precision = 10, scale = 4)
    private BigDecimal returnRate; // 수익률 (%)

    // 수수료 누적
    @Column(name = "accumulated_fees", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal accumulatedFees = BigDecimal.ZERO; // 누적 수수료 (판매수수료 + 운용보수 등)

    // IRP 연계
    @Column(name = "irp_account_number", length = 50)
    private String irpAccountNumber; // IRP 계좌번호

    // 외부 참조 (각 은행사의 가입 ID)
    @Column(name = "subscription_id")
    private Long subscriptionId; // 은행사의 가입 ID (외부 참조)

    // 상태
    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "ACTIVE"; // 상태 (ACTIVE: 보유중, SOLD: 전량매도, PARTIAL_SOLD: 일부매도)

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
     * 평가금액 및 수익률 업데이트
     */
    public void updateValuation(BigDecimal newNav) {
        this.currentNav = newNav;
        
        // 현재 평가금액 = 보유좌수 × 현재기준가
        this.currentValue = this.currentUnits
            .multiply(newNav)
            .setScale(2, RoundingMode.DOWN);
        
        // 평가손익 = 평가금액 - 원금
        this.totalReturn = this.currentValue.subtract(this.purchaseAmount);
        
        // 수익률 = (평가손익 / 원금) × 100
        if (this.purchaseAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.returnRate = this.totalReturn
                .divide(this.purchaseAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }
    }

    /**
     * 추가 매수 처리
     */
    public void addPurchase(BigDecimal amount, BigDecimal nav, BigDecimal fee, BigDecimal units) {
        this.purchaseAmount = this.purchaseAmount.add(amount);
        this.purchaseFee = this.purchaseFee.add(fee);
        this.purchaseUnits = this.purchaseUnits.add(units);
        this.currentUnits = this.currentUnits.add(units);
        this.accumulatedFees = this.accumulatedFees.add(fee);
        
        // 평균 매수 기준가 재계산
        BigDecimal netAmount = this.purchaseAmount.subtract(this.purchaseFee);
        if (this.purchaseUnits.compareTo(BigDecimal.ZERO) > 0) {
            this.purchaseNav = netAmount.divide(this.purchaseUnits, 4, RoundingMode.HALF_UP);
        }
    }

    /**
     * 매도 처리
     */
    public void sellUnits(BigDecimal soldUnits) {
        this.currentUnits = this.currentUnits.subtract(soldUnits);
        
        if (this.currentUnits.compareTo(BigDecimal.ZERO) <= 0) {
            this.status = "SOLD"; // 전량 매도
            this.currentUnits = BigDecimal.ZERO;
        } else {
            this.status = "PARTIAL_SOLD"; // 일부 매도
        }
    }

    /**
     * 활성 상태 확인
     */
    public boolean isActive() {
        return "ACTIVE".equals(status) || "PARTIAL_SOLD".equals(status);
    }

    /**
     * 수익 여부 확인
     */
    public boolean isProfitable() {
        return totalReturn != null && totalReturn.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 수익률 문자열 반환 (예: "+12.34%")
     */
    public String getReturnRateString() {
        if (returnRate == null) {
            return "0.00%";
        }
        String sign = returnRate.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
        return String.format("%s%.2f%%", sign, returnRate);
    }
}

