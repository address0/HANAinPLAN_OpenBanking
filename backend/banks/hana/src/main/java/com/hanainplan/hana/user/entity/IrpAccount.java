package com.hanainplan.hana.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * IRP 계좌 엔터티
 */
@Entity
@Table(name = "hana_irp_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IrpAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "irp_account_id")
    private Long irpAccountId;

    // HANAinPLAN 고객 ID는 하나은행에서 사용하지 않음
    // @Column(name = "customer_id", nullable = true)
    // private Long customerId;

    @Column(name = "customer_ci", nullable = false, length = 64)
    private String customerCi; // 고객 CI

    @Column(name = "bank_code", nullable = false, length = 10)
    @Builder.Default
    private String bankCode = "HANA"; // 은행 코드 (항상 HANA)

    @Column(name = "account_number", nullable = false, length = 50)
    private String accountNumber; // IRP 계좌번호

    @Column(name = "account_status", length = 20)
    @Builder.Default
    private String accountStatus = "ACTIVE"; // ACTIVE, CLOSED, SUSPENDED

    @Column(name = "initial_deposit", precision = 15, scale = 2)
    private BigDecimal initialDeposit; // 초기 납입금액

    @Column(name = "monthly_deposit", precision = 15, scale = 2)
    private BigDecimal monthlyDeposit; // 월 자동납입금액

    @Column(name = "current_balance", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal currentBalance = BigDecimal.ZERO; // 현재 잔고

    @Column(name = "total_contribution", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalContribution = BigDecimal.ZERO; // 총 납입금

    @Column(name = "total_return", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalReturn = BigDecimal.ZERO; // 총 수익금

    @Column(name = "return_rate", precision = 7, scale = 4)
    @Builder.Default
    private BigDecimal returnRate = BigDecimal.ZERO; // 수익률 (%)

    @Column(name = "investment_style", length = 30)
    private String investmentStyle; // CONSERVATIVE, MODERATE_CONSERVATIVE, MODERATE, AGGRESSIVE

    @Column(name = "is_auto_deposit")
    @Builder.Default
    private Boolean isAutoDeposit = false; // 자동납입 여부

    @Column(name = "deposit_day")
    private Integer depositDay; // 자동납입일 (1-31)

    @Column(name = "linked_main_account", length = 50)
    private String linkedMainAccount; // 연결된 주계좌

    @Column(name = "product_code", length = 20)
    private String productCode; // IRP 상품코드

    @Column(name = "product_name", length = 100)
    private String productName; // IRP 상품명

    @Column(name = "management_fee_rate", precision = 5, scale = 4)
    private BigDecimal managementFeeRate; // 운용수수료율

    @Column(name = "trust_fee_rate", precision = 5, scale = 4)
    private BigDecimal trustFeeRate; // 신탁수수료율

    @Column(name = "open_date")
    private LocalDate openDate; // 계좌 개설일

    @Column(name = "maturity_date")
    private LocalDate maturityDate; // 만기일

    @Column(name = "last_contribution_date")
    private LocalDate lastContributionDate; // 마지막 납입일

    @Column(name = "last_sync_date")
    private LocalDateTime lastSyncDate; // 마지막 동기화 일시

    @Column(name = "sync_status", length = 20)
    @Builder.Default
    private String syncStatus = "PENDING"; // PENDING, SUCCESS, FAILED

    @Column(name = "sync_error_message", length = 500)
    private String syncErrorMessage; // 동기화 오류 메시지

    @Column(name = "external_account_id", length = 100)
    private String externalAccountId; // 은행별 내부 계좌 ID

    @Column(name = "external_last_updated")
    private LocalDateTime externalLastUpdated; // 은행별 마지막 업데이트 일시

    @Setter
    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
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

    /**
     * 계좌 활성화 상태 확인
     */
    public boolean isActive() {
        return "ACTIVE".equals(accountStatus);
    }

    /**
     * 계좌 만기 확인
     */
    public boolean isMatured() {
        if (maturityDate == null) {
            return false;
        }
        return LocalDate.now().isAfter(maturityDate) || LocalDate.now().isEqual(maturityDate);
    }
}
