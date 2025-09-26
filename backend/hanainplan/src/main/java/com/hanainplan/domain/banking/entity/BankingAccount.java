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
    
    // 고객 CI (각 은행사와 호환)
    @Column(name = "customer_ci", nullable = false, length = 100)
    private String customerCi;
    
    // 계좌번호 (각 은행사 계좌번호 형식에 맞춤: 최대 20자)
    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;
    
    // 계좌명
    @Column(name = "account_name", nullable = false, length = 50)
    private String accountName;
    
    // 계좌 유형 (각 은행사와 호환: Integer 타입)
    @Column(name = "account_type", nullable = false)
    private Integer accountType;
    
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
    
    // 계좌 유형 상수 (각 은행사와 호환)
    public static class AccountType {
        public static final int CHECKING = 1;        // 수시입출금
        public static final int SAVINGS = 2;         // 예적금
        public static final int SECURITIES = 6;      // 수익증권
        public static final int INTEGRATED = 0;      // 통합계좌
        
        public static String getDescription(int accountType) {
            switch (accountType) {
                case CHECKING: return "수시입출금";
                case SAVINGS: return "예적금";
                case SECURITIES: return "수익증권";
                case INTEGRATED: return "통합계좌";
                default: return "기타";
            }
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
    
    // 계좌번호 생성 메서드 (각 은행사와 호환)
    public static String generateAccountNumber() {
        // 각 은행사 계좌번호 형식에 맞춤: 최대 20자
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 10000);
        return String.format("%d%04d", timestamp % 10000000000L, random);
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
