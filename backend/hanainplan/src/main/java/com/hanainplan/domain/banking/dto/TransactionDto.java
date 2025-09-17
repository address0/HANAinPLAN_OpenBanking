package com.hanainplan.domain.banking.dto;

import com.hanainplan.domain.banking.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    
    private Long transactionId;
    private String transactionNumber;
    private Long fromAccountId;
    private String fromAccountNumber;
    private Long toAccountId;
    private String toAccountNumber;
    private Transaction.TransactionType transactionType;
    private String transactionTypeDescription;
    private Transaction.TransactionCategory transactionCategory;
    private String transactionCategoryDescription;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private BigDecimal fee;
    private String description;
    private String counterpartAccountNumber;
    private String counterpartName;
    private Transaction.TransactionStatus transactionStatus;
    private String transactionStatusDescription;
    private LocalDateTime transactionDate;
    private LocalDateTime processedDate;
    private String failureReason;
    private String referenceNumber;
    private String memo;
    private LocalDateTime createdAt;
    
    // 엔터티에서 DTO로 변환
    public static TransactionDto fromEntity(Transaction transaction) {
        return TransactionDto.builder()
                .transactionId(transaction.getTransactionId())
                .transactionNumber(transaction.getTransactionNumber())
                .fromAccountId(transaction.getFromAccountId())
                .toAccountId(transaction.getToAccountId())
                .transactionType(transaction.getTransactionType())
                .transactionTypeDescription(transaction.getTransactionType().getDescription())
                .transactionCategory(transaction.getTransactionCategory())
                .transactionCategoryDescription(transaction.getTransactionCategory().getDescription())
                .amount(transaction.getAmount())
                .balanceAfter(transaction.getBalanceAfter())
                .fee(transaction.getFee())
                .description(transaction.getDescription())
                .counterpartAccountNumber(transaction.getCounterpartAccountNumber())
                .counterpartName(transaction.getCounterpartName())
                .transactionStatus(transaction.getTransactionStatus())
                .transactionStatusDescription(transaction.getTransactionStatus().getDescription())
                .transactionDate(transaction.getTransactionDate())
                .processedDate(transaction.getProcessedDate())
                .failureReason(transaction.getFailureReason())
                .referenceNumber(transaction.getReferenceNumber())
                .memo(transaction.getMemo())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
    
    // DTO에서 엔터티로 변환
    public Transaction toEntity() {
        return Transaction.builder()
                .transactionId(this.transactionId)
                .transactionNumber(this.transactionNumber)
                .fromAccountId(this.fromAccountId)
                .toAccountId(this.toAccountId)
                .transactionType(this.transactionType)
                .transactionCategory(this.transactionCategory)
                .amount(this.amount)
                .balanceAfter(this.balanceAfter)
                .fee(this.fee)
                .description(this.description)
                .counterpartAccountNumber(this.counterpartAccountNumber)
                .counterpartName(this.counterpartName)
                .transactionStatus(this.transactionStatus)
                .transactionDate(this.transactionDate)
                .processedDate(this.processedDate)
                .failureReason(this.failureReason)
                .referenceNumber(this.referenceNumber)
                .memo(this.memo)
                .build();
    }
}
