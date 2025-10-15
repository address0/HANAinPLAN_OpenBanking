package com.hanainplan.domain.banking.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositSubscriptionRequest {

    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;

    @NotBlank(message = "은행 코드는 필수입니다")
    private String bankCode;

    @NotBlank(message = "IRP 계좌번호는 필수입니다")
    private String irpAccountNumber;

    @NotBlank(message = "연결 주계좌 번호는 필수입니다")
    private String linkedAccountNumber;

    @NotBlank(message = "예금 상품 코드는 필수입니다")
    private String depositCode;

    @NotNull(message = "상품 유형은 필수입니다")
    @Min(value = 0, message = "상품 유형은 0, 1, 2 중 하나여야 합니다")
    @Max(value = 2, message = "상품 유형은 0, 1, 2 중 하나여야 합니다")
    private Integer productType;

    @NotNull(message = "계약 기간은 필수입니다")
    @Min(value = 1, message = "계약 기간은 1 이상이어야 합니다")
    private Integer contractPeriod;

    @NotNull(message = "가입 금액은 필수입니다")
    @DecimalMin(value = "10000", message = "가입 금액은 10,000원 이상이어야 합니다")
    private BigDecimal subscriptionAmount;

    public void validateContractPeriod() {
        switch (productType) {
            case 0:
                if (contractPeriod != 6 && contractPeriod != 12 && contractPeriod != 24 
                    && contractPeriod != 36 && contractPeriod != 48 && contractPeriod != 60) {
                    throw new IllegalArgumentException("일반 상품은 6개월, 12~60개월(연 단위)만 가능합니다");
                }
                break;
            case 1:
                if (contractPeriod != 36) {
                    throw new IllegalArgumentException("디폴트옵션은 36개월(3년) 고정입니다");
                }
                break;
            case 2:
                if (contractPeriod < 31 || contractPeriod > 1825) {
                    throw new IllegalArgumentException("일단위 상품은 31일 이상 1825일(5년) 이하만 가능합니다");
                }
                break;
        }
    }

    public String getContractPeriodUnit() {
        return productType == 2 ? "일" : "개월";
    }
}