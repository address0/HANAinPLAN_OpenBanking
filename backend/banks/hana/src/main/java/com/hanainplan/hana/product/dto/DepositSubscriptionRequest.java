package com.hanainplan.hana.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositSubscriptionRequest {

    @NotBlank(message = "고객 CI는 필수입니다")
    private String customerCi;

    @NotBlank(message = "고객명은 필수입니다")
    private String customerName;

    @NotBlank(message = "생년월일은 필수입니다")
    private String birthDate;

    @NotBlank(message = "성별은 필수입니다")
    private String gender;

    @NotBlank(message = "전화번호는 필수입니다")
    private String phone;

    @NotBlank(message = "상품코드는 필수입니다")
    private String productCode;

    @NotBlank(message = "출금 계좌번호는 필수입니다")
    private String accountNumber;

    @NotBlank(message = "IRP 계좌번호는 필수입니다")
    private String irpAccountNumber;

    @NotBlank(message = "상태는 필수입니다")
    private String status;

    @NotBlank(message = "가입일자는 필수입니다")
    private String subscriptionDate;

    @NotBlank(message = "만기일자는 필수입니다")
    private String maturityDate;

    @NotNull(message = "계약기간은 필수입니다")
    private Integer contractPeriod;

    @NotBlank(message = "만기기간은 필수입니다")
    private String maturityPeriod;

    @NotBlank(message = "금리유형은 필수입니다")
    private String rateType;

    @NotBlank(message = "이자계산기준은 필수입니다")
    private String interestCalculationBasis;

    @NotBlank(message = "이자지급방법은 필수입니다")
    private String interestPaymentMethod;

    @NotNull(message = "계약원금은 필수입니다")
    private BigDecimal contractPrincipal;

    @NotNull(message = "현재잔액은 필수입니다")
    private BigDecimal currentBalance;

    @NotBlank(message = "지점명은 필수입니다")
    private String branchName;

    @NotNull(message = "기본 금리는 필수입니다")
    private BigDecimal baseRate;

    private BigDecimal preferentialRate;

    @NotNull(message = "최종 적용 금리는 필수입니다")
    private BigDecimal finalAppliedRate;
}