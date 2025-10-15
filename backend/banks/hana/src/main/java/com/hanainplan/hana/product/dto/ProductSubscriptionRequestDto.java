package com.hanainplan.hana.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ProductSubscriptionRequestDto {

    @NotBlank(message = "고객 CI는 필수입니다.")
    @Size(min = 10, max = 64, message = "고객 CI는 10-64자 사이여야 합니다.")
    private String customerCi;

    @NotBlank(message = "상품코드는 필수입니다.")
    @Size(min = 1, max = 20, message = "상품코드는 1-20자 사이여야 합니다.")
    private String productCode;

    @NotBlank(message = "계좌번호는 필수입니다.")
    @Pattern(regexp = "^\\d{10,20}$", message = "계좌번호는 10-20자리 숫자여야 합니다.")
    private String accountNumber;

    @NotBlank(message = "상태는 필수입니다.")
    @Pattern(regexp = "^(ACTIVE|INACTIVE|MATURED|CANCELLED)$", message = "상태는 ACTIVE, INACTIVE, MATURED, CANCELLED 중 하나여야 합니다.")
    private String status;

    @NotNull(message = "가입일자는 필수입니다.")
    private LocalDate subscriptionDate;

    private LocalDate maturityDate;

    private Integer contractPeriod;

    @Size(max = 50, message = "만기기간은 50자를 초과할 수 없습니다.")
    private String maturityPeriod;

    @Pattern(regexp = "^(FIXED|VARIABLE)$", message = "금리유형은 FIXED 또는 VARIABLE이어야 합니다.")
    private String rateType;

    @NotNull(message = "기준금리는 필수입니다.")
    private BigDecimal baseRate;

    private BigDecimal preferentialRate;

    @NotNull(message = "최종 적용금리는 필수입니다.")
    private BigDecimal finalAppliedRate;

    @Size(max = 200, message = "우대사유는 200자를 초과할 수 없습니다.")
    private String preferentialReason;

    @Size(max = 50, message = "이자 계산기준은 50자를 초과할 수 없습니다.")
    private String interestCalculationBasis;

    @Pattern(regexp = "^(MATURITY|MONTHLY|QUARTERLY)$", message = "이자 지급방식은 MATURITY, MONTHLY, QUARTERLY 중 하나여야 합니다.")
    private String interestPaymentMethod;

    @Pattern(regexp = "^(SIMPLE|COMPOUND)$", message = "이자 유형은 SIMPLE 또는 COMPOUND여야 합니다.")
    private String interestType;

    @NotNull(message = "약정 원금은 필수입니다.")
    private BigDecimal contractPrincipal;

    @NotNull(message = "현재 잔액은 필수입니다.")
    private BigDecimal currentBalance;

    private BigDecimal unpaidInterest;

    private BigDecimal unpaidTax;

    private LocalDate lastInterestCalculationDate;

    private LocalDate nextInterestPaymentDate;

    @Size(max = 100, message = "취급점명은 100자를 초과할 수 없습니다.")
    private String branchName;

    private BigDecimal monthlyPaymentAmount;
    private Integer monthlyPaymentDay;
    private Integer totalInstallments;
    private Integer completedInstallments;
    private Integer missedInstallments;
}