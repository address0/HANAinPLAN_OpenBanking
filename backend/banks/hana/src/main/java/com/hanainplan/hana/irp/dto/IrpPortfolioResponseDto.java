package com.hanainplan.hana.irp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IrpPortfolioResponseDto {
    private String accountNumber;
    private BigDecimal totalValue;
    private BigDecimal cashBalance;
    private List<IrpDepositHoldingsResponseDto> depositHoldings;
    private List<IrpFundHoldingsResponseDto> fundHoldings;
    private LocalDateTime lastUpdated;
}




