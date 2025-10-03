package com.hanainplan.domain.banking.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 정기예금 상품 가입 요청 DTO
 * - 프론트엔드에서 IRP 계좌 기반으로 정기예금 가입 요청
 * - 항상 하나은행 IRP 서버를 통해 가입 처리
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DepositSubscriptionRequestDto {

    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;

    @NotBlank(message = "은행 코드는 필수입니다")
    private String bankCode; // 추천된 은행 코드 (HANA, KOOKMIN, SHINHAN)

    @NotBlank(message = "IRP 계좌번호는 필수입니다")
    private String irpAccountNumber; // IRP 계좌번호

    @NotBlank(message = "연결 주계좌번호는 필수입니다")
    private String linkedAccountNumber; // 출금할 연결 주계좌

    @NotBlank(message = "상품 코드는 필수입니다")
    private String depositCode; // 상품 코드

    private String productName; // 상품명 (선택사항, 없으면 depositCode 사용)

    @NotNull(message = "상품 유형은 필수입니다")
    private Integer productType; // 0:일반, 1:디폴트옵션

    @NotNull(message = "계약 기간은 필수입니다")
    private Integer contractPeriod; // 계약 기간 (개월)

    @NotNull(message = "가입 금액은 필수입니다")
    @DecimalMin(value = "1000000", message = "가입 금액은 최소 100만원 이상이어야 합니다")
    private BigDecimal subscriptionAmount; // 가입 금액
}

