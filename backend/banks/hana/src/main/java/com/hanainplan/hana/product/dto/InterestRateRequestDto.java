package com.hanainplan.hana.product.dto;

import com.hanainplan.hana.product.entity.InterestRate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class InterestRateRequestDto {

    @NotBlank(message = "상품코드는 필수입니다.")
    @Size(min = 1, max = 20, message = "상품코드는 1-20자 사이여야 합니다.")
    private String productCode;

    @NotNull(message = "금리종류는 필수입니다.")
    private InterestRate.InterestType interestType;

    @Size(max = 50, message = "만기기간은 50자를 초과할 수 없습니다.")
    private String maturityPeriod;

    @NotNull(message = "금리는 필수입니다.")
    private BigDecimal interestRate;

    private Boolean isIrp;

    @NotNull(message = "적용일자는 필수입니다.")
    private LocalDate effectiveDate;
}