package com.hanainplan.domain.banking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositRecommendationRequest {

    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;

    @NotNull(message = "은퇴 예정일은 필수입니다")
    private LocalDate retirementDate;

    @NotNull(message = "예치 희망 금액은 필수입니다")
    private BigDecimal depositAmount;
}