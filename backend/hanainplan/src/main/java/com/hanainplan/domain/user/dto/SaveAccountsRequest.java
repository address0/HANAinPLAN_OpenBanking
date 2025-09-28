package com.hanainplan.domain.user.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveAccountsRequest {
    private Long userId;
    private List<Object> bankAccountInfo; // 프론트엔드에서 받은 계좌 정보
}

