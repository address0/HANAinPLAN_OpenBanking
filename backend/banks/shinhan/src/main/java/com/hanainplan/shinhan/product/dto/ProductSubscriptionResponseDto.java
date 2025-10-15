package com.hanainplan.shinhan.product.dto;

import com.hanainplan.shinhan.product.entity.ProductSubscription;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class ProductSubscriptionResponseDto {

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
    private String interestType;
    private BigDecimal contractPrincipal;
    private BigDecimal currentBalance;
    private BigDecimal unpaidInterest;
    private BigDecimal unpaidTax;
    private LocalDate lastInterestCalculationDate;
    private LocalDate nextInterestPaymentDate;
    private String branchName;
    private BigDecimal monthlyPaymentAmount;
    private Integer monthlyPaymentDay;
    private Integer totalInstallments;
    private Integer completedInstallments;
    private Integer missedInstallments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProductSubscriptionResponseDto() {}

    public ProductSubscriptionResponseDto(ProductSubscription subscription) {
        this.subscriptionId = subscription.getSubscriptionId();
        this.customerCi = subscription.getCustomerCi();
        this.productCode = subscription.getProductCode();
        this.accountNumber = subscription.getAccountNumber();
        this.status = subscription.getStatus();
        this.subscriptionDate = subscription.getSubscriptionDate();
        this.maturityDate = subscription.getMaturityDate();
        this.contractPeriod = subscription.getContractPeriod();
        this.maturityPeriod = subscription.getMaturityPeriod();
        this.rateType = subscription.getRateType();
        this.baseRate = subscription.getBaseRate();
        this.preferentialRate = subscription.getPreferentialRate();
        this.finalAppliedRate = subscription.getFinalAppliedRate();
        this.preferentialReason = subscription.getPreferentialReason();
        this.interestCalculationBasis = subscription.getInterestCalculationBasis();
        this.interestPaymentMethod = subscription.getInterestPaymentMethod();
        this.interestType = subscription.getInterestType();
        this.contractPrincipal = subscription.getContractPrincipal();
        this.currentBalance = subscription.getCurrentBalance();
        this.unpaidInterest = subscription.getUnpaidInterest();
        this.unpaidTax = subscription.getUnpaidTax();
        this.lastInterestCalculationDate = subscription.getLastInterestCalculationDate();
        this.nextInterestPaymentDate = subscription.getNextInterestPaymentDate();
        this.branchName = subscription.getBranchName();
        this.monthlyPaymentAmount = subscription.getMonthlyPaymentAmount();
        this.monthlyPaymentDay = subscription.getMonthlyPaymentDay();
        this.totalInstallments = subscription.getTotalInstallments();
        this.completedInstallments = subscription.getCompletedInstallments();
        this.missedInstallments = subscription.getMissedInstallments();
        this.createdAt = subscription.getCreatedAt();
        this.updatedAt = subscription.getUpdatedAt();
    }

    public static ProductSubscriptionResponseDto from(ProductSubscription subscription) {
        return new ProductSubscriptionResponseDto(subscription);
    }
}