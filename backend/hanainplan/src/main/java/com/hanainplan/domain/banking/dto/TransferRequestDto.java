package com.hanainplan.domain.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequestDto {

    @NotNull(message = "출금 계좌 ID는 필수입니다")
    private Long fromAccountId;

    @NotNull(message = "입금 계좌 ID는 필수입니다")
    private Long toAccountId;

    @NotNull(message = "이체 금액은 필수입니다")
    @DecimalMin(value = "0.01", message = "이체 금액은 0.01원 이상이어야 합니다")
    private BigDecimal amount;

    @Size(max = 200, message = "거래 설명은 200자를 초과할 수 없습니다")
    private String description;

    @Size(max = 500, message = "메모는 500자를 초과할 수 없습니다")
    private String memo;

    private String referenceNumber;
}