package com.hanainplan.domain.fund.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 펀드 매도(환매) 응답 DTO (하나인플랜)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundRedemptionResponseDto {

    private boolean success;
    private String message;
    private String errorMessage;

    // 거래 정보
    private Long subscriptionId;
    private String childFundCd;
    private String fundName;
    private String classCode;
    
    // 매도 정보
    private BigDecimal sellUnits;               // 매도 좌수
    private BigDecimal sellNav;                 // 매도 기준가
    private BigDecimal sellAmount;              // 매도 금액 (수수료 차감 전)
    private BigDecimal redemptionFee;           // 환매수수료
    private BigDecimal netAmount;               // 실수령액 (수수료 차감 후)
    
    // 수익 정보
    private BigDecimal profit;                  // 실현 손익
    private BigDecimal profitRate;              // 실현 수익률
    
    // 잔고 정보
    private BigDecimal remainingUnits;          // 남은 보유 좌수
    private String status;                      // 상태 (SOLD/PARTIAL_SOLD/ACTIVE)
    
    // 결제 정보
    private LocalDate redemptionDate;           // 매도일
    private LocalDate settlementDate;           // 결제일 (T+N)
    
    // IRP 정보
    private String irpAccountNumber;
    private BigDecimal irpBalanceAfter;         // 입금 후 잔액

    /**
     * 성공 여부 확인
     */
    public boolean isSuccess() {
        return success;
    }
}

