package com.hanainplan.domain.banking.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class DepositSubscriptionRequestDto {

    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;

    @NotBlank(message = "은행 코드는 필수입니다")
    private String bankCode;

    @NotBlank(message = "IRP 계좌번호는 필수입니다")
    private String irpAccountNumber;

    @NotBlank(message = "연결 주계좌번호는 필수입니다")
    private String linkedAccountNumber;

    @NotBlank(message = "상품 코드는 필수입니다")
    private String depositCode;

    private String productName;

    @NotNull(message = "상품 유형은 필수입니다")
    private Integer productType;

    @NotNull(message = "계약 기간은 필수입니다")
    private Integer contractPeriod;

    @NotNull(message = "가입 금액은 필수입니다")
    @DecimalMin(value = "1000000", message = "가입 금액은 최소 100만원 이상이어야 합니다")
    private BigDecimal subscriptionAmount;
}