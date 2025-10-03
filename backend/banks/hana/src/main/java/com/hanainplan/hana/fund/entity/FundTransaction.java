package com.hanainplan.hana.fund.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 하나은행 펀드 거래 내역 엔티티
 */
@Entity
@Table(name = "hana_fund_transactions",
       indexes = {
           @Index(name = "idx_customer_ci", columnList = "customer_ci"),
           @Index(name = "idx_subscription_id", columnList = "subscription_id"),
           @Index(name = "idx_transaction_type", columnList = "transaction_type"),
           @Index(name = "idx_transaction_date", columnList = "transaction_date")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId; // 거래 ID

    @Column(name = "customer_ci", nullable = false, length = 64)
    private String customerCi; // 고객 CI

    @Column(name = "subscription_id", nullable = false)
    private Long subscriptionId; // 가입 ID

    @Column(name = "child_fund_cd", nullable = false, length = 16)
    private String childFundCd; // 펀드 클래스 코드

    @Column(name = "fund_name", nullable = false, length = 100)
    private String fundName; // 펀드명

    @Column(name = "class_code", length = 8)
    private String classCode; // 클래스 코드 (A/C/P)

    // 거래 정보
    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType; // BUY(매수), SELL(매도), DIVIDEND(분배금)

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate; // 거래일

    @Column(name = "settlement_date")
    private LocalDate settlementDate; // 결제일 (T+N)

    @Column(name = "nav", nullable = false, precision = 15, scale = 4)
    private BigDecimal nav; // 거래 기준가

    @Column(name = "units", nullable = false, precision = 15, scale = 6)
    private BigDecimal units; // 거래 좌수

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount; // 거래 금액

    // 수수료
    @Column(name = "fee", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal fee = BigDecimal.ZERO; // 수수료 (판매수수료/환매수수료)

    @Column(name = "fee_type", length = 20)
    private String feeType; // PURCHASE_FEE(매수수수료), REDEMPTION_FEE(환매수수료)

    // 손익 정보 (매도 시)
    @Column(name = "profit", precision = 15, scale = 2)
    private BigDecimal profit; // 실현 손익

    @Column(name = "profit_rate", precision = 10, scale = 4)
    private BigDecimal profitRate; // 실현 수익률 (%)

    // IRP 계좌 정보
    @Column(name = "irp_account_number", length = 50)
    private String irpAccountNumber; // IRP 계좌번호

    @Column(name = "irp_balance_before", precision = 15, scale = 2)
    private BigDecimal irpBalanceBefore; // 거래 전 IRP 잔액

    @Column(name = "irp_balance_after", precision = 15, scale = 2)
    private BigDecimal irpBalanceAfter; // 거래 후 IRP 잔액

    // 메타 정보
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "COMPLETED"; // PENDING(대기), COMPLETED(완료), CANCELLED(취소)

    @Column(name = "note", length = 200)
    private String note; // 비고

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * 매수 거래 생성
     */
    public static FundTransaction createPurchase(
            String customerCi,
            Long subscriptionId,
            String childFundCd,
            String fundName,
            String classCode,
            LocalDate transactionDate,
            LocalDate settlementDate,
            BigDecimal nav,
            BigDecimal units,
            BigDecimal amount,
            BigDecimal fee,
            String irpAccountNumber,
            BigDecimal irpBalanceBefore,
            BigDecimal irpBalanceAfter
    ) {
        return FundTransaction.builder()
                .customerCi(customerCi)
                .subscriptionId(subscriptionId)
                .childFundCd(childFundCd)
                .fundName(fundName)
                .classCode(classCode)
                .transactionType("BUY")
                .transactionDate(transactionDate)
                .settlementDate(settlementDate)
                .nav(nav)
                .units(units)
                .amount(amount)
                .fee(fee)
                .feeType("PURCHASE_FEE")
                .irpAccountNumber(irpAccountNumber)
                .irpBalanceBefore(irpBalanceBefore)
                .irpBalanceAfter(irpBalanceAfter)
                .status("COMPLETED")
                .note("펀드 매수")
                .build();
    }

    /**
     * 매도 거래 생성
     */
    public static FundTransaction createRedemption(
            String customerCi,
            Long subscriptionId,
            String childFundCd,
            String fundName,
            String classCode,
            LocalDate transactionDate,
            LocalDate settlementDate,
            BigDecimal nav,
            BigDecimal units,
            BigDecimal amount,
            BigDecimal fee,
            BigDecimal profit,
            BigDecimal profitRate,
            String irpAccountNumber,
            BigDecimal irpBalanceBefore,
            BigDecimal irpBalanceAfter
    ) {
        return FundTransaction.builder()
                .customerCi(customerCi)
                .subscriptionId(subscriptionId)
                .childFundCd(childFundCd)
                .fundName(fundName)
                .classCode(classCode)
                .transactionType("SELL")
                .transactionDate(transactionDate)
                .settlementDate(settlementDate)
                .nav(nav)
                .units(units)
                .amount(amount)
                .fee(fee)
                .feeType("REDEMPTION_FEE")
                .profit(profit)
                .profitRate(profitRate)
                .irpAccountNumber(irpAccountNumber)
                .irpBalanceBefore(irpBalanceBefore)
                .irpBalanceAfter(irpBalanceAfter)
                .status("COMPLETED")
                .note("펀드 매도")
                .build();
    }

    /**
     * 매수 여부 확인
     */
    public boolean isPurchase() {
        return "BUY".equals(transactionType);
    }

    /**
     * 매도 여부 확인
     */
    public boolean isRedemption() {
        return "SELL".equals(transactionType);
    }

    /**
     * 수익 여부 확인 (매도 시)
     */
    public boolean isProfitable() {
        return profit != null && profit.compareTo(BigDecimal.ZERO) > 0;
    }
}

