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

    @Column(name = "customer_ci", nullable = false, length = 64)
    private String customerCi;

    @Column(name = "bank_code", nullable = false, length = 10)
    @Builder.Default
    private String bankCode = "HANA";

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

    @Column(name = "current_year_deposit", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal currentYearDeposit = BigDecimal.ZERO;

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

    public boolean isActive() {
        return "ACTIVE".equals(accountStatus);
    }

    public boolean isMatured() {
        if (maturityDate == null) {
            return false;
        }
        return LocalDate.now().isAfter(maturityDate) || LocalDate.now().isEqual(maturityDate);
    }
}