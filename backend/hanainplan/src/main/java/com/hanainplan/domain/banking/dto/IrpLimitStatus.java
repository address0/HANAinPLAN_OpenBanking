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
public class IrpLimitStatus {
    private Integer year;
    private BigDecimal totalDeposited;
    private BigDecimal limit;
    private BigDecimal remaining;
    private BigDecimal usageRate;
    private Boolean isLimitReached;

    public static IrpLimitStatus of(Integer year, BigDecimal totalDeposited, BigDecimal limit) {
        BigDecimal remaining = limit.subtract(totalDeposited);
        BigDecimal usageRate = totalDeposited.divide(limit, 4, java.math.RoundingMode.HALF_UP);
        Boolean isLimitReached = remaining.compareTo(BigDecimal.ZERO) <= 0;

        return IrpLimitStatus.builder()
                .year(year)
                .totalDeposited(totalDeposited)
                .limit(limit)
                .remaining(remaining)
                .usageRate(usageRate)
                .isLimitReached(isLimitReached)
                .build();
    }
}

