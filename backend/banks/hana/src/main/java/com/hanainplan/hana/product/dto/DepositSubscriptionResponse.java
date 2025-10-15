package com.hanainplan.hana.product.dto;

import com.hanainplan.hana.product.entity.DepositSubscription;
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
public class DepositSubscriptionResponse {

    private Long subscriptionId;
    private String customerCi;
    private String accountNumber;
    private String irpAccountNumber;
    private String depositCode;
    private String status;
    private LocalDate subscriptionDate;
    private LocalDate maturityDate;
    private Integer contractPeriod;
    private String contractPeriodUnit;
    private Integer productType;
    private String productTypeName;
    private BigDecimal rate;
    private BigDecimal currentBalance;
    private BigDecimal principalAmount;
    private BigDecimal expectedInterest;
    private BigDecimal expectedMaturityAmount;
    private BigDecimal unpaidInterest;
    private LocalDate lastInterestCalculationDate;
    private LocalDate nextInterestPaymentDate;

    public static DepositSubscriptionResponse fromEntity(DepositSubscription entity) {
        if (entity == null) {
            return null;
        }

        return DepositSubscriptionResponse.builder()
                .subscriptionId(entity.getSubscriptionId())
                .customerCi(entity.getCustomerCi())
                .accountNumber(entity.getAccountNumber())
                .depositCode(entity.getDepositCode())
                .status(entity.getStatus())
                .subscriptionDate(entity.getSubscriptionDate())
                .maturityDate(entity.getMaturityDate())
                .contractPeriod(entity.getContractPeriod())
                .contractPeriodUnit(entity.getProductType() == 2 ? "일" : "개월")
                .productType(entity.getProductType())
                .productTypeName(getProductTypeName(entity.getProductType()))
                .rate(entity.getRate())
                .currentBalance(entity.getCurrentBalance())
                .unpaidInterest(entity.getUnpaidInterest())
                .lastInterestCalculationDate(entity.getLastInterestCalculationDate())
                .nextInterestPaymentDate(entity.getNextInterestPaymentDate())
                .build();
    }

    private static String getProductTypeName(Integer productType) {
        if (productType == null) {
            return "알 수 없음";
        }
        switch (productType) {
            case 0:
                return "일반";
            case 1:
                return "디폴트옵션";
            case 2:
                return "일단위";
            default:
                return "알 수 없음";
        }
    }
}