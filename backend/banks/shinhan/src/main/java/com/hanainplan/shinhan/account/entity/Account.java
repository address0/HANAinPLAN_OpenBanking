package com.hanainplan.shinhan.account.entity;

import com.hanainplan.shinhan.user.entity.Customer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "shinhan_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @Column(name = "account_number", length = 20)
    private String accountNumber; // 계좌번호 (PK)

    @Column(name = "account_type", nullable = false)
    private Integer accountType; // 계좌종류 (1: 수시입출금, 2: 예적금, 6: 수익증권, 0: 통합계좌)

    @Column(name = "balance", precision = 15, scale = 2)
    private BigDecimal balance; // 잔액

    @Column(name = "opening_date")
    private LocalDate openingDate; // 계좌개설일

    @Column(name = "maturity_date")
    private LocalDate maturityDate; // 만기일

    @Column(name = "third_party_consent")
    private Boolean thirdPartyConsent; // 제3자정보제공동의여부

    @Column(name = "withdrawal_consent")
    private Boolean withdrawalConsent; // 출금동의여부

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 고객과의 관계 (다대일)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Customer customer;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
