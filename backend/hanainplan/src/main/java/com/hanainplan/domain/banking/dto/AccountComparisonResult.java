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
public class AccountComparisonResult {
    private BigDecimal investmentAmount;
    private BigDecimal expectedReturn;
    
    private BigDecimal regularAccountTax;
    private BigDecimal regularAccountNetReturn;
    
    private BigDecimal irpTaxDeduction;
    private BigDecimal irpPensionTax;
    private BigDecimal irpNetReturn;
    
    private BigDecimal totalIrpBenefit;
    private BigDecimal advantageAmount;
    private BigDecimal advantageRate;
}

