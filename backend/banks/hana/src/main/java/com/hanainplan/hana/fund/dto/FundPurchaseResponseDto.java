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
public class FundPurchaseResponseDto {

    private boolean success;
    private String message;
    private String errorMessage;

    private Long subscriptionId;
    private String childFundCd;
    private String fundName;
    private String classCode;

    private BigDecimal purchaseAmount;
    private BigDecimal purchaseNav;
    private BigDecimal purchaseUnits;
    private BigDecimal purchaseFee;

    private LocalDate purchaseDate;
    private LocalDate settlementDate;

    private String irpAccountNumber;
    private BigDecimal irpBalanceAfter;

    public static FundPurchaseResponseDto success(
            FundSubscription subscription,
            BigDecimal irpBalanceAfter,
            LocalDate settlementDate
    ) {
        return FundPurchaseResponseDto.builder()
                .success(true)
                .message("펀드 매수가 완료되었습니다")
                .subscriptionId(subscription.getSubscriptionId())
                .childFundCd(subscription.getChildFundCd())
                .fundName(subscription.getFundName())
                .classCode(subscription.getClassCode())
                .purchaseAmount(subscription.getPurchaseAmount())
                .purchaseNav(subscription.getPurchaseNav())
                .purchaseUnits(subscription.getPurchaseUnits())
                .purchaseFee(subscription.getPurchaseFee())
                .purchaseDate(subscription.getPurchaseDate())
                .settlementDate(settlementDate)
                .irpAccountNumber(subscription.getIrpAccountNumber())
                .irpBalanceAfter(irpBalanceAfter)
                .build();
    }

    public static FundPurchaseResponseDto failure(String errorMessage) {
        return FundPurchaseResponseDto.builder()
                .success(false)
                .message("펀드 매수에 실패했습니다")
                .errorMessage(errorMessage)
                .build();
    }
}