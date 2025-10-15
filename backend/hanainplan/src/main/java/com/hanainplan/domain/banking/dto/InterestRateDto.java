package com.hanainplan.domain.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterestRateDto {
    private String bankCode;
    private String bankName;
    private String productCode;
    private String productName;
    private String maturityPeriod;
    private BigDecimal interestRate;
    private String interestType;
    private Boolean isIrp;
}