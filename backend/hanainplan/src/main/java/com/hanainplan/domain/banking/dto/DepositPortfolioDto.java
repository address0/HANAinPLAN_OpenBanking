package com.hanainplan.domain.banking.dto;

import com.hanainplan.domain.banking.entity.DepositPortfolio;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositPortfolioDto {

    private Long portfolioId;
    private Long userId;
    private String customerCi;
    private String bankCode;
    private String bankName;
    private String productCode;
    private String productName;
    private Long subscriptionId;
    private LocalDate subscriptionDate;
    private LocalDate maturityDate;
    private Integer contractPeriod;
    private String maturityPeriod;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private BigDecimal expectedInterest;
    private BigDecimal maturityAmount;
    private String status;
    private String irpAccountNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long daysRemaining;
    private Integer progressPercentage;

    public static DepositPortfolioDto from(DepositPortfolio portfolio) {
        DepositPortfolioDtoBuilder builder = DepositPortfolioDto.builder()
                .portfolioId(portfolio.getPortfolioId())
                .userId(portfolio.getUserId())
                .customerCi(portfolio.getCustomerCi())
                .bankCode(portfolio.getBankCode())
                .bankName(portfolio.getBankName())
                .productCode(portfolio.getProductCode())
                .productName(portfolio.getProductName())
                .subscriptionId(portfolio.getSubscriptionId())
                .subscriptionDate(portfolio.getSubscriptionDate())
                .maturityDate(portfolio.getMaturityDate())
                .contractPeriod(portfolio.getContractPeriod())
                .maturityPeriod(portfolio.getMaturityPeriod())
                .principalAmount(portfolio.getPrincipalAmount())
                .interestRate(portfolio.getInterestRate())
                .expectedInterest(portfolio.getExpectedInterest())
                .maturityAmount(portfolio.getMaturityAmount())
                .status(portfolio.getStatus())
                .irpAccountNumber(portfolio.getIrpAccountNumber())
                .createdAt(portfolio.getCreatedAt())
                .updatedAt(portfolio.getUpdatedAt());

        if (portfolio.getMaturityDate() != null) {
            long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), portfolio.getMaturityDate());
            builder.daysRemaining(daysRemaining > 0 ? daysRemaining : 0);

            if (portfolio.getSubscriptionDate() != null) {
                long totalDays = ChronoUnit.DAYS.between(portfolio.getSubscriptionDate(), portfolio.getMaturityDate());
                long elapsedDays = ChronoUnit.DAYS.between(portfolio.getSubscriptionDate(), LocalDate.now());

                if (totalDays > 0) {
                    int progress = (int) ((elapsedDays * 100) / totalDays);
                    builder.progressPercentage(Math.min(progress, 100));
                } else {
                    builder.progressPercentage(0);
                }
            }
        }

        return builder.build();
    }
}