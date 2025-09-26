package com.hanainplan.domain.banking.dto;

import com.hanainplan.domain.banking.entity.BankingAccount;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {
    
    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;
    
    @NotNull(message = "계좌 유형은 필수입니다")
    private Integer accountType;
    
    @NotBlank(message = "계좌명은 필수입니다")
    private String accountName;
    
    private BigDecimal initialBalance;
    
    private String description;
    
    // 적금/정기예금 전용 필드들
    private String purpose; // 계좌 용도
    
    private BigDecimal monthlyDepositAmount; // 월 적립 금액 (적금의 경우)
    
    private Integer depositPeriod; // 적립/예치 기간 (개월)
    
    private String interestPaymentMethod; // 이자 지급 방법 (AUTO, MANUAL)
    
    @Pattern(regexp = "^.{4,}$", message = "계좌 비밀번호는 4자리 이상이어야 합니다")
    private String accountPassword; // 계좌 비밀번호
}
