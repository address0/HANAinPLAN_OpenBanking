package com.hanainplan.hana.fund.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 펀드 매도(환매) 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundRedemptionRequestDto {

    private String customerCi;              // 고객 CI
    private Long subscriptionId;            // 가입 ID
    
    // irpAccountNumber는 백엔드에서 customerCi로 자동 조회하므로 불필요
    // private String irpAccountNumber;
    
    // 매도 수량 (둘 중 하나만 사용)
    private BigDecimal sellUnits;           // 매도 좌수 (null이면 전량 매도)
    private Boolean sellAll;                // 전량 매도 여부

    /**
     * 유효성 검증
     */
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

    /**
     * 전량 매도 여부 확인
     */
    public boolean isSellAll() {
        return sellAll != null && sellAll;
    }
}

