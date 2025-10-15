package com.hanainplan.shinhan.product.dto;

import com.hanainplan.shinhan.product.entity.InterestRate;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class InterestRateResponseDto {

    private Long interestRateId;
    private String productCode;
    private InterestRate.InterestType interestType;
    private String maturityPeriod;
    private BigDecimal interestRate;
    private Boolean isIrp;
    private LocalDate effectiveDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public InterestRateResponseDto() {}

    public InterestRateResponseDto(InterestRate interestRate) {
        this.interestRateId = interestRate.getInterestRateId();
        this.productCode = interestRate.getProductCode();
        this.interestType = interestRate.getInterestType();
        this.maturityPeriod = interestRate.getMaturityPeriod();
        this.interestRate = interestRate.getInterestRate();
        this.isIrp = interestRate.getIsIrp();
        this.effectiveDate = interestRate.getEffectiveDate();
        this.createdAt = interestRate.getCreatedAt();
        this.updatedAt = interestRate.getUpdatedAt();
    }

    public static InterestRateResponseDto from(InterestRate interestRate) {
        return new InterestRateResponseDto(interestRate);
    }
}