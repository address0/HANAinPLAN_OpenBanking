package com.hanainplan.domain.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IrpAccountStatusResponseDto {

    private String customerCi;
    private boolean hasIrpAccount;
    private String message;

    public static IrpAccountStatusResponseDto success(String customerCi, boolean hasIrpAccount) {
        String message = hasIrpAccount ?
            "IRP 계좌를 보유하고 있습니다" : "IRP 계좌를 보유하고 있지 않습니다";

        return IrpAccountStatusResponseDto.builder()
                .customerCi(customerCi)
                .hasIrpAccount(hasIrpAccount)
                .message(message)
                .build();
    }
}