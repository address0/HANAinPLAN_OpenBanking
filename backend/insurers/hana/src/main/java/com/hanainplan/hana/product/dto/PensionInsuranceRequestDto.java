package com.hanainplan.hana.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PensionInsuranceRequestDto {

    @NotBlank(message = "상품 코드는 필수입니다.")
    @Size(max = 20, message = "상품 코드는 20자를 초과할 수 없습니다.")
    private String productCode;

    @NotBlank(message = "상품명은 필수입니다.")
    @Size(max = 100, message = "상품명은 100자를 초과할 수 없습니다.")
    private String productName;

    @Size(max = 50, message = "유지기간은 50자를 초과할 수 없습니다.")
    private String maintenancePeriod;

    private BigDecimal premiumPayment;

    private BigDecimal contractorAccumulationMale;

    private BigDecimal accumulationRateMale;

    private BigDecimal surrenderValueMale;

    private BigDecimal contractorAccumulationFemale;

    private BigDecimal accumulationRateFemale;

    private BigDecimal surrenderValueFemale;

    private BigDecimal expectedReturnRateMinimum;

    private BigDecimal expectedReturnRateCurrent;

    private BigDecimal expectedReturnRateAverage;

    private BigDecimal businessExpenseRatio;

    @Size(max = 200, message = "위험보장은 200자를 초과할 수 없습니다.")
    private String riskCoverage;

    private BigDecimal currentAnnouncedRate;

    @Size(max = 20, message = "최저보증이율은 20자를 초과할 수 없습니다.")
    private String minimumGuaranteedRate;

    @Size(max = 50, message = "가입유형은 50자를 초과할 수 없습니다.")
    private String subscriptionType;

    @Size(max = 10, message = "유니버셜 여부는 10자를 초과할 수 없습니다.")
    private String isUniversal;

    @Size(max = 100, message = "납입방법은 100자를 초과할 수 없습니다.")
    private String paymentMethod;

    @Size(max = 100, message = "판매채널은 100자를 초과할 수 없습니다.")
    private String salesChannel;

    @Size(max = 1000, message = "특이사항은 1000자를 초과할 수 없습니다.")
    private String specialNotes;

    @Size(max = 20, message = "대표번호는 20자를 초과할 수 없습니다.")
    private String representativeNumber;

    private Boolean isActive;
}
