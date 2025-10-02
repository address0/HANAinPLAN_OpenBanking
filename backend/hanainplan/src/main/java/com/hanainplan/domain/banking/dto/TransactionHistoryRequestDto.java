package com.hanainplan.domain.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.hanainplan.domain.banking.entity.Transaction;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistoryRequestDto {
    
    private Long accountId; // 기존 호환성을 위해 유지

    private String accountNumber; // 계좌번호 기반 조회용
    
    private Transaction.TransactionType transactionType;
    
    private Transaction.TransactionCategory transactionCategory;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;
    
    private int page = 0;
    private int size = 20;
    private String sortBy = "transactionDate";
    private String sortDirection = "DESC";
}
