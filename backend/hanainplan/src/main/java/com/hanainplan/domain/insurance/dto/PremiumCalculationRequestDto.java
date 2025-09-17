package com.hanainplan.domain.insurance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PremiumCalculationRequestDto {
    private String productId;
    private Integer age;
    private String gender;
    private Long coverageAmount;
    private Integer paymentPeriod;
    private Integer coveragePeriod;
    private String paymentFrequency;
    private List<String> riders;
    private List<String> medicalHistory;
}

