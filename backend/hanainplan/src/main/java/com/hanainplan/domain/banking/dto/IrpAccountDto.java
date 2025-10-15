package com.hanainplan.domain.banking.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class IrpAccountDto {

    private Long irpAccountId;
    private Long customerId;
    private String customerCi;
    private String bankCode;
    private String accountNumber;
    private String accountStatus;
    private BigDecimal initialDeposit;
    private BigDecimal monthlyDeposit;
    private BigDecimal currentBalance;
    private BigDecimal totalContribution;
    private BigDecimal totalReturn;
    private BigDecimal returnRate;
    private String investmentStyle;
    private Boolean isAutoDeposit;
    private Integer depositDay;
    private String linkedMainAccount;
    private String productCode;
    private String productName;
    private BigDecimal managementFeeRate;
    private BigDecimal trustFeeRate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate openDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate maturityDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate lastContributionDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastSyncDate;

    private String syncStatus;
    private String syncErrorMessage;
    private String externalAccountId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime externalLastUpdated;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;

    public boolean isActive() {
        return "ACTIVE".equals(accountStatus);
    }

    public boolean hasAutoDeposit() {
        return Boolean.TRUE.equals(isAutoDeposit);
    }

    public boolean isMatured() {
        return maturityDate != null && !maturityDate.isAfter(LocalDate.now());
    }

    public String getDisplayAccountNumber() {
        if (accountNumber == null || accountNumber.length() < 4) {
            return accountNumber;
        }
        return accountNumber.substring(0, accountNumber.length() - 3) + "***";
    }

    public String getBankName() {
        switch (bankCode) {
            case "HANA": return "하나은행";
            case "KOOKMIN": return "국민은행";
            case "SHINHAN": return "신한은행";
            default: return bankCode;
        }
    }

    public String getInvestmentStyleDisplay() {
        if (investmentStyle == null) return "-";

        switch (investmentStyle) {
            case "CONSERVATIVE": return "안정형";
            case "MODERATE_CONSERVATIVE": return "안정추구형";
            case "MODERATE": return "위험중립형";
            case "AGGRESSIVE": return "공격투자형";
            default: return investmentStyle;
        }
    }

    public String getSyncStatusDisplay() {
        if (syncStatus == null) return "-";

        switch (syncStatus) {
            case "PENDING": return "대기중";
            case "RUNNING": return "동기화중";
            case "SUCCESS": return "성공";
            case "FAILED": return "실패";
            case "PARTIAL": return "부분성공";
            default: return syncStatus;
        }
    }
}