package com.hanainplan.hana.fund.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 펀드 기준가 업데이트 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundNavUpdateRequest {

    /**
     * 자펀드 코드
     */
    @NotNull(message = "자펀드 코드는 필수입니다")
    private String childFundCd;

    /**
     * 기준일
     */
    @NotNull(message = "기준일은 필수입니다")
    private LocalDate navDate;

    /**
     * 기준가
     */
    @NotNull(message = "기준가는 필수입니다")
    private BigDecimal nav;
}

