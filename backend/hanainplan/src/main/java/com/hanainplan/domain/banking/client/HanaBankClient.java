package com.hanainplan.domain.banking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 하나은행 API Feign Client
 */
@FeignClient(name = "hana-bank", url = "${bank.hana.base-url:http://localhost:8081}")
public interface HanaBankClient {

    /**
     * IRP 계좌 개설
     */
    @PostMapping("/api/v1/irp/open")
    Map<String, Object> openIrpAccount(@RequestBody Map<String, Object> request);

    /**
     * 예금 상품 가입
     */
    @PostMapping("/api/v1/deposit/subscribe")
    Map<String, Object> subscribeDeposit(@RequestBody Map<String, Object> request);

    /**
     * 예금 중도해지
     */
    @PostMapping("/api/v1/deposit/terminate/{accountNumber}")
    Map<String, Object> terminateDeposit(@PathVariable("accountNumber") String accountNumber);

    /**
     * 계좌별 거래내역 조회
     */
    @GetMapping("/api/v1/transactions/account/{accountNumber}")
    List<BankTransactionDto> getTransactionsByAccount(@PathVariable("accountNumber") String accountNumber);

    /**
     * 은행 거래내역 DTO
     */
    class BankTransactionDto {
        private String transactionNumber;
        private String transactionType;
        private String transactionDirection;
        private BigDecimal amount;
        private BigDecimal balanceAfter;
        private java.time.LocalDateTime transactionDatetime;
        private java.time.LocalDateTime processedDate;
        private String transactionStatus;
        private String transactionCategory;
        private String description;
        private String memo;
        private String referenceNumber;

        // Getters and Setters
        public String getTransactionNumber() { return transactionNumber; }
        public void setTransactionNumber(String transactionNumber) { this.transactionNumber = transactionNumber; }
        
        public String getTransactionType() { return transactionType; }
        public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
        
        public String getTransactionDirection() { return transactionDirection; }
        public void setTransactionDirection(String transactionDirection) { this.transactionDirection = transactionDirection; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public BigDecimal getBalanceAfter() { return balanceAfter; }
        public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }
        
        public java.time.LocalDateTime getTransactionDatetime() { return transactionDatetime; }
        public void setTransactionDatetime(java.time.LocalDateTime transactionDatetime) { this.transactionDatetime = transactionDatetime; }
        
        public java.time.LocalDateTime getProcessedDate() { return processedDate; }
        public void setProcessedDate(java.time.LocalDateTime processedDate) { this.processedDate = processedDate; }
        
        public String getTransactionStatus() { return transactionStatus; }
        public void setTransactionStatus(String transactionStatus) { this.transactionStatus = transactionStatus; }
        
        public String getTransactionCategory() { return transactionCategory; }
        public void setTransactionCategory(String transactionCategory) { this.transactionCategory = transactionCategory; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getMemo() { return memo; }
        public void setMemo(String memo) { this.memo = memo; }
        
        public String getReferenceNumber() { return referenceNumber; }
        public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    }
}


