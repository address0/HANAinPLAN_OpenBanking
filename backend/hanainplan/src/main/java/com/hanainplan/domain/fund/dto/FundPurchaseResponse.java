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
public class FundPurchaseResponse {

    private boolean success;
    private String message;
    private String errorMessage;

    private Long portfolioId;
    private String fundCode;
    private String fundName;
    private BigDecimal purchaseAmount;
    private BigDecimal purchaseNav;
    private BigDecimal purchaseUnits;
    private BigDecimal salesFee;
    private LocalDate settlementDate;

    public static FundPurchaseResponse success(
            Long portfolioId,
            String fundCode,
            String fundName,
            BigDecimal purchaseAmount,
            BigDecimal purchaseNav,
            BigDecimal purchaseUnits,
            BigDecimal salesFee,
            LocalDate settlementDate
    ) {
        return FundPurchaseResponse.builder()
                .success(true)
                .message("펀드 매수가 완료되었습니다")
                .portfolioId(portfolioId)
                .fundCode(fundCode)
                .fundName(fundName)
                .purchaseAmount(purchaseAmount)
                .purchaseNav(purchaseNav)
                .purchaseUnits(purchaseUnits)
                .salesFee(salesFee)
                .settlementDate(settlementDate)
                .build();
    }

    public static FundPurchaseResponse failure(String errorMessage) {
        return FundPurchaseResponse.builder()
                .success(false)
                .message("펀드 매수에 실패했습니다")
                .errorMessage(errorMessage)
                .build();
    }
}