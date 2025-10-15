package com.hanainplan.domain.banking.dto;

import com.hanainplan.domain.banking.entity.BankingAccount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {

    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;

    @NotNull(message = "계좌 유형은 필수입니다")
    private Integer accountType;

    @NotBlank(message = "계좌명은 필수입니다")
    private String accountName;

    private BigDecimal initialBalance;

    private String description;

    private String purpose;

    private BigDecimal monthlyDepositAmount;

    private Integer depositPeriod;

    private String interestPaymentMethod;

    @Pattern(regexp = "^.{4,}$", message = "계좌 비밀번호는 4자리 이상이어야 합니다")
    private String accountPassword;
}