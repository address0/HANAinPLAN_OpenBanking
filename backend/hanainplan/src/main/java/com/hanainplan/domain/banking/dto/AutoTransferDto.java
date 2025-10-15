package com.hanainplan.domain.banking.dto;

import com.hanainplan.domain.banking.entity.AutoTransfer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoTransferDto {

    private Long autoTransferId;
    private Long fromAccountId;
    private String fromAccountNumber;
    private Long toAccountId;
    private String toAccountNumber;
    private String transferName;
    private BigDecimal amount;
    private AutoTransfer.TransferCycle transferCycle;
    private String transferCycleDescription;
    private Integer transferDay;
    private AutoTransfer.Weekday transferWeekday;
    private String transferWeekdayDescription;
    private LocalDate startDate;
    private LocalDate endDate;
    private AutoTransfer.TransferStatus transferStatus;
    private String transferStatusDescription;
    private String counterpartAccountNumber;
    private String counterpartName;
    private String counterpartBankCode;
    private String description;
    private LocalDate lastTransferDate;
    private LocalDate nextTransferDate;
    private Integer transferCount;
    private Integer maxTransferCount;
    private Integer failureCount;
    private Integer maxFailureCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AutoTransferDto fromEntity(AutoTransfer autoTransfer) {
        return AutoTransferDto.builder()
                .autoTransferId(autoTransfer.getAutoTransferId())
                .fromAccountId(autoTransfer.getFromAccountId())
                .toAccountId(autoTransfer.getToAccountId())
                .transferName(autoTransfer.getTransferName())
                .amount(autoTransfer.getAmount())
                .transferCycle(autoTransfer.getTransferCycle())
                .transferCycleDescription(autoTransfer.getTransferCycle().getDescription())
                .transferDay(autoTransfer.getTransferDay())
                .transferWeekday(autoTransfer.getTransferWeekday())
                .transferWeekdayDescription(autoTransfer.getTransferWeekday() != null ? 
                        autoTransfer.getTransferWeekday().getDescription() : null)
                .startDate(autoTransfer.getStartDate())
                .endDate(autoTransfer.getEndDate())
                .transferStatus(autoTransfer.getTransferStatus())
                .transferStatusDescription(autoTransfer.getTransferStatus().getDescription())
                .counterpartAccountNumber(autoTransfer.getCounterpartAccountNumber())
                .counterpartName(autoTransfer.getCounterpartName())
                .counterpartBankCode(autoTransfer.getCounterpartBankCode())
                .description(autoTransfer.getDescription())
                .lastTransferDate(autoTransfer.getLastTransferDate())
                .nextTransferDate(autoTransfer.getNextTransferDate())
                .transferCount(autoTransfer.getTransferCount())
                .maxTransferCount(autoTransfer.getMaxTransferCount())
                .failureCount(autoTransfer.getFailureCount())
                .maxFailureCount(autoTransfer.getMaxFailureCount())
                .createdAt(autoTransfer.getCreatedAt())
                .updatedAt(autoTransfer.getUpdatedAt())
                .build();
    }

    public AutoTransfer toEntity() {
        return AutoTransfer.builder()
                .autoTransferId(this.autoTransferId)
                .fromAccountId(this.fromAccountId)
                .toAccountId(this.toAccountId)
                .transferName(this.transferName)
                .amount(this.amount)
                .transferCycle(this.transferCycle)
                .transferDay(this.transferDay)
                .transferWeekday(this.transferWeekday)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .transferStatus(this.transferStatus)
                .counterpartAccountNumber(this.counterpartAccountNumber)
                .counterpartName(this.counterpartName)
                .counterpartBankCode(this.counterpartBankCode)
                .description(this.description)
                .lastTransferDate(this.lastTransferDate)
                .nextTransferDate(this.nextTransferDate)
                .transferCount(this.transferCount)
                .maxTransferCount(this.maxTransferCount)
                .failureCount(this.failureCount)
                .maxFailureCount(this.maxFailureCount)
                .build();
    }
}