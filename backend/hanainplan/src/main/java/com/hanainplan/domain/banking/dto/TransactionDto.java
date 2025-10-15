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
    private Transaction.TransactionDirection transactionDirection;
    private String transactionDirectionDescription;
    private String transactionDirectionSymbol;
    private String description;
    private Transaction.TransactionStatus transactionStatus;
    private String transactionStatusDescription;
    private LocalDateTime transactionDate;
    private LocalDateTime processedDate;
    private String referenceNumber;
    private String memo;
    private LocalDateTime createdAt;

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
                .transactionDirection(transaction.getTransactionDirection())
                .transactionDirectionDescription(transaction.getTransactionDirection().getDescription())
                .transactionDirectionSymbol(transaction.getTransactionDirection().getSymbol())
                .description(transaction.getDescription())
                .transactionStatus(transaction.getTransactionStatus())
                .transactionStatusDescription(transaction.getTransactionStatus().getDescription())
                .transactionDate(transaction.getTransactionDate())
                .processedDate(transaction.getProcessedDate())
                .referenceNumber(transaction.getReferenceNumber())
                .memo(transaction.getMemo())
                .createdAt(transaction.getCreatedAt())
                .build();
    }

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
                .transactionDirection(this.transactionDirection)
                .description(this.description)
                .transactionStatus(this.transactionStatus)
                .transactionDate(this.transactionDate)
                .processedDate(this.processedDate)
                .referenceNumber(this.referenceNumber)
                .memo(this.memo)
                .build();
    }
}