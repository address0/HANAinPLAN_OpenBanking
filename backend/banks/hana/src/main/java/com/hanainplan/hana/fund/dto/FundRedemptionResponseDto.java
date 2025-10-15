package com.hanainplan.hana.fund.dto;

import com.hanainplan.hana.fund.entity.FundSubscription;
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
public class FundRedemptionResponseDto {

    private boolean success;
    private String message;
    private String errorMessage;

    private Long subscriptionId;
    private String childFundCd;
    private String fundName;
    private String classCode;

    private BigDecimal sellUnits;
    private BigDecimal sellNav;
    private BigDecimal sellAmount;
    private BigDecimal redemptionFee;
    private BigDecimal netAmount;

    private BigDecimal profit;
    private BigDecimal profitRate;

    private BigDecimal remainingUnits;
    private String status;

    private LocalDate redemptionDate;
    private LocalDate settlementDate;

    private String irpAccountNumber;
    private BigDecimal irpBalanceAfter;

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

    public static FundRedemptionResponseDto failure(String errorMessage) {
        return FundRedemptionResponseDto.builder()
                .success(false)
                .message("펀드 매도에 실패했습니다")
                .errorMessage(errorMessage)
                .build();
    }
}