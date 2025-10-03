package com.hanainplan.hana.account.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "hana_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @Column(name = "transaction_id", length = 50)
    @JsonProperty("transactionNumber") // JSON 직렬화 시 transactionNumber로 매핑
    private String transactionId; // 거래고유번호 (PK)

    @Column(name = "transaction_datetime", nullable = false)
    private LocalDateTime transactionDatetime; // 거래일시

    @Column(name = "transaction_type", nullable = false, length = 50)
    private String transactionType; // 입출금구분 (입금, 출금, 지급, 기타)

    @Column(name = "transaction_category", length = 50)
    private String transactionCategory; // 거래구분 (현금, 급여, 타행환)

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount; // 거래금액

    @Column(name = "balance_after", precision = 15, scale = 2)
    private BigDecimal balanceAfter; // 거래후잔액

    @Column(name = "branch_name", length = 100)
    private String branchName; // 거래점명

    @Column(name = "description")
    private String description; // 거래 설명

    @Column(name = "reference_number", length = 50)
    private String referenceNumber; // 참조 번호
    
    @Column(name = "transaction_status", length = 20)
    private String transactionStatus; // 거래 상태
    
    @Column(name = "transaction_direction", length = 20)
    private String transactionDirection; // 거래 방향 (CREDIT/DEBIT)
    
    @Column(name = "memo")
    private String memo; // 메모

    @Column(name = "created_at")
    @JsonProperty("processedDate") // JSON 직렬화 시 processedDate로 매핑
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @JsonIgnore // updatedAt은 JSON에서 제외
    private LocalDateTime updatedAt;

    // 계좌와의 관계 (다대일)
    @JsonIgnore // JSON 직렬화 시 순환 참조 및 Lazy Loading 문제 방지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_number", nullable = false, insertable = false, updatable = false)
    private Account account;
    
    // 계좌번호를 직접 필드로 추가 (조회용)
    @Column(name = "account_number", insertable = true, updatable = true)
    private String accountNumber;

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
