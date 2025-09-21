package com.hanainplan.hana.product.dto;

import com.hanainplan.hana.product.entity.GeneralInsurance;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class GeneralInsuranceResponseDto {
    private Long productId;
    private String productCode;
    private String productName;
    private String category;
    private String benefitName;
    private String paymentReason;
    private BigDecimal paymentAmount;
    private BigDecimal subscriptionAmountBasic;
    private BigDecimal subscriptionAmountMale;
    private BigDecimal subscriptionAmountFemale;
    private BigDecimal interestRate;
    private BigDecimal insurancePriceIndexMale;
    private BigDecimal insurancePriceIndexFemale;
    private String productFeatures;
    private BigDecimal surrenderValue;
    private String renewalCycle;
    private String isUniversal;
    private String salesChannel;
    private String specialNotes;
    private String representativeNumber;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static GeneralInsuranceResponseDto fromEntity(GeneralInsurance entity) {
        GeneralInsuranceResponseDto dto = new GeneralInsuranceResponseDto();
        dto.setProductId(entity.getProductId());
        dto.setProductCode(entity.getProductCode());
        dto.setProductName(entity.getProductName());
        dto.setCategory(entity.getCategory());
        dto.setBenefitName(entity.getBenefitName());
        dto.setPaymentReason(entity.getPaymentReason());
        dto.setPaymentAmount(entity.getPaymentAmount());
        dto.setSubscriptionAmountBasic(entity.getSubscriptionAmountBasic());
        dto.setSubscriptionAmountMale(entity.getSubscriptionAmountMale());
        dto.setSubscriptionAmountFemale(entity.getSubscriptionAmountFemale());
        dto.setInterestRate(entity.getInterestRate());
        dto.setInsurancePriceIndexMale(entity.getInsurancePriceIndexMale());
        dto.setInsurancePriceIndexFemale(entity.getInsurancePriceIndexFemale());
        dto.setProductFeatures(entity.getProductFeatures());
        dto.setSurrenderValue(entity.getSurrenderValue());
        dto.setRenewalCycle(entity.getRenewalCycle());
        dto.setIsUniversal(entity.getIsUniversal());
        dto.setSalesChannel(entity.getSalesChannel());
        dto.setSpecialNotes(entity.getSpecialNotes());
        dto.setRepresentativeNumber(entity.getRepresentativeNumber());
        dto.setIsActive(entity.getIsActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
