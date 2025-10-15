package com.hanainplan.hana.user.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IrpDepositRequest {

    @NotBlank(message = "계좌번호는 필수입니다")
    private String accountNumber;

    @NotNull(message = "입금액은 필수입니다")
    @DecimalMin(value = "0.01", message = "입금액은 0.01원 이상이어야 합니다")
    private BigDecimal amount;

    private String description;
}