package com.hanainplan.domain.fund.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 펀드 거래 내역 엔티티
 * - 펀드 매수/매도 거래 내역 관리
 */
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
    private Long portfolioId; // 포트폴리오 ID

    @Column(name = "user_id", nullable = false)
    private Long userId; // 사용자 ID

    @Column(name = "fund_code", nullable = false, length = 20)
    private String fundCode; // 펀드 코드

    // 거래 정보
    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType; // 거래 유형 (BUY: 매수, SELL: 매도)

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate; // 거래일시

    @Column(name = "settlement_date")
    private LocalDate settlementDate; // 결제일 (보통 T+2)

    // 매수/매도 정보
    @Column(name = "nav", nullable = false, precision = 15, scale = 4)
    private BigDecimal nav; // 거래 시점 기준가

    @Column(name = "units", nullable = false, precision = 15, scale = 6)
    private BigDecimal units; // 거래 좌수

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount; // 거래 금액

    @Column(name = "fee", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal fee = BigDecimal.ZERO; // 거래 수수료

    // 잔고 정보 (거래 후)
    @Column(name = "balance_units", nullable = false, precision = 15, scale = 6)
    private BigDecimal balanceUnits; // 거래 후 보유 좌수

    // IRP 연계
    @Column(name = "irp_account_number", length = 50)
    private String irpAccountNumber; // IRP 계좌번호

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // 거래 설명

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * 매수 거래 여부 확인
     */
    public boolean isBuyTransaction() {
        return "BUY".equals(transactionType);
    }

    /**
     * 매도 거래 여부 확인
     */
    public boolean isSellTransaction() {
        return "SELL".equals(transactionType);
    }

    /**
     * 실투자금액 계산 (매수 시: 금액 - 수수료, 매도 시: 금액 + 수수료)
     */
    public BigDecimal getNetAmount() {
        if (isBuyTransaction()) {
            return amount.subtract(fee);
        } else {
            return amount.add(fee);
        }
    }

    /**
     * 거래 유형 한글 반환
     */
    public String getTransactionTypeKorean() {
        return switch (transactionType) {
            case "BUY" -> "매수";
            case "SELL" -> "매도";
            default -> "알 수 없음";
        };
    }
}

