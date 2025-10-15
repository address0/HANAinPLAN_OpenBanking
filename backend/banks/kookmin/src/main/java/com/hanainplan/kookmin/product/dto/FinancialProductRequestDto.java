package com.hanainplan.kookmin.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class FinancialProductRequestDto {

    @NotBlank(message = "상품코드는 필수입니다.")
    @Size(min = 1, max = 20, message = "상품코드는 1-20자 사이여야 합니다.")
    private String productCode;

    @NotBlank(message = "상품명은 필수입니다.")
    @Size(min = 1, max = 100, message = "상품명은 1-100자 사이여야 합니다.")
    private String productName;

    @Size(max = 50, message = "예금종류는 50자를 초과할 수 없습니다.")
    private String depositType;

    private Integer minContractPeriod;

    private Integer maxContractPeriod;

    @Size(max = 10, message = "가입기간 단위는 10자를 초과할 수 없습니다.")
    private String contractPeriodUnit;

    @Size(max = 100, message = "가입대상은 100자를 초과할 수 없습니다.")
    private String subscriptionTarget;

    @Size(max = 50, message = "가입금액은 50자를 초과할 수 없습니다.")
    private String subscriptionAmount;

    @Size(max = 50, message = "상품유형은 50자를 초과할 수 없습니다.")
    private String productCategory;

    @Size(max = 100, message = "이자지급은 100자를 초과할 수 없습니다.")
    private String interestPayment;

    @Size(max = 200, message = "세제혜택은 200자를 초과할 수 없습니다.")
    private String taxBenefit;

    @Size(max = 100, message = "일부해지는 100자를 초과할 수 없습니다.")
    private String partialWithdrawal;

    @Size(max = 10, message = "예금자보호여부는 10자를 초과할 수 없습니다.")
    private String depositorProtection;

    @Size(max = 200, message = "거래방법은 200자를 초과할 수 없습니다.")
    private String transactionMethod;

    @Size(max = 1000, message = "유의사항은 1000자를 초과할 수 없습니다.")
    private String precautions;

    @Size(max = 200, message = "위법계약해지권은 200자를 초과할 수 없습니다.")
    private String contractCancellationRight;

    @Size(max = 200, message = "해지 시 불이익은 200자를 초과할 수 없습니다.")
    private String cancellationPenalty;

    @Size(max = 200, message = "지급관련제한은 200자를 초과할 수 없습니다.")
    private String paymentRestrictions;

    private Boolean isActive;

    private LocalDate startDate;

    private LocalDate endDate;
}