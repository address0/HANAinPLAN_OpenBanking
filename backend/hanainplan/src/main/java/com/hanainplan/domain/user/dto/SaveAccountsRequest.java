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
    private List<Object> bankAccountInfo;
}