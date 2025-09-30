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
 * 통합 IRP 거래 내역 엔티티
 * - 은행별 IRP 거래 내역을 HANAinPLAN에서 통합 관리
 */
@Entity
@Table(name = "tb_irp_transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IrpTransaction extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "irp_account_id", nullable = false)
    private IrpAccount irpAccount; // 연결된 IRP 계좌

    @Column(name = "customer_ci", nullable = false, length = 64)
    private String customerCi; // 고객 CI

    @Column(name = "bank_code", nullable = false, length = 10)
    private String bankCode; // 은행 코드 (HANA, KOOKMIN, SHINHAN)

    @Column(name = "account_number", nullable = false, length = 50)
    private String accountNumber; // IRP 계좌번호

    @Column(name = "transaction_type", nullable = false, length = 30)
    private String transactionType; // CONTRIBUTION(납입), WITHDRAWAL(출금), RETURN(수익), FEE(수수료), REBALANCING(리밸런싱)

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate; // 거래일

    @Column(name = "amount", precision = 15, scale = 2)
    private BigDecimal amount; // 거래금액

    @Column(name = "balance_after", precision = 15, scale = 2)
    private BigDecimal balanceAfter; // 거래 후 잔고

    @Column(name = "description", length = 200)
    private String description; // 거래 설명

    @Column(name = "reference_number", length = 100)
    private String referenceNumber; // 참조번호 (은행별 거래 ID)

    @Column(name = "transaction_category", length = 50)
    private String transactionCategory; // REGULAR(정기), EXTRA(추가), RETURN(수익), FEE(수수료), REBALANCING(리밸런싱)

    @Column(name = "investment_style_before", length = 30)
    private String investmentStyleBefore; // 변경 전 투자성향

    @Column(name = "investment_style_after", length = 30)
    private String investmentStyleAfter; // 변경 후 투자성향

    @Column(name = "fee_type", length = 50)
    private String feeType; // 수수료 유형 (관리수수료, 신탁수수료, 판매수수료)

    @Column(name = "fee_rate", precision = 5, scale = 4)
    private BigDecimal feeRate; // 수수료율

    @Column(name = "tax_amount", precision = 15, scale = 2)
    private BigDecimal taxAmount; // 세금 금액

    @Column(name = "fund_allocation_before", length = 1000)
    private String fundAllocationBefore; // 변경 전 펀드 배분 (JSON)

    @Column(name = "fund_allocation_after", length = 1000)
    private String fundAllocationAfter; // 변경 후 펀드 배분 (JSON)

    @Column(name = "processing_status", length = 20)
    @Builder.Default
    private String processingStatus = "COMPLETED"; // PENDING, PROCESSING, COMPLETED, FAILED

    @Column(name = "error_message", length = 500)
    private String errorMessage; // 처리 오류 메시지

    @Column(name = "external_transaction_id", length = 100)
    private String externalTransactionId; // 은행별 거래 ID

    @Column(name = "sync_date")
    private LocalDateTime syncDate; // 동기화 일시

    @Column(name = "is_latest", nullable = false)
    @Builder.Default
    private Boolean isLatest = true; // 최신 거래 여부 (중복 방지용)

    /**
     * 납입 거래 확인
     */
    public boolean isContribution() {
        return "CONTRIBUTION".equals(transactionType);
    }

    /**
     * 출금 거래 확인
     */
    public boolean isWithdrawal() {
        return "WITHDRAWAL".equals(transactionType);
    }

    /**
     * 수익 거래 확인
     */
    public boolean isReturn() {
        return "RETURN".equals(transactionType);
    }

    /**
     * 수수료 거래 확인
     */
    public boolean isFee() {
        return "FEE".equals(transactionType);
    }

    /**
     * 리밸런싱 거래 확인
     */
    public boolean isRebalancing() {
        return "REBALANCING".equals(transactionType);
    }

    /**
     * 정기 납입 확인
     */
    public boolean isRegularContribution() {
        return "CONTRIBUTION".equals(transactionType) && "REGULAR".equals(transactionCategory);
    }

    /**
     * 추가 납입 확인
     */
    public boolean isExtraContribution() {
        return "CONTRIBUTION".equals(transactionType) && "EXTRA".equals(transactionCategory);
    }

    /**
     * 처리 성공 처리
     */
    public void markCompleted() {
        this.processingStatus = "COMPLETED";
        this.syncDate = LocalDateTime.now();
    }

    /**
     * 처리 실패 처리
     */
    public void markFailed(String errorMessage) {
        this.processingStatus = "FAILED";
        this.errorMessage = errorMessage;
        this.syncDate = LocalDateTime.now();
    }

    /**
     * 거래 정보 업데이트
     */
    public void updateTransactionInfo(IrpTransaction updatedTransaction) {
        this.transactionType = updatedTransaction.transactionType;
        this.transactionDate = updatedTransaction.transactionDate;
        this.amount = updatedTransaction.amount;
        this.balanceAfter = updatedTransaction.balanceAfter;
        this.description = updatedTransaction.description;
        this.referenceNumber = updatedTransaction.referenceNumber;
        this.transactionCategory = updatedTransaction.transactionCategory;
        this.investmentStyleBefore = updatedTransaction.investmentStyleBefore;
        this.investmentStyleAfter = updatedTransaction.investmentStyleAfter;
        this.feeType = updatedTransaction.feeType;
        this.feeRate = updatedTransaction.feeRate;
        this.taxAmount = updatedTransaction.taxAmount;
        this.fundAllocationBefore = updatedTransaction.fundAllocationBefore;
        this.fundAllocationAfter = updatedTransaction.fundAllocationAfter;
        this.syncDate = LocalDateTime.now();
    }

    /**
     * 최신 거래로 설정
     */
    public void markAsLatest() {
        this.isLatest = true;
    }

    /**
     * 최신 거래 해제
     */
    public void unmarkAsLatest() {
        this.isLatest = false;
    }
}
