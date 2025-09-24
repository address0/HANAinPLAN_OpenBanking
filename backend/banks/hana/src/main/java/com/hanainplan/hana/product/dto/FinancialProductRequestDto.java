package com.hanainplan.hana.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class FinancialProductRequestDto {

    @NotBlank(message = "상품코드는 필수입니다.")
    @Size(min = 1, max = 20, message = "상품코드는 1-20자 사이여야 합니다.")
    private String productCode; // 상품코드

    @NotBlank(message = "상품명은 필수입니다.")
    @Size(min = 1, max = 100, message = "상품명은 1-100자 사이여야 합니다.")
    private String productName; // 상품명

    @Size(max = 50, message = "예금종류는 50자를 초과할 수 없습니다.")
    private String depositType; // 예금종류

    private Integer minContractPeriod; // 가입기간(최소)

    private Integer maxContractPeriod; // 가입기간(최대)

    @Size(max = 10, message = "가입기간 단위는 10자를 초과할 수 없습니다.")
    private String contractPeriodUnit; // 가입기간 단위 (개월, 년)

    @Size(max = 100, message = "가입대상은 100자를 초과할 수 없습니다.")
    private String subscriptionTarget; // 가입대상

    private BigDecimal subscriptionAmount; // 가입금액

    @Size(max = 50, message = "상품유형은 50자를 초과할 수 없습니다.")
    private String productCategory; // 상품유형(선택)

    @Size(max = 100, message = "이자지급은 100자를 초과할 수 없습니다.")
    private String interestPayment; // 이자지급

    @Size(max = 200, message = "세제혜택은 200자를 초과할 수 없습니다.")
    private String taxBenefit; // 세제혜택(선택)

    @Size(max = 100, message = "일부해지는 100자를 초과할 수 없습니다.")
    private String partialWithdrawal; // 일부해지

    @Size(max = 200, message = "해지 시 불이익은 200자를 초과할 수 없습니다.")
    private String cancellationPenalty; // 해지 시 불이익(선택)

    @Size(max = 1000, message = "상품 설명은 1000자를 초과할 수 없습니다.")
    private String description; // 상품 설명
}
