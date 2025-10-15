package com.hanainplan.domain.fund.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundTransactionDto {

    private Long transactionId;
    private Long portfolioId;
    private Long userId;
    private String customerCi;
    private Long subscriptionId;
    private String fundCode;
    private String childFundCd;
    private String fundName;
    private String classCode;

    private String transactionType;
    private String transactionTypeName;
    private LocalDate transactionDate;
    private LocalDate settlementDate;

    private BigDecimal nav;
    private BigDecimal units;
    private BigDecimal amount;
    private BigDecimal fee;
    private String feeType;

    private BigDecimal profit;
    private BigDecimal profitRate;

    private String irpAccountNumber;
    private BigDecimal irpBalanceBefore;
    private BigDecimal irpBalanceAfter;

    private String status;
    private String note;
    private String description;

    private java.time.LocalDateTime createdAt;
}