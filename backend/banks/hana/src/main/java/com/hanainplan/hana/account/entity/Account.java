package com.hanainplan.hana.account.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "hana_accounts")
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

    @Column(name = "customer_ci", nullable = false, length = 100)
    private String customerCi; // 고객 CI (본인확인정보)

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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
