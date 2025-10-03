package com.hanainplan.domain.fund.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 펀드 매도(환매) 요청 DTO (하나인플랜)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundRedemptionRequestDto {

    private Long userId;                    // 사용자 ID (백엔드에서 CI 조회)
    private Long subscriptionId;            // 가입 ID
    
    // 매도 수량 (둘 중 하나만 사용)
    private BigDecimal sellUnits;           // 매도 좌수 (null이면 전량 매도)
    private Boolean sellAll;                // 전량 매도 여부

    /**
     * 유효성 검증
     */
    public void validate() {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다");
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

