package com.hanainplan.domain.fund.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 펀드 매수 요청 DTO (하나인플랜)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundPurchaseRequestDto {

    private Long userId;                    // 사용자 ID (백엔드에서 CI 조회)
    private String childFundCd;             // 클래스 펀드 코드 (예: 51306P)
    private BigDecimal purchaseAmount;      // 매수 금액

    /**
     * 유효성 검증
     */
    public void validate() {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다");
        }
        if (childFundCd == null || childFundCd.isBlank()) {
            throw new IllegalArgumentException("펀드 클래스 코드는 필수입니다");
        }
        if (purchaseAmount == null || purchaseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("매수 금액은 0보다 커야 합니다");
        }
    }
}

