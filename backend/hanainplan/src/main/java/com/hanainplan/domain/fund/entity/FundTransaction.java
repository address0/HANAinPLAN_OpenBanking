package com.hanainplan.domain.fund.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fund_transactions",
       indexes = {
           @Index(name = "idx_portfolio_id", columnList = "portfolio_id"),
           @Index(name = "idx_user_id", columnList = "user_id"),
           @Index(name = "idx_transaction_date", columnList = "transaction_date"),
           @Index(name = "idx_transaction_type", columnList = "transaction_type")
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

    @Column(name = "portfolio_id", nullable = false)
    private Long portfolioId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "fund_code", nullable = false, length = 20)
    private String fundCode;

    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

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

    @Column(name = "balance_units", nullable = false, precision = 15, scale = 6)
    private BigDecimal balanceUnits;

    @Column(name = "irp_account_number", length = 50)
    private String irpAccountNumber;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public boolean isBuyTransaction() {
        return "BUY".equals(transactionType);
    }

    public boolean isSellTransaction() {
        return "SELL".equals(transactionType);
    }

    public BigDecimal getNetAmount() {
        if (isBuyTransaction()) {
            return amount.subtract(fee);
        } else {
            return amount.add(fee);
        }
    }

    public String getTransactionTypeKorean() {
        return switch (transactionType) {
            case "BUY" -> "매수";
            case "SELL" -> "매도";
            default -> "알 수 없음";
        };
    }
}