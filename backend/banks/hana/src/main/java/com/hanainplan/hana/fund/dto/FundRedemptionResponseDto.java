package com.hanainplan.hana.fund.dto;

import com.hanainplan.hana.fund.entity.FundSubscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 펀드 매도(환매) 응답 DTO
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
     * 성공 응답 생성
     */
    public static FundRedemptionResponseDto success(
            FundSubscription subscription,
            BigDecimal sellUnits,
            BigDecimal sellNav,
            BigDecimal sellAmount,
            BigDecimal redemptionFee,
            BigDecimal profit,
            BigDecimal profitRate,
            BigDecimal irpBalanceAfter,
            LocalDate settlementDate
    ) {
        BigDecimal netAmount = sellAmount.subtract(redemptionFee);
        
        return FundRedemptionResponseDto.builder()
                .success(true)
                .message("펀드 매도가 완료되었습니다")
                .subscriptionId(subscription.getSubscriptionId())
                .childFundCd(subscription.getChildFundCd())
                .fundName(subscription.getFundName())
                .classCode(subscription.getClassCode())
                .sellUnits(sellUnits)
                .sellNav(sellNav)
                .sellAmount(sellAmount)
                .redemptionFee(redemptionFee)
                .netAmount(netAmount)
                .profit(profit)
                .profitRate(profitRate)
                .remainingUnits(subscription.getCurrentUnits())
                .status(subscription.getStatus())
                .redemptionDate(LocalDate.now())
                .settlementDate(settlementDate)
                .irpAccountNumber(subscription.getIrpAccountNumber())
                .irpBalanceAfter(irpBalanceAfter)
                .build();
    }

    /**
     * 실패 응답 생성
     */
    public static FundRedemptionResponseDto failure(String errorMessage) {
        return FundRedemptionResponseDto.builder()
                .success(false)
                .message("펀드 매도에 실패했습니다")
                .errorMessage(errorMessage)
                .build();
    }
}

