package com.hanainplan.samsung.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GeneralInsuranceRequestDto {

    @NotBlank(message = "상품 코드는 필수입니다.")
    @Size(max = 20, message = "상품 코드는 20자를 초과할 수 없습니다.")
    private String productCode;

    @NotBlank(message = "상품명은 필수입니다.")
    @Size(max = 100, message = "상품명은 100자를 초과할 수 없습니다.")
    private String productName;

    @Size(max = 50, message = "구분은 50자를 초과할 수 없습니다.")
    private String category;

    @Size(max = 100, message = "급부명칭은 100자를 초과할 수 없습니다.")
    private String benefitName;

    @Size(max = 200, message = "지급사유는 200자를 초과할 수 없습니다.")
    private String paymentReason;

    private BigDecimal paymentAmount;

    private BigDecimal subscriptionAmountBasic;

    private BigDecimal subscriptionAmountMale;

    private BigDecimal subscriptionAmountFemale;

    private BigDecimal interestRate;

    private BigDecimal insurancePriceIndexMale;

    private BigDecimal insurancePriceIndexFemale;

    @Size(max = 1000, message = "상품특징은 1000자를 초과할 수 없습니다.")
    private String productFeatures;

    private BigDecimal surrenderValue;

    @Size(max = 50, message = "갱신주기는 50자를 초과할 수 없습니다.")
    private String renewalCycle;

    @Size(max = 10, message = "유니버셜 여부는 10자를 초과할 수 없습니다.")
    private String isUniversal;

    @Size(max = 100, message = "판매채널은 100자를 초과할 수 없습니다.")
    private String salesChannel;

    @Size(max = 1000, message = "특이사항은 1000자를 초과할 수 없습니다.")
    private String specialNotes;

    @Size(max = 20, message = "대표번호는 20자를 초과할 수 없습니다.")
    private String representativeNumber;

    private Boolean isActive;
}
