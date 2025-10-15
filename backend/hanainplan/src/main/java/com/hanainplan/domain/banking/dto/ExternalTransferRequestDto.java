package com.hanainplan.domain.banking.dto;

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
public class ExternalTransferRequestDto {

    @NotNull(message = "출금 계좌 ID는 필수입니다")
    private Long fromAccountId;

    @NotBlank(message = "수신 계좌번호는 필수입니다")
    private String toAccountNumber;

    @NotNull(message = "송금 금액은 필수입니다")
    @DecimalMin(value = "0.01", message = "송금 금액은 0.01원 이상이어야 합니다")
    private BigDecimal amount;

    private String description;
}