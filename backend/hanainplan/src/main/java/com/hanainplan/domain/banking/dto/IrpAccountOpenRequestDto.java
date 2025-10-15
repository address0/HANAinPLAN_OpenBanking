package com.hanainplan.domain.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IrpAccountOpenRequestDto {

    @NotNull(message = "고객 ID는 필수입니다")
    private Long customerId;

    @Size(min = 10, max = 100, message = "고객 CI는 10자 이상 100자 이하여야 합니다")
    private String customerCi;

    @NotNull(message = "초기 납입금액은 필수입니다")
    @Min(value = 50000, message = "초기 납입금액은 최소 5만원 이상이어야 합니다")
    @Max(value = 10000000, message = "초기 납입금액은 최대 1,000만원 이하여야 합니다")
    private Integer initialDeposit;

    @Min(value = 0, message = "월 납입금액은 0원 이상이어야 합니다")
    @Max(value = 10000000, message = "월 납입금액은 최대 1,000만원 이하여야 합니다")
    private Integer monthlyDeposit;

    @NotNull(message = "자동납입 여부는 필수입니다")
    private Boolean isAutoDeposit;

    @Min(value = 1, message = "자동납입일은 1일 이상이어야 합니다")
    @Max(value = 31, message = "자동납입일은 31일 이하여야 합니다")
    private Integer depositDay;

    @NotBlank(message = "투자성향은 필수입니다")
    @Pattern(regexp = "^(CONSERVATIVE|MODERATE_CONSERVATIVE|MODERATE|AGGRESSIVE)$",
             message = "투자성향은 CONSERVATIVE, MODERATE_CONSERVATIVE, MODERATE, AGGRESSIVE 중 하나여야 합니다")
    private String investmentStyle;

    @NotBlank(message = "연결 주계좌는 필수입니다")
    @Size(min = 10, max = 50, message = "계좌번호 형식이 올바르지 않습니다")
    private String linkedMainAccount;
}