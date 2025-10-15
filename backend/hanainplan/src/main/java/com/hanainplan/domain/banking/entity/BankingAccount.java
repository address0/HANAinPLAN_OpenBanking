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

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "customer_ci", nullable = false, length = 100)
    private String customerCi;

    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @Column(name = "account_name", nullable = false, length = 50)
    private String accountName;

    @Column(name = "account_type", nullable = false)
    private Integer accountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus;

    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "opened_date", nullable = false)
    private LocalDateTime openedDate;

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "interest_rate", precision = 5, scale = 4)
    private BigDecimal interestRate;

    @Column(name = "minimum_balance", precision = 15, scale = 2)
    private BigDecimal minimumBalance;

    @Column(name = "credit_limit", precision = 15, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "purpose", length = 50)
    private String purpose;

    @Column(name = "monthly_deposit_amount", precision = 15, scale = 2)
    private BigDecimal monthlyDepositAmount;

    @Column(name = "deposit_period")
    private Integer depositPeriod;

    @Column(name = "interest_payment_method", length = 10)
    private String interestPaymentMethod;

    @Column(name = "account_password", length = 255)
    private String accountPassword;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static class AccountType {
        public static final int CHECKING = 1;
        public static final int SAVINGS = 2;
        public static final int SECURITIES = 6;
        public static final int INTEGRATED = 0;

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

    public static String generateAccountNumber() {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 10000);
        return String.format("%d%04d", timestamp % 10000000000L, random);
    }

    public boolean hasSufficientBalance(BigDecimal amount) {
        return this.balance.compareTo(amount) >= 0;
    }

    public void updateBalance(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }
}