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
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_banking_auto_transfer")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AutoTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auto_transfer_id")
    private Long autoTransferId;

    @Column(name = "from_account_id", nullable = false)
    private Long fromAccountId;

    @Column(name = "to_account_id", nullable = false)
    private Long toAccountId;

    @Column(name = "transfer_name", nullable = false, length = 100)
    private String transferName;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_cycle", nullable = false)
    private TransferCycle transferCycle;

    @Column(name = "transfer_day")
    private Integer transferDay;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_weekday")
    private Weekday transferWeekday;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_status", nullable = false)
    private TransferStatus transferStatus;

    @Column(name = "counterpart_account_number", length = 20)
    private String counterpartAccountNumber;

    @Column(name = "counterpart_name", length = 50)
    private String counterpartName;

    @Column(name = "counterpart_bank_code", length = 10)
    private String counterpartBankCode;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "last_transfer_date")
    private LocalDate lastTransferDate;

    @Column(name = "next_transfer_date")
    private LocalDate nextTransferDate;

    @Column(name = "transfer_count", nullable = false)
    private Integer transferCount;

    @Column(name = "max_transfer_count")
    private Integer maxTransferCount;

    @Column(name = "failure_count", nullable = false)
    private Integer failureCount;

    @Column(name = "max_failure_count", nullable = false)
    private Integer maxFailureCount;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum TransferCycle {
        DAILY("매일"),
        WEEKLY("매주"),
        MONTHLY("매월"),
        QUARTERLY("분기별"),
        YEARLY("연간");

        private final String description;

        TransferCycle(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum Weekday {
        MONDAY("월요일"),
        TUESDAY("화요일"),
        WEDNESDAY("수요일"),
        THURSDAY("목요일"),
        FRIDAY("금요일"),
        SATURDAY("토요일"),
        SUNDAY("일요일");

        private final String description;

        Weekday(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum TransferStatus {
        ACTIVE("활성"),
        INACTIVE("비활성"),
        SUSPENDED("정지"),
        COMPLETED("완료"),
        CANCELLED("취소");

        private final String description;

        TransferStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public void calculateNextTransferDate() {
        LocalDate today = LocalDate.now();
        LocalDate nextDate = null;

        switch (this.transferCycle) {
            case DAILY:
                nextDate = today.plusDays(1);
                break;
            case WEEKLY:
                if (this.transferWeekday != null) {
                    int daysUntilNext = (this.transferWeekday.ordinal() - today.getDayOfWeek().getValue() + 7) % 7;
                    if (daysUntilNext == 0) daysUntilNext = 7;
                    nextDate = today.plusDays(daysUntilNext);
                }
                break;
            case MONTHLY:
                if (this.transferDay != null) {
                    nextDate = today.withDayOfMonth(this.transferDay);
                    if (nextDate.isBefore(today) || nextDate.isEqual(today)) {
                        nextDate = nextDate.plusMonths(1);
                    }
                }
                break;
            case QUARTERLY:
                nextDate = today.plusMonths(3);
                break;
            case YEARLY:
                nextDate = today.plusYears(1);
                break;
        }

        this.nextTransferDate = nextDate;
    }

    public boolean canExecute() {
        return this.transferStatus == TransferStatus.ACTIVE &&
               this.nextTransferDate != null &&
               LocalDate.now().isEqual(this.nextTransferDate) &&
               (this.maxTransferCount == null || this.transferCount < this.maxTransferCount) &&
               this.failureCount < this.maxFailureCount;
    }

    public void executeTransfer() {
        this.lastTransferDate = LocalDate.now();
        this.transferCount++;
        this.failureCount = 0;
        calculateNextTransferDate();

        if (this.maxTransferCount != null && this.transferCount >= this.maxTransferCount) {
            this.transferStatus = TransferStatus.COMPLETED;
        }
    }

    public void failTransfer() {
        this.failureCount++;

        if (this.failureCount >= this.maxFailureCount) {
            this.transferStatus = TransferStatus.SUSPENDED;
        }
    }
}