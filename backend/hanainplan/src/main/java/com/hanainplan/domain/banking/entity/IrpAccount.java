package com.hanainplan.domain.banking.entity;

import com.hanainplan.domain.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 통합 IRP 계좌 엔티티
 * - 은행별 IRP 계좌 정보를 HANAinPLAN에서 통합 관리
 */
@Entity
@Table(name = "tb_irp_account")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IrpAccount extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "irp_account_id")
    private Long irpAccountId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId; // HANAinPLAN 고객 ID

    @Column(name = "customer_ci", nullable = false, length = 64)
    private String customerCi; // 고객 CI

    @Column(name = "bank_code", nullable = false, length = 10)
    private String bankCode; // 은행 코드 (HANA, KOOKMIN, SHINHAN)

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

    /**
     * 계좌 활성화 상태 확인
     */
    public boolean isActive() {
        return "ACTIVE".equals(accountStatus);
    }

    /**
     * 자동납입 설정 여부 확인
     */
    public boolean hasAutoDeposit() {
        return Boolean.TRUE.equals(isAutoDeposit);
    }

    /**
     * 만기 도래 여부 확인
     */
    public boolean isMatured() {
        return maturityDate != null && !maturityDate.isAfter(LocalDate.now());
    }

    /**
     * 동기화 성공 처리
     */
    public void markSyncSuccess() {
        this.syncStatus = "SUCCESS";
        this.lastSyncDate = LocalDateTime.now();
    }

    /**
     * 동기화 실패 처리
     */
    public void markSyncFailed(String errorMessage) {
        this.syncStatus = "FAILED";
        this.syncErrorMessage = errorMessage;
        this.lastSyncDate = LocalDateTime.now();
    }

    /**
     * 잔고 업데이트
     */
    public void updateBalance(BigDecimal newBalance) {
        this.currentBalance = newBalance;
        this.externalLastUpdated = LocalDateTime.now();
    }

    /**
     * 납입 정보 업데이트
     */
    public void updateContribution(BigDecimal amount) {
        this.totalContribution = this.totalContribution.add(amount);
        this.lastContributionDate = LocalDate.now();
        this.externalLastUpdated = LocalDateTime.now();
    }
}
