package com.hanainplan.hana.fund.dto;

import com.hanainplan.hana.fund.entity.FundSubscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 펀드 매수 응답 DTO
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
     * 성공 응답 생성
     */
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

    /**
     * 실패 응답 생성
     */
    public static FundPurchaseResponseDto failure(String errorMessage) {
        return FundPurchaseResponseDto.builder()
                .success(false)
                .message("펀드 매수에 실패했습니다")
                .errorMessage(errorMessage)
                .build();
    }
}

