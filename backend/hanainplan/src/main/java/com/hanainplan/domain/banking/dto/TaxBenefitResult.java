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
public class TaxBenefitResult {
    private BigDecimal depositAmount;
    private BigDecimal deductibleAmount;
    private BigDecimal deductionRate;
    private BigDecimal taxDeduction;
    private BigDecimal effectiveSavings;
    private String salaryBracket;
}

