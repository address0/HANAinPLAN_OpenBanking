package com.hanainplan.domain.fund.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 펀드 매수 응답 DTO (하나인플랜)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundPurchaseResponseDto {

    private boolean success;
    private String message;
    private String errorMessage;

    // 거래 정보
    private Long subscriptionId;
    private String childFundCd;
    private String fundName;
    private String classCode;
    
    // 매수 정보
    private BigDecimal purchaseAmount;
    private BigDecimal purchaseNav;
    private BigDecimal purchaseUnits;
    private BigDecimal purchaseFee;
    
    // 결제 정보
    private LocalDate purchaseDate;
    private LocalDate settlementDate;      // T+N
    
    // IRP 정보
    private String irpAccountNumber;
    private BigDecimal irpBalanceAfter;     // 출금 후 잔액

    /**
     * 성공 여부 확인
     */
    public boolean isSuccess() {
        return success;
    }
}

