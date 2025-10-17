package com.hanainplan.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IrpAccountCreateResponse {
    private Long customerId;
    private String irpAccountNumber;
    private String bankCode;
    private String bankName;
    private String message;
    private Boolean success;
}
