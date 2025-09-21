package com.hanainplan.hana.subscription.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InsuranceSubscriptionRequestDto {

    @NotBlank(message = "고객 CI는 필수입니다.")
    @Size(max = 64, message = "고객 CI는 64자를 초과할 수 없습니다.")
    private String customerCi;

    @NotBlank(message = "상품코드는 필수입니다.")
    @Size(max = 20, message = "상품코드는 20자를 초과할 수 없습니다.")
    private String productCode;

    @NotBlank(message = "상품유형은 필수입니다.")
    @Pattern(regexp = "GENERAL|PENSION", message = "상품유형은 GENERAL 또는 PENSION이어야 합니다.")
    private String productType;

    @NotBlank(message = "증권번호는 필수입니다.")
    @Size(max = 30, message = "증권번호는 30자를 초과할 수 없습니다.")
    private String policyNumber;

    @NotNull(message = "가입일자는 필수입니다.")
    private LocalDate subscriptionDate;

    private LocalDate maturityDate;

    @NotNull(message = "보험료는 필수입니다.")
    private BigDecimal premiumAmount;

    @Pattern(regexp = "MONTHLY|QUARTERLY|YEARLY|SINGLE", message = "납입주기는 MONTHLY, QUARTERLY, YEARLY, SINGLE 중 하나여야 합니다.")
    private String paymentFrequency;

    private BigDecimal coverageAmount;

    @Size(max = 100, message = "수익자명은 100자를 초과할 수 없습니다.")
    private String beneficiaryName;

    @Size(max = 20, message = "수익자 관계는 20자를 초과할 수 없습니다.")
    private String beneficiaryRelation;

    @Pattern(regexp = "BANK_TRANSFER|CREDIT_CARD|CASH", message = "납입방법은 BANK_TRANSFER, CREDIT_CARD, CASH 중 하나여야 합니다.")
    private String paymentMethod;

    @Size(max = 50, message = "납입계좌는 50자를 초과할 수 없습니다.")
    private String paymentAccount;

    @Pattern(regexp = "ACTIVE|SUSPENDED|TERMINATED|EXPIRED", message = "가입상태는 ACTIVE, SUSPENDED, TERMINATED, EXPIRED 중 하나여야 합니다.")
    private String subscriptionStatus;

    @Pattern(regexp = "LOW|MEDIUM|HIGH", message = "위험평가는 LOW, MEDIUM, HIGH 중 하나여야 합니다.")
    private String riskAssessment;

    private Boolean medicalExamRequired;

    private LocalDate medicalExamDate;

    @Pattern(regexp = "PASS|FAIL|PENDING", message = "의료검진 결과는 PASS, FAIL, PENDING 중 하나여야 합니다.")
    private String medicalExamResult;

    @Size(max = 20, message = "설계사코드는 20자를 초과할 수 없습니다.")
    private String agentCode;

    @Size(max = 20, message = "지점코드는 20자를 초과할 수 없습니다.")
    private String branchCode;

    @Size(max = 500, message = "특별조건은 500자를 초과할 수 없습니다.")
    private String specialConditions;

    @Size(max = 500, message = "면책사항은 500자를 초과할 수 없습니다.")
    private String exclusions;

    private Integer waitingPeriod;

    private Integer gracePeriod;

    private Boolean renewalOption;

    private Boolean automaticRenewal;
}
