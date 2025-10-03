package com.hanainplan.hana.fund.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 펀드 매수 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundPurchaseRequestDto {

    private String customerCi;              // 고객 CI
    private String childFundCd;             // 클래스 펀드 코드 (예: 51306P)
    private BigDecimal purchaseAmount;      // 매수 금액
    
    // irpAccountNumber는 백엔드에서 customerCi로 자동 조회하므로 불필요
    // private String irpAccountNumber;

    /**
     * 유효성 검증
     */
    public void validate() {
        if (customerCi == null || customerCi.isBlank()) {
            throw new IllegalArgumentException("고객 CI는 필수입니다");
        }
        if (childFundCd == null || childFundCd.isBlank()) {
            throw new IllegalArgumentException("펀드 클래스 코드는 필수입니다");
        }
        if (purchaseAmount == null || purchaseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("매수 금액은 0보다 커야 합니다");
        }
    }
}

