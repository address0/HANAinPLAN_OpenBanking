package com.hanainplan.hana.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IrpAccountResponse {

    private String accountNumber; // 생성된 IRP 계좌번호
    private String accountStatus; // 계좌상태
    private BigDecimal initialDeposit; // 초기 납입금액
    private BigDecimal monthlyDeposit; // 월 자동납입 금액
    private String investmentStyle; // 투자성향
    private String productCode; // 연결된 상품코드
    private LocalDate openDate; // 개설일
    private LocalDateTime createdAt; // 생성시간
    private String message; // 응답 메시지
    private boolean success; // 성공 여부

    // 편의 생성자
    public static IrpAccountResponse success(String accountNumber, String message) {
        return IrpAccountResponse.builder()
                .accountNumber(accountNumber)
                .success(true)
                .message(message)
                .build();
    }

    public static IrpAccountResponse failure(String message) {
        return IrpAccountResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
















