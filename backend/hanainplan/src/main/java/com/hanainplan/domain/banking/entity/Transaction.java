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
    
    // 거래 고유 번호 (UUID 형식)
    @Column(name = "transaction_number", nullable = false, unique = true, length = 36)
    private String transactionNumber;
    
    // 출금 계좌 ID (외래키 제약조건 제거)
    @Column(name = "from_account_id")
    private Long fromAccountId;
    
    // 입금 계좌 ID (외래키 제약조건 제거)
    @Column(name = "to_account_id")
    private Long toAccountId;
    
    // 출금 계좌번호 (동기화용)
    @Column(name = "from_account_number", length = 20)
    private String fromAccountNumber;
    
    // 입금 계좌번호 (동기화용)
    @Column(name = "to_account_number", length = 20)
    private String toAccountNumber;
    
    // 거래 유형
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;
    
    // 거래 분류
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_category", nullable = false)
    private TransactionCategory transactionCategory;
    
    // 거래 금액
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    // 거래 후 잔액
    @Column(name = "balance_after", nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfter;
    
    // 거래 방향 (CREDIT: 입금/+, DEBIT: 출금/-)
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_direction", nullable = false)
    private TransactionDirection transactionDirection;
    
    // 거래 설명
    @Column(name = "description", length = 200)
    private String description;
    
    // 거래 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_status", nullable = false)
    private TransactionStatus transactionStatus;
    
    // 거래 일시
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;
    
    // 처리 일시
    @Column(name = "processed_date")
    private LocalDateTime processedDate;
    
    // 참조 번호 (외부 시스템 연동 시)
    @Column(name = "reference_number", length = 50)
    private String referenceNumber;
    
    // 메모
    @Column(name = "memo", length = 500)
    private String memo;
    
    // 생성일시
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // 거래 유형 열거형
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
    
    // 거래 분류 열거형
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
        OTHER("기타");
        
        private final String description;
        
        TransactionCategory(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // 거래 상태 열거형
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
    
    // 거래번호 생성 메서드
    public static String generateTransactionNumber() {
        return java.util.UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
    
    // 거래 완료 처리
    public void complete() {
        this.transactionStatus = TransactionStatus.COMPLETED;
        this.processedDate = LocalDateTime.now();
    }
    
    // 거래 실패 처리
    public void fail(String reason) {
        this.transactionStatus = TransactionStatus.FAILED;
        this.processedDate = LocalDateTime.now();
    }
    
    // 거래 취소 처리
    public void cancel() {
        this.transactionStatus = TransactionStatus.CANCELLED;
        this.processedDate = LocalDateTime.now();
    }
    
    // 거래 방향 열거형
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
        
        // 거래 유형에 따른 방향 결정
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
