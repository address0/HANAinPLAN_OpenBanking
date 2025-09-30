package com.hanainplan.domain.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * IRP 계좌 상태 확인 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IrpAccountStatusResponseDto {

    private String customerCi;
    private boolean hasIrpAccount;
    private String message;

    // 성공 응답용 생성자
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
