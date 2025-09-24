package com.hanainplan.hana.product.dto;

import com.hanainplan.hana.product.entity.FinancialProduct;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class FinancialProductResponseDto {

    private Long productId;
    private String productCode;
    private String productName;
    private String depositType;
    private Integer minContractPeriod;
    private Integer maxContractPeriod;
    private String contractPeriodUnit;
    private String subscriptionTarget;
    private BigDecimal subscriptionAmount;
    private String productCategory;
    private String interestPayment;
    private String taxBenefit;
    private String partialWithdrawal;
    private String cancellationPenalty;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 기본 생성자
    public FinancialProductResponseDto() {}

    // 생성자
    public FinancialProductResponseDto(FinancialProduct product) {
        this.productId = product.getProductId();
        this.productCode = product.getProductCode();
        this.productName = product.getProductName();
        this.depositType = product.getDepositType();
        this.minContractPeriod = product.getMinContractPeriod();
        this.maxContractPeriod = product.getMaxContractPeriod();
        this.contractPeriodUnit = product.getContractPeriodUnit();
        this.subscriptionTarget = product.getSubscriptionTarget();
        this.subscriptionAmount = product.getSubscriptionAmount();
        this.productCategory = product.getProductCategory();
        this.interestPayment = product.getInterestPayment();
        this.taxBenefit = product.getTaxBenefit();
        this.partialWithdrawal = product.getPartialWithdrawal();
        this.cancellationPenalty = product.getCancellationPenalty();
        this.description = product.getDescription();
        this.createdAt = product.getCreatedAt();
        this.updatedAt = product.getUpdatedAt();
    }

    // 정적 팩토리 메서드
    public static FinancialProductResponseDto from(FinancialProduct product) {
        return new FinancialProductResponseDto(product);
    }
}
