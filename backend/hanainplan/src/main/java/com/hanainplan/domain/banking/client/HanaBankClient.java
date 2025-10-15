package com.hanainplan.domain.banking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@FeignClient(name = "hana-bank", url = "${bank.hana.base-url:http://localhost:8081}")
public interface HanaBankClient {

    @PostMapping("/api/v1/irp/open")
    Map<String, Object> openIrpAccount(@RequestBody Map<String, Object> request);

    @PostMapping("/api/v1/deposit/subscribe")
    Map<String, Object> subscribeDeposit(@RequestBody Map<String, Object> request);

    @PostMapping("/api/v1/deposit/terminate/{accountNumber}")
    Map<String, Object> terminateDeposit(@PathVariable("accountNumber") String accountNumber);

    @GetMapping("/api/v1/transactions/account/{accountNumber}")
    List<BankTransactionDto> getTransactionsByAccount(@PathVariable("accountNumber") String accountNumber);

    @GetMapping("/api/hana/interest-rates/all")
    List<Map<String, Object>> getAllInterestRates();

    @GetMapping("/api/hana/fund-products")
    List<Map<String, Object>> getAllFundProducts();

    @GetMapping("/api/hana/fund-products/{fundCode}")
    Map<String, Object> getFundProduct(@PathVariable("fundCode") String fundCode);

    @GetMapping("/api/hana/fund-products/type/{fundType}")
    List<Map<String, Object>> getFundProductsByType(@PathVariable("fundType") String fundType);

    @GetMapping("/api/hana/fund-products/risk/{riskLevel}")
    List<Map<String, Object>> getFundProductsByRiskLevel(@PathVariable("riskLevel") String riskLevel);

    @GetMapping("/api/hana/fund-products/irp-eligible")
    List<Map<String, Object>> getIrpEligibleFunds();

    @GetMapping("/api/hana/fund-products/top-performing")
    List<Map<String, Object>> getTopPerformingFunds();

    @GetMapping("/api/hana/fund-products/filter")
    List<Map<String, Object>> filterFunds(
        @RequestParam(required = false) String fundType,
        @RequestParam(required = false) String riskLevel,
        @RequestParam(required = false) String investmentRegion,
        @RequestParam(required = false) Boolean isIrpEligible
    );

    @GetMapping("/api/hana/fund-classes")
    List<Map<String, Object>> getAllOnSaleFundClasses();

    @GetMapping("/api/hana/fund-classes/{childFundCd}")
    Map<String, Object> getFundClass(@PathVariable("childFundCd") String childFundCd);

    @GetMapping("/api/hana/fund-classes/master/{fundCd}")
    List<Map<String, Object>> getFundClassesByMaster(@PathVariable("fundCd") String fundCd);

    @GetMapping("/api/hana/fund-classes/asset-type/{assetType}")
    List<Map<String, Object>> getFundClassesByAssetType(@PathVariable("assetType") String assetType);

    @GetMapping("/api/hana/fund-classes/class-code/{classCode}")
    List<Map<String, Object>> getFundClassesByClassCode(@PathVariable("classCode") String classCode);

    @GetMapping("/api/hana/fund-classes/max-amount/{maxAmount}")
    List<Map<String, Object>> getFundClassesByMaxAmount(@PathVariable("maxAmount") int maxAmount);

    @PostMapping("/api/hana/fund-subscription/purchase")
    Map<String, Object> purchaseFund(@RequestBody Map<String, Object> request);

    @GetMapping("/api/hana/fund-subscription/customer/{customerCi}")
    List<Map<String, Object>> getCustomerSubscriptions(@PathVariable("customerCi") String customerCi);

    @GetMapping("/api/hana/fund-subscription/customer/{customerCi}/active")
    List<Map<String, Object>> getActiveSubscriptions(@PathVariable("customerCi") String customerCi);

    @PostMapping("/api/hana/fund-subscription/redeem")
    Map<String, Object> redeemFund(@RequestBody Map<String, Object> request);

    @GetMapping("/api/hana/fund-transactions/customer/{customerCi}")
    List<Map<String, Object>> getCustomerTransactions(@PathVariable("customerCi") String customerCi);

    @GetMapping("/api/hana/fund-transactions/subscription/{subscriptionId}")
    List<Map<String, Object>> getSubscriptionTransactions(@PathVariable("subscriptionId") Long subscriptionId);

    @GetMapping("/api/hana/fund-transactions/customer/{customerCi}/stats")
    Map<String, Object> getTransactionStats(@PathVariable("customerCi") String customerCi);

    @PostMapping("/api/hana/accounts/deposit")
    Map<String, Object> createDepositTransaction(@RequestBody Map<String, Object> request);

    @PostMapping("/api/hana/accounts/withdrawal")
    Map<String, Object> processWithdrawal(@RequestBody Map<String, Object> request);

    @DeleteMapping("/api/v1/irp/delete")
    Map<String, Object> deleteIrpAccount(@RequestBody Map<String, Object> request);

    @GetMapping("/api/hana/customer-accounts/ci/{ci}")
    Map<String, Object> getCustomerAccountsByCi(@PathVariable("ci") String ci);

    @GetMapping("/api/v1/transactions/account/{accountNumber}")
    List<BankTransactionDto> getTransactionsByAccountNumber(@PathVariable("accountNumber") String accountNumber);

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
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