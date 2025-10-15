package com.hanainplan.domain.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositSubscriptionResponseDto {

    private boolean success;
    private String message;
    private String errorCode;

    private Long subscriptionId;
    private String customerCi;
    private String productCode;
    private String accountNumber;
    private String status;
    private LocalDate subscriptionDate;
    private LocalDate maturityDate;
    private Integer contractPeriod;
    private String maturityPeriod;
    private String rateType;
    private BigDecimal baseRate;
    private BigDecimal preferentialRate;
    private BigDecimal finalAppliedRate;
    private String preferentialReason;
    private String interestCalculationBasis;
    private String interestPaymentMethod;
    private BigDecimal contractPrincipal;
    private BigDecimal currentBalance;
    private String branchName;

    private BigDecimal expectedInterest;
    private BigDecimal expectedMaturityAmount;
    private String bankName;

    public static DepositSubscriptionResponseDto success(
            Long subscriptionId,
            String productCode,
            String accountNumber,
            LocalDate subscriptionDate,
            LocalDate maturityDate,
            BigDecimal finalAppliedRate,
            BigDecimal contractPrincipal,
            BigDecimal expectedInterest,
            BigDecimal expectedMaturityAmount,
            String bankName) {
        DepositSubscriptionResponseDto dto = new DepositSubscriptionResponseDto();
        dto.success = true;
        dto.message = "정기예금 가입이 완료되었습니다.";
        dto.subscriptionId = subscriptionId;
        dto.productCode = productCode;
        dto.accountNumber = accountNumber;
        dto.subscriptionDate = subscriptionDate;
        dto.maturityDate = maturityDate;
        dto.finalAppliedRate = finalAppliedRate;
        dto.contractPrincipal = contractPrincipal;
        dto.expectedInterest = expectedInterest;
        dto.expectedMaturityAmount = expectedMaturityAmount;
        dto.bankName = bankName;
        return dto;
    }

    public static DepositSubscriptionResponseDto failure(String message, String errorCode) {
        DepositSubscriptionResponseDto dto = new DepositSubscriptionResponseDto();
        dto.success = false;
        dto.message = message;
        dto.errorCode = errorCode;
        return dto;
    }
}