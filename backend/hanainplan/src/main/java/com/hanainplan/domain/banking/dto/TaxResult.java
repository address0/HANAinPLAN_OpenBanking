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
public class TaxResult {
    private BigDecimal grossAmount;
    private BigDecimal incomeTax;
    private BigDecimal localTax;
    private BigDecimal totalTax;
    private BigDecimal netAmount;
    private BigDecimal taxRate;
    private Boolean pensionTaxApplied;
    private String taxType;
}

