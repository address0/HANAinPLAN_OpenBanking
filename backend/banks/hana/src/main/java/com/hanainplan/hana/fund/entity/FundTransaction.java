package com.hanainplan.hana.fund.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private Long transactionId;

    @Column(name = "customer_ci", nullable = false, length = 64)
    private String customerCi;

    @Column(name = "subscription_id", nullable = false)
    private Long subscriptionId;

    @Column(name = "child_fund_cd", nullable = false, length = 16)
    private String childFundCd;

    @Column(name = "fund_name", nullable = false, length = 100)
    private String fundName;

    @Column(name = "class_code", length = 8)
    private String classCode;

    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "settlement_date")
    private LocalDate settlementDate;

    @Column(name = "nav", nullable = false, precision = 15, scale = 4)
    private BigDecimal nav;

    @Column(name = "units", nullable = false, precision = 15, scale = 6)
    private BigDecimal units;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "fee", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal fee = BigDecimal.ZERO;

    @Column(name = "fee_type", length = 20)
    private String feeType;

    @Column(name = "profit", precision = 15, scale = 2)
    private BigDecimal profit;

    @Column(name = "profit_rate", precision = 10, scale = 4)
    private BigDecimal profitRate;

    @Column(name = "irp_account_number", length = 50)
    private String irpAccountNumber;

    @Column(name = "irp_balance_before", precision = 15, scale = 2)
    private BigDecimal irpBalanceBefore;

    @Column(name = "irp_balance_after", precision = 15, scale = 2)
    private BigDecimal irpBalanceAfter;

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "COMPLETED";

    @Column(name = "note", length = 200)
    private String note;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

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

    public boolean isPurchase() {
        return "BUY".equals(transactionType);
    }

    public boolean isRedemption() {
        return "SELL".equals(transactionType);
    }

    public boolean isProfitable() {
        return profit != null && profit.compareTo(BigDecimal.ZERO) > 0;
    }
}