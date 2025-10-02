package com.hanainplan.hana.product.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 정기예금 가입 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositSubscriptionRequest {

    @NotBlank(message = "고객 CI는 필수입니다")
    private String customerCi;

    @NotBlank(message = "IRP 계좌번호는 필수입니다")
    private String irpAccountNumber;

    @NotBlank(message = "연결 주계좌 번호는 필수입니다")
    private String linkedAccountNumber;

    @NotBlank(message = "예금 상품 코드는 필수입니다")
    private String depositCode; // 예: HANA-DEP-001

    @NotNull(message = "상품 유형은 필수입니다")
    @Min(value = 0, message = "상품 유형은 0, 1, 2 중 하나여야 합니다")
    @Max(value = 2, message = "상품 유형은 0, 1, 2 중 하나여야 합니다")
    private Integer productType; // 0:일반, 1:디폴트옵션, 2:일단위

    @NotNull(message = "계약 기간은 필수입니다")
    @Min(value = 1, message = "계약 기간은 1 이상이어야 합니다")
    private Integer contractPeriod; // 개월(일반/디폴트) 또는 일(일단위)

    @NotNull(message = "가입 금액은 필수입니다")
    @DecimalMin(value = "10000", message = "가입 금액은 10,000원 이상이어야 합니다")
    private BigDecimal subscriptionAmount; // 신규 금액

    /**
     * 가입 기간 검증
     */
    public void validateContractPeriod() {
        switch (productType) {
            case 0: // 일반 상품
                if (contractPeriod != 6 && contractPeriod != 12 && contractPeriod != 24 
                    && contractPeriod != 36 && contractPeriod != 48 && contractPeriod != 60) {
                    throw new IllegalArgumentException(
                        "일반 상품은 6개월, 12~60개월(연 단위)만 가능합니다: " + contractPeriod + "개월");
                }
                break;
            case 1: // 디폴트옵션
                if (contractPeriod != 36) {
                    throw new IllegalArgumentException(
                        "디폴트옵션은 36개월(3년) 고정입니다: " + contractPeriod + "개월");
                }
                break;
            case 2: // 일단위
                if (contractPeriod < 31 || contractPeriod > 1825) {
                    throw new IllegalArgumentException(
                        "일단위 상품은 31일 이상 1825일(5년) 이하만 가능합니다: " + contractPeriod + "일");
                }
                break;
            default:
                throw new IllegalArgumentException("지원하지 않는 상품 유형입니다: " + productType);
        }
    }

    /**
     * 계약 기간 단위 반환
     */
    public String getContractPeriodUnit() {
        return productType == 2 ? "일" : "개월";
    }
}


