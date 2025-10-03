package com.hanainplan.domain.fund.dto;

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
public class FundPurchaseRequest {

    private Long userId; // 사용자 ID
    private String customerCi; // 고객 CI
    private String fundCode; // 펀드 코드
    private String irpAccountNumber; // IRP 계좌번호
    private BigDecimal purchaseAmount; // 매수 금액
    private String bankCode; // 은행 코드 (기본값: HANA)

    /**
     * 유효성 검증
     */
    public void validate() {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다");
        }
        if (customerCi == null || customerCi.isBlank()) {
            throw new IllegalArgumentException("고객 CI는 필수입니다");
        }
        if (fundCode == null || fundCode.isBlank()) {
            throw new IllegalArgumentException("펀드 코드는 필수입니다");
        }
        if (irpAccountNumber == null || irpAccountNumber.isBlank()) {
            throw new IllegalArgumentException("IRP 계좌번호는 필수입니다");
        }
        if (purchaseAmount == null || purchaseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("매수 금액은 0보다 커야 합니다");
        }
        if (bankCode == null || bankCode.isBlank()) {
            bankCode = "HANA"; // 기본값
        }
    }
}

