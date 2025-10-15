package com.hanainplan.domain.banking.entity;

import com.hanainplan.domain.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_irp_account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class IrpAccount extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "irp_account_id")
    private Long irpAccountId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "customer_ci", nullable = false, length = 64)
    private String customerCi;

    @Column(name = "bank_code", nullable = false, length = 10)
    private String bankCode;

    @Column(name = "account_number", nullable = false, length = 50)
    private String accountNumber;

    @Column(name = "account_status", length = 20)
    @Builder.Default
    private String accountStatus = "ACTIVE";

    @Column(name = "initial_deposit", precision = 15, scale = 2)
    private BigDecimal initialDeposit;

    @Column(name = "monthly_deposit", precision = 15, scale = 2)
    private BigDecimal monthlyDeposit;

    @Column(name = "current_balance", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(name = "total_contribution", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalContribution = BigDecimal.ZERO;

    @Column(name = "total_return", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal totalReturn = BigDecimal.ZERO;

    @Column(name = "return_rate", precision = 7, scale = 4)
    @Builder.Default
    private BigDecimal returnRate = BigDecimal.ZERO;

    @Column(name = "investment_style", length = 30)
    private String investmentStyle;

    @Column(name = "is_auto_deposit")
    @Builder.Default
    private Boolean isAutoDeposit = false;

    @Column(name = "deposit_day")
    private Integer depositDay;

    @Column(name = "linked_main_account", length = 50)
    private String linkedMainAccount;

    @Column(name = "product_code", length = 20)
    private String productCode;

    @Column(name = "product_name", length = 100)
    private String productName;

    @Column(name = "management_fee_rate", precision = 5, scale = 4)
    private BigDecimal managementFeeRate;

    @Column(name = "trust_fee_rate", precision = 5, scale = 4)
    private BigDecimal trustFeeRate;

    @Column(name = "open_date")
    private LocalDate openDate;

    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    @Column(name = "last_contribution_date")
    private LocalDate lastContributionDate;

    @Column(name = "last_sync_date")
    private LocalDateTime lastSyncDate;

    @Column(name = "sync_status", length = 20)
    @Builder.Default
    private String syncStatus = "PENDING";

    @Column(name = "sync_error_message", length = 500)
    private String syncErrorMessage;

    @Column(name = "external_account_id", length = 100)
    private String externalAccountId;

    @Column(name = "external_last_updated")
    private LocalDateTime externalLastUpdated;

    public boolean isActive() {
        return "ACTIVE".equals(accountStatus);
    }

    public boolean hasAutoDeposit() {
        return Boolean.TRUE.equals(isAutoDeposit);
    }

    public boolean isMatured() {
        return maturityDate != null && !maturityDate.isAfter(LocalDate.now());
    }

    public void markSyncSuccess() {
        this.syncStatus = "SUCCESS";
        this.lastSyncDate = LocalDateTime.now();
    }

    public void markSyncFailed(String errorMessage) {
        this.syncStatus = "FAILED";
        this.syncErrorMessage = errorMessage;
        this.lastSyncDate = LocalDateTime.now();
    }

    public void updateBalance(BigDecimal newBalance) {
        this.currentBalance = newBalance;
        this.externalLastUpdated = LocalDateTime.now();
    }

    public void updateContribution(BigDecimal amount) {
        this.totalContribution = this.totalContribution.add(amount);
        this.lastContributionDate = LocalDate.now();
        this.externalLastUpdated = LocalDateTime.now();
    }
}