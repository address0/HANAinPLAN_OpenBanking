package com.hanainplan.domain.banking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_banking_account")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class BankingAccount {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;
    
    // 사용자 ID (외래키 제약조건 제거)
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    // 계좌번호 (하나은행 계좌번호 형식: 123-456-789012)
    @Column(name = "account_number", nullable = false, unique = true, length = 14)
    private String accountNumber;
    
    // 계좌명
    @Column(name = "account_name", nullable = false, length = 50)
    private String accountName;
    
    // 계좌 유형
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;
    
    // 계좌 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus;
    
    // 잔액
    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;
    
    // 통화 코드 (기본: KRW)
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;
    
    // 계좌 개설일
    @Column(name = "opened_date", nullable = false)
    private LocalDateTime openedDate;
    
    // 계좌 만료일 (정기예금 등)
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    
    // 이자율 (예금 상품의 경우)
    @Column(name = "interest_rate", precision = 5, scale = 4)
    private BigDecimal interestRate;
    
    // 최소 잔액 (마이너스 통장 등)
    @Column(name = "minimum_balance", precision = 15, scale = 2)
    private BigDecimal minimumBalance;
    
    // 한도 금액 (대출 계좌 등)
    @Column(name = "credit_limit", precision = 15, scale = 2)
    private BigDecimal creditLimit;
    
    // 계좌 설명
    @Column(name = "description", length = 200)
    private String description;
    
    // 계좌 용도 (적금/정기예금의 경우)
    @Column(name = "purpose", length = 50)
    private String purpose;
    
    // 월 적립 금액 (적금의 경우)
    @Column(name = "monthly_deposit_amount", precision = 15, scale = 2)
    private BigDecimal monthlyDepositAmount;
    
    // 적립/예치 기간 (개월)
    @Column(name = "deposit_period")
    private Integer depositPeriod;
    
    // 이자 지급 방법 (AUTO, MANUAL)
    @Column(name = "interest_payment_method", length = 10)
    private String interestPaymentMethod;
    
    // 계좌 비밀번호 (암호화된 형태로 저장)
    @Column(name = "account_password", length = 255)
    private String accountPassword;
    
    // 생성일시
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // 수정일시
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 계좌 유형 열거형
    public enum AccountType {
        CHECKING("입출금통장"),
        SAVINGS("저축예금"),
        TIME_DEPOSIT("정기예금"),
        FIXED_DEPOSIT("정기적금"),
        LOAN("대출계좌"),
        CREDIT("신용계좌");
        
        private final String description;
        
        AccountType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // 계좌 상태 열거형
    public enum AccountStatus {
        ACTIVE("활성"),
        INACTIVE("비활성"),
        SUSPENDED("정지"),
        CLOSED("해지"),
        FROZEN("동결");
        
        private final String description;
        
        AccountStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // 계좌번호 생성 메서드
    public static String generateAccountNumber() {
        // 하나은행 계좌번호 형식: 123-456-789012
        int first = (int) (Math.random() * 900) + 100; // 100-999
        int second = (int) (Math.random() * 900) + 100; // 100-999
        int third = (int) (Math.random() * 900000) + 100000; // 100000-999999
        return String.format("%03d-%03d-%06d", first, second, third);
    }
    
    // 잔액 충분 여부 확인
    public boolean hasSufficientBalance(BigDecimal amount) {
        return this.balance.compareTo(amount) >= 0;
    }
    
    // 잔액 업데이트
    public void updateBalance(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }
}
