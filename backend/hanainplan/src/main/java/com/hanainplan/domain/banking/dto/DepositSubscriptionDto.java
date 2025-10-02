package com.hanainplan.domain.banking.dto;

import com.hanainplan.domain.banking.entity.DepositSubscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 예금 가입 정보 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositSubscriptionDto {

    private Long subscriptionId;
    private Long userId;
    private String customerCi;
    private String accountNumber;
    private String status;
    private LocalDate subscriptionDate;
    private LocalDate maturityDate;
    private Integer contractPeriod;
    private Integer productType; // 0:일반, 1:디폴트옵션, 2:일단위
    private String bankName;
    private String bankCode;
    private String depositCode; // 예금 상품 코드
    private BigDecimal rate;
    private BigDecimal currentBalance;
    private BigDecimal unpaidInterest;
    private LocalDate lastInterestCalculationDate;
    private LocalDate nextInterestPaymentDate;

    /**
     * Entity를 DTO로 변환
     */
    public static DepositSubscriptionDto fromEntity(DepositSubscription entity) {
        if (entity == null) {
            return null;
        }
        
        return DepositSubscriptionDto.builder()
                .subscriptionId(entity.getSubscriptionId())
                .userId(entity.getUserId())
                .customerCi(entity.getCustomerCi())
                .accountNumber(entity.getAccountNumber())
                .status(entity.getStatus())
                .subscriptionDate(entity.getSubscriptionDate())
                .maturityDate(entity.getMaturityDate())
                .contractPeriod(entity.getContractPeriod())
                .productType(entity.getProductType())
                .bankName(entity.getBankName())
                .bankCode(entity.getBankCode())
                .depositCode(entity.getDepositCode())
                .rate(entity.getRate())
                .currentBalance(entity.getCurrentBalance())
                .unpaidInterest(entity.getUnpaidInterest())
                .lastInterestCalculationDate(entity.getLastInterestCalculationDate())
                .nextInterestPaymentDate(entity.getNextInterestPaymentDate())
                .build();
    }

    /**
     * DTO를 Entity로 변환
     */
    public DepositSubscription toEntity() {
        return DepositSubscription.builder()
                .subscriptionId(this.subscriptionId)
                .userId(this.userId)
                .customerCi(this.customerCi)
                .accountNumber(this.accountNumber)
                .status(this.status)
                .subscriptionDate(this.subscriptionDate)
                .maturityDate(this.maturityDate)
                .contractPeriod(this.contractPeriod)
                .productType(this.productType)
                .bankName(this.bankName)
                .bankCode(this.bankCode)
                .depositCode(this.depositCode)
                .rate(this.rate)
                .currentBalance(this.currentBalance)
                .unpaidInterest(this.unpaidInterest)
                .lastInterestCalculationDate(this.lastInterestCalculationDate)
                .nextInterestPaymentDate(this.nextInterestPaymentDate)
                .build();
    }
    
    /**
     * 계약 기간 단위 반환 (일/개월)
     */
    public String getContractPeriodUnit() {
        if (productType == null) {
            return "개월";
        }
        return productType == 2 ? "일" : "개월";
    }
    
    /**
     * 만기 여부 확인
     */
    public boolean isMatured() {
        return maturityDate != null && !maturityDate.isAfter(LocalDate.now());
    }
    
    /**
     * 활성 상태 확인
     */
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
}
