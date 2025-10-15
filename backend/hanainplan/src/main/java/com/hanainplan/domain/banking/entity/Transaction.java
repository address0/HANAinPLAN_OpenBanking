package com.hanainplan.domain.banking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_banking_transaction")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "transaction_number", nullable = false, unique = true, length = 36)
    private String transactionNumber;

    @Column(name = "from_account_id")
    private Long fromAccountId;

    @Column(name = "to_account_id")
    private Long toAccountId;

    @Column(name = "from_account_number", length = 20)
    private String fromAccountNumber;

    @Column(name = "to_account_number", length = 20)
    private String toAccountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_category", nullable = false)
    private TransactionCategory transactionCategory;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_direction", nullable = false)
    private TransactionDirection transactionDirection;

    @Column(name = "description", length = 200)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_status", nullable = false)
    private TransactionStatus transactionStatus;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "processed_date")
    private LocalDateTime processedDate;

    @Column(name = "reference_number", length = 50)
    private String referenceNumber;

    @Column(name = "memo", length = 500)
    private String memo;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum TransactionType {
        DEPOSIT("입금"),
        WITHDRAWAL("출금"),
        TRANSFER("이체"),
        AUTO_TRANSFER("자동이체"),
        INTEREST("이자"),
        FEE("수수료"),
        REFUND("환불"),
        REVERSAL("취소");

        private final String description;

        TransactionType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum TransactionCategory {
        SALARY("급여"),
        PENSION("연금"),
        SAVINGS("저축"),
        INVESTMENT("투자"),
        LOAN("대출"),
        INSURANCE("보험"),
        UTILITY("공과금"),
        SHOPPING("쇼핑"),
        FOOD("식비"),
        TRANSPORT("교통비"),
        MEDICAL("의료비"),
        EDUCATION("교육비"),
        ENTERTAINMENT("오락비"),
        INTEREST("이자"),
        TAX("세금"),
        OTHER("기타");

        private final String description;

        TransactionCategory(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum TransactionStatus {
        PENDING("대기중"),
        PROCESSING("처리중"),
        COMPLETED("완료"),
        FAILED("실패"),
        CANCELLED("취소"),
        REVERSED("역처리");

        private final String description;

        TransactionStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public static String generateTransactionNumber() {
        return java.util.UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    public void complete() {
        this.transactionStatus = TransactionStatus.COMPLETED;
        this.processedDate = LocalDateTime.now();
    }

    public void fail(String reason) {
        this.transactionStatus = TransactionStatus.FAILED;
        this.processedDate = LocalDateTime.now();
    }

    public void cancel() {
        this.transactionStatus = TransactionStatus.CANCELLED;
        this.processedDate = LocalDateTime.now();
    }

    public enum TransactionDirection {
        CREDIT("입금", "+"),
        DEBIT("출금", "-");

        private final String description;
        private final String symbol;

        TransactionDirection(String description, String symbol) {
            this.description = description;
            this.symbol = symbol;
        }

        public String getDescription() {
            return description;
        }

        public String getSymbol() {
            return symbol;
        }

        public static TransactionDirection fromTransactionType(TransactionType type, boolean isFromAccount) {
            switch (type) {
                case DEPOSIT:
                case INTEREST:
                case REFUND:
                    return CREDIT;
                case WITHDRAWAL:
                case FEE:
                    return DEBIT;
                case TRANSFER:
                case AUTO_TRANSFER:
                    return isFromAccount ? DEBIT : CREDIT;
                default:
                    return DEBIT;
            }
        }
    }
}