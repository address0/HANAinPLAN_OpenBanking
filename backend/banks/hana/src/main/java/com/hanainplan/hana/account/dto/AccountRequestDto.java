package com.hanainplan.hana.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class AccountRequestDto {

    @NotNull(message = "계좌 종류는 필수입니다.")
    private Integer accountType;

    @NotNull(message = "잔액은 필수입니다.")
    private BigDecimal balance;

    @NotNull(message = "계좌개설일은 필수입니다.")
    private LocalDate openingDate;

    @NotBlank(message = "고객 CI는 필수입니다.")
    @Pattern(regexp = "^[A-Za-z0-9+/=]{20,100}$", message = "CI 형식이 올바르지 않습니다.")
    private String customerCi;
}