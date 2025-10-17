package com.hanainplan.hana.fund.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundNavUpdateRequest {

    @NotNull(message = "자펀드 코드는 필수입니다")
    private String childFundCd;

    @NotNull(message = "기준일은 필수입니다")
    private LocalDate navDate;

    @NotNull(message = "기준가는 필수입니다")
    private BigDecimal nav;
}




