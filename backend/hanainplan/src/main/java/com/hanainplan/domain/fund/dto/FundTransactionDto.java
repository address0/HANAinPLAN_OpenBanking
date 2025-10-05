package com.hanainplan.domain.fund.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 펀드 거래 내역 DTO (하나인플랜)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundTransactionDto {

    private Long transactionId;
    private Long portfolioId;            // 포트폴리오 ID (내부용)
    private Long userId;                 // 사용자 ID
    private String customerCi;
    private Long subscriptionId;         // 가입 ID (프론트엔드용)
    private String fundCode;             // 펀드 코드
    private String childFundCd;          // 클래스 펀드 코드
    private String fundName;
    private String classCode;
    
    private String transactionType;      // BUY, SELL, DIVIDEND
    private String transactionTypeName;  // 매수, 매도, 분배금
    private LocalDate transactionDate;
    private LocalDate settlementDate;
    
    private BigDecimal nav;
    private BigDecimal units;
    private BigDecimal amount;
    private BigDecimal fee;
    private String feeType;
    
    private BigDecimal profit;           // 실현 손익 (매도 시)
    private BigDecimal profitRate;       // 실현 수익률 (매도 시)
    
    private String irpAccountNumber;
    private BigDecimal irpBalanceBefore;
    private BigDecimal irpBalanceAfter;
    
    private String status;
    private String note;
    private String description;          // 거래 설명
    
    private java.time.LocalDateTime createdAt;
}
