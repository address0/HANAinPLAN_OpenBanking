package com.hanainplan.hana.irp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IrpFundHoldingsResponseDto {
    private Long subscriptionId;
    private String fundCode;
    private String fundName;
    private BigDecimal units;
    private BigDecimal currentNav;
    private BigDecimal purchaseNav;
    private BigDecimal currentValue;
    private BigDecimal purchaseAmount;
    private BigDecimal totalReturn;
    private BigDecimal returnRate;
    private LocalDateTime subscriptionDate;
    private String status;
}




