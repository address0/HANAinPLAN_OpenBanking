package com.hanainplan.hana.fund.dto;

import com.hanainplan.hana.fund.entity.FundTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 펀드 거래 내역 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundTransactionDto {

    private Long transactionId;
    private String customerCi;
    private Long subscriptionId;
    private String childFundCd;
    private String fundName;
    private String classCode;
    
    private String transactionType;      // BUY, SELL, DIVIDEND
    private String transactionTypeName;  // 매수, 매도, 분배금
    private LocalDate transactionDate;
    private LocalDate settlementDate;
    
    private BigDecimal nav;
    private BigDecimal units;
    private BigDecimal amount;
    private BigDecimal fee;
    private String feeType;
    
    private BigDecimal profit;           // 실현 손익 (매도 시)
    private BigDecimal profitRate;       // 실현 수익률 (매도 시)
    
    private String irpAccountNumber;
    private BigDecimal irpBalanceBefore;
    private BigDecimal irpBalanceAfter;
    
    private String status;
    private String note;

    /**
     * Entity -> DTO 변환
     */
    public static FundTransactionDto from(FundTransaction transaction) {
        return FundTransactionDto.builder()
                .transactionId(transaction.getTransactionId())
                .customerCi(transaction.getCustomerCi())
                .subscriptionId(transaction.getSubscriptionId())
                .childFundCd(transaction.getChildFundCd())
                .fundName(transaction.getFundName())
                .classCode(transaction.getClassCode())
                .transactionType(transaction.getTransactionType())
                .transactionTypeName(getTransactionTypeName(transaction.getTransactionType()))
                .transactionDate(transaction.getTransactionDate())
                .settlementDate(transaction.getSettlementDate())
                .nav(transaction.getNav())
                .units(transaction.getUnits())
                .amount(transaction.getAmount())
                .fee(transaction.getFee())
                .feeType(transaction.getFeeType())
                .profit(transaction.getProfit())
                .profitRate(transaction.getProfitRate())
                .irpAccountNumber(transaction.getIrpAccountNumber())
                .irpBalanceBefore(transaction.getIrpBalanceBefore())
                .irpBalanceAfter(transaction.getIrpBalanceAfter())
                .status(transaction.getStatus())
                .note(transaction.getNote())
                .build();
    }

    /**
     * 거래 유형 한글명 반환
     */
    private static String getTransactionTypeName(String type) {
        return switch (type) {
            case "BUY" -> "매수";
            case "SELL" -> "매도";
            case "DIVIDEND" -> "분배금";
            default -> type;
        };
    }
}

