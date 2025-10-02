package com.hanainplan.domain.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 최적 예금 상품 추천 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimalDepositRecommendation {

    // 추천 상품 정보
    private String depositCode;
    private String depositName;
    private String bankCode;
    private String bankName;
    
    // 추천 가입 조건
    private Integer productType; // 0:일반, 1:디폴트옵션, 2:일단위
    private String productTypeName;
    private Integer contractPeriod; // 개월 또는 일
    private String contractPeriodUnit; // "개월" 또는 "일"
    private BigDecimal recommendedAmount; // 추천 예치 금액
    
    // 예상 수익 정보
    private BigDecimal appliedRate; // 적용 금리
    private BigDecimal expectedInterest; // 예상 이자
    private BigDecimal expectedMaturityAmount; // 예상 만기 금액
    private LocalDate expectedMaturityDate; // 예상 만기일
    
    // 추천 근거
    private String recommendationReason;
    private Integer yearsToRetirement; // 은퇴까지 남은 연수
    private BigDecimal currentIrpBalance; // 현재 IRP 잔액
    private BigDecimal targetAmount; // 목표 금액
    private BigDecimal shortfall; // 부족 금액
}


