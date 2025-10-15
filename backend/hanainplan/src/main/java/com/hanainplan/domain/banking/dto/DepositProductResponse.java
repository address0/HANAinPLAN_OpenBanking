package com.hanainplan.domain.banking.dto;

import com.hanainplan.domain.banking.entity.DepositProduct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositProductResponse {

    private String depositCode;
    private String name;
    private String bankCode;
    private String bankName;
    private String description;

    private String rateInfo;

    public static DepositProductResponse fromEntity(DepositProduct product) {
        return DepositProductResponse.builder()
                .depositCode(product.getDepositCode())
                .name(product.getName())
                .bankCode(product.getBankCode())
                .bankName(product.getBankName())
                .description(product.getDescription())
                .rateInfo(getRateInfoByBankCode(product.getBankCode()))
                .build();
    }

    private static String getRateInfoByBankCode(String bankCode) {
        switch (bankCode.toUpperCase()) {
            case "HANA":
                return "6개월: 2.07%, 1년: 2.40%, 2년: 2.00%, 3년: 2.10%, 4년: 2.00%, 5년: 2.02%";
            case "SHINHAN":
                return "6개월: 2.10%, 1년: 2.45%, 2년: 2.05%, 3년: 2.15%";
            case "KOOKMIN":
                return "6개월: 2.05%, 1년: 2.35%, 2년: 1.95%, 3년: 2.05%";
            default:
                return "상품별 금리 문의";
        }
    }
}