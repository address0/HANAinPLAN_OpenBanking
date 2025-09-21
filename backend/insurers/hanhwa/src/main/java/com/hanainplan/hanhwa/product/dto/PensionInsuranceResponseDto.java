package com.hanainplan.hanhwa.product.dto;

import com.hanainplan.hanhwa.product.entity.PensionInsurance;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PensionInsuranceResponseDto {
    private Long productId;
    private String productCode;
    private String productName;
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
    private String riskCoverage;
    private BigDecimal currentAnnouncedRate;
    private String minimumGuaranteedRate;
    private String subscriptionType;
    private String isUniversal;
    private String paymentMethod;
    private String salesChannel;
    private String specialNotes;
    private String representativeNumber;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PensionInsuranceResponseDto fromEntity(PensionInsurance entity) {
        PensionInsuranceResponseDto dto = new PensionInsuranceResponseDto();
        dto.setProductId(entity.getProductId());
        dto.setProductCode(entity.getProductCode());
        dto.setProductName(entity.getProductName());
        dto.setMaintenancePeriod(entity.getMaintenancePeriod());
        dto.setPremiumPayment(entity.getPremiumPayment());
        dto.setContractorAccumulationMale(entity.getContractorAccumulationMale());
        dto.setAccumulationRateMale(entity.getAccumulationRateMale());
        dto.setSurrenderValueMale(entity.getSurrenderValueMale());
        dto.setContractorAccumulationFemale(entity.getContractorAccumulationFemale());
        dto.setAccumulationRateFemale(entity.getAccumulationRateFemale());
        dto.setSurrenderValueFemale(entity.getSurrenderValueFemale());
        dto.setExpectedReturnRateMinimum(entity.getExpectedReturnRateMinimum());
        dto.setExpectedReturnRateCurrent(entity.getExpectedReturnRateCurrent());
        dto.setExpectedReturnRateAverage(entity.getExpectedReturnRateAverage());
        dto.setBusinessExpenseRatio(entity.getBusinessExpenseRatio());
        dto.setRiskCoverage(entity.getRiskCoverage());
        dto.setCurrentAnnouncedRate(entity.getCurrentAnnouncedRate());
        dto.setMinimumGuaranteedRate(entity.getMinimumGuaranteedRate());
        dto.setSubscriptionType(entity.getSubscriptionType());
        dto.setIsUniversal(entity.getIsUniversal());
        dto.setPaymentMethod(entity.getPaymentMethod());
        dto.setSalesChannel(entity.getSalesChannel());
        dto.setSpecialNotes(entity.getSpecialNotes());
        dto.setRepresentativeNumber(entity.getRepresentativeNumber());
        dto.setIsActive(entity.getIsActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
