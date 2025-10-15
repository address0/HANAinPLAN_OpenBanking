package com.hanainplan.shinhan.product.dto;

import com.hanainplan.shinhan.product.entity.FinancialProduct;
import lombok.Getter;
import lombok.Setter;

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
    private String subscriptionAmount;
    private String productCategory;
    private String interestPayment;
    private String taxBenefit;
    private String partialWithdrawal;
    private String depositorProtection;
    private String transactionMethod;
    private String precautions;
    private String contractCancellationRight;
    private String cancellationPenalty;
    private String paymentRestrictions;
    private Boolean isActive;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FinancialProductResponseDto() {}

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
        this.depositorProtection = product.getDepositorProtection();
        this.transactionMethod = product.getTransactionMethod();
        this.precautions = product.getPrecautions();
        this.contractCancellationRight = product.getContractCancellationRight();
        this.cancellationPenalty = product.getCancellationPenalty();
        this.paymentRestrictions = product.getPaymentRestrictions();
        this.isActive = product.getIsActive();
        this.startDate = product.getStartDate();
        this.endDate = product.getEndDate();
        this.createdAt = product.getCreatedAt();
        this.updatedAt = product.getUpdatedAt();
    }

    public static FinancialProductResponseDto from(FinancialProduct product) {
        return new FinancialProductResponseDto(product);
    }
}