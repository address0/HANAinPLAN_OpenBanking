package com.hanainplan.domain.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 금리 정보 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterestRateDto {
    private String bankCode; // HANA, KOOKMIN, SHINHAN
    private String bankName; // 하나은행, 국민은행, 신한은행
    private String productCode;
    private String productName;
    private String maturityPeriod; // "6개월", "1년", "2년", "3년", "5년"
    private BigDecimal interestRate; // 금리 (예: 0.0230 = 2.30%)
    private String interestType; // BASIC, PREFERENTIAL
    private Boolean isIrp; // IRP 여부
}

