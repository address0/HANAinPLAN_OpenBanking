package com.hanainplan.domain.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 정기예금 상품 가입 응답 DTO
 */
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
    private Integer contractPeriod; // 약정기간 (개월)
    private String maturityPeriod; // 만기기간 문자열
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

    public static DepositSubscriptionResponseDto success(
            Long subscriptionId,
            String productCode,
            String accountNumber,
            LocalDate subscriptionDate,
            LocalDate maturityDate,
            BigDecimal finalAppliedRate) {
        return DepositSubscriptionResponseDto.builder()
                .success(true)
                .message("정기예금 가입이 완료되었습니다.")
                .subscriptionId(subscriptionId)
                .productCode(productCode)
                .accountNumber(accountNumber)
                .subscriptionDate(subscriptionDate)
                .maturityDate(maturityDate)
                .finalAppliedRate(finalAppliedRate)
                .build();
    }

    public static DepositSubscriptionResponseDto failure(String message, String errorCode) {
        return DepositSubscriptionResponseDto.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
}



