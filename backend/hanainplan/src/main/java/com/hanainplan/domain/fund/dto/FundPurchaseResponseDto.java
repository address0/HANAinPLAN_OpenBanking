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

    public boolean isSuccess() {
        return success;
    }
}