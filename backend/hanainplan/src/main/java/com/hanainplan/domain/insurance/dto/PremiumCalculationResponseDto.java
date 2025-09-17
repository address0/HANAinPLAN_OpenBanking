package com.hanainplan.domain.insurance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PremiumCalculationResponseDto {
    private Long basePremium;
    private Long riderPremium;
    private Long totalPremium;
    private Long discount;
    private Long finalPremium;
    private List<PremiumBreakdownDto> breakdown;
}


