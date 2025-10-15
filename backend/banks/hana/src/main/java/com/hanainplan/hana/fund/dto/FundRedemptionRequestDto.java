package com.hanainplan.hana.fund.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundRedemptionRequestDto {

    private String customerCi;
    private Long subscriptionId;

    private BigDecimal sellUnits;
    private Boolean sellAll;

    public void validate() {
        if (customerCi == null || customerCi.isBlank()) {
            throw new IllegalArgumentException("고객 CI는 필수입니다");
        }
        if (subscriptionId == null) {
            throw new IllegalArgumentException("가입 ID는 필수입니다");
        }
        if (sellAll == null || !sellAll) {
            if (sellUnits == null || sellUnits.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("매도 좌수는 0보다 커야 합니다");
            }
        }
    }

    public boolean isSellAll() {
        return sellAll != null && sellAll;
    }
}