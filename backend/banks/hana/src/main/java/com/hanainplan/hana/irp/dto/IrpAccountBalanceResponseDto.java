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
public class IrpAccountBalanceResponseDto {
    private String accountNumber;
    private BigDecimal cashBalance;
    private LocalDateTime lastUpdated;
}




