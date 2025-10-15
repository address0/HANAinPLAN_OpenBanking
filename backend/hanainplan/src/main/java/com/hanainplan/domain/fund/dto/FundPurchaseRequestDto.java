package com.hanainplan.domain.fund.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundPurchaseRequestDto {

    private Long userId;
    private String childFundCd;
    private BigDecimal purchaseAmount;

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