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

    public boolean isSuccess() {
        return success;
    }
}