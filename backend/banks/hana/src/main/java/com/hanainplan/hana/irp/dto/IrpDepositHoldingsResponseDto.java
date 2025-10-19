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
public class IrpDepositHoldingsResponseDto {
    private Long subscriptionId;
    private String productCode;
    private String productName;
    private BigDecimal principalAmount;
    private BigDecimal currentValue;
    private BigDecimal interestRate;
    private LocalDateTime subscriptionDate;
    private LocalDateTime maturityDate;
    private String status;
}




