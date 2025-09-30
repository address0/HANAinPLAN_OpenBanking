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
    private String productCode; // 상품코드(논리적fk. 실제 외래키 설정X)

    @NotNull(message = "금리종류는 필수입니다.")
    private InterestRate.InterestType interestType; // 금리종류(기본/우대/만기후/중도해지)

    @Size(max = 50, message = "만기기간은 50자를 초과할 수 없습니다.")
    private String maturityPeriod; // 만기기간

    @NotNull(message = "금리는 필수입니다.")
    private BigDecimal interestRate; // 금리(%)

    private Boolean isIrp; // IRP여부

    @NotNull(message = "적용일자는 필수입니다.")
    private LocalDate effectiveDate; // 적용일자
}





