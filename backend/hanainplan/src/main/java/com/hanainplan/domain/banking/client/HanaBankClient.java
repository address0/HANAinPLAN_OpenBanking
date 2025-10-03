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
     * 모든 금리 정보 조회
     */
    @GetMapping("/api/hana/interest-rates/all")
    List<Map<String, Object>> getAllInterestRates();

    // ==================== 펀드 관련 API ====================

    /**
     * 모든 활성 펀드 상품 조회
     */
    @GetMapping("/api/hana/fund-products")
    List<Map<String, Object>> getAllFundProducts();

    /**
     * 펀드 코드로 상품 상세 조회
     */
    @GetMapping("/api/hana/fund-products/{fundCode}")
    Map<String, Object> getFundProduct(@PathVariable("fundCode") String fundCode);

    /**
     * 펀드 유형별 조회
     */
    @GetMapping("/api/hana/fund-products/type/{fundType}")
    List<Map<String, Object>> getFundProductsByType(@PathVariable("fundType") String fundType);

    /**
     * 위험등급별 조회
     */
    @GetMapping("/api/hana/fund-products/risk/{riskLevel}")
    List<Map<String, Object>> getFundProductsByRiskLevel(@PathVariable("riskLevel") String riskLevel);

    /**
     * IRP 편입 가능 펀드 조회
     */
    @GetMapping("/api/hana/fund-products/irp-eligible")
    List<Map<String, Object>> getIrpEligibleFunds();

    /**
     * 수익률 상위 펀드 조회
     */
    @GetMapping("/api/hana/fund-products/top-performing")
    List<Map<String, Object>> getTopPerformingFunds();

    /**
     * 복합 필터링 조회
     */
    @GetMapping("/api/hana/fund-products/filter")
    List<Map<String, Object>> filterFunds(
        @RequestParam(required = false) String fundType,
        @RequestParam(required = false) String riskLevel,
        @RequestParam(required = false) String investmentRegion,
        @RequestParam(required = false) Boolean isIrpEligible
    );

    // ==================== 실제 펀드 구조 API (FundClass) ====================

    /**
     * 판매중인 펀드 클래스 목록 조회
     */
    @GetMapping("/api/hana/fund-classes")
    List<Map<String, Object>> getAllOnSaleFundClasses();

    /**
     * 클래스 코드로 상세 조회
     */
    @GetMapping("/api/hana/fund-classes/{childFundCd}")
    Map<String, Object> getFundClass(@PathVariable("childFundCd") String childFundCd);

    /**
     * 모펀드 코드로 클래스 목록 조회
     */
    @GetMapping("/api/hana/fund-classes/master/{fundCd}")
    List<Map<String, Object>> getFundClassesByMaster(@PathVariable("fundCd") String fundCd);

    /**
     * 자산 유형별 조회
     */
    @GetMapping("/api/hana/fund-classes/asset-type/{assetType}")
    List<Map<String, Object>> getFundClassesByAssetType(@PathVariable("assetType") String assetType);

    /**
     * 클래스 코드별 조회 (A/C/P 등)
     */
    @GetMapping("/api/hana/fund-classes/class-code/{classCode}")
    List<Map<String, Object>> getFundClassesByClassCode(@PathVariable("classCode") String classCode);

    /**
     * 최소 투자금액 이하 펀드 조회
     */
    @GetMapping("/api/hana/fund-classes/max-amount/{maxAmount}")
    List<Map<String, Object>> getFundClassesByMaxAmount(@PathVariable("maxAmount") int maxAmount);

    // ==================== 펀드 매수/가입 API ====================

    /**
     * 펀드 매수
     */
    @PostMapping("/api/hana/fund-subscription/purchase")
    Map<String, Object> purchaseFund(@RequestBody Map<String, Object> request);

    /**
     * 고객의 펀드 가입 목록 조회
     */
    @GetMapping("/api/hana/fund-subscription/customer/{customerCi}")
    List<Map<String, Object>> getCustomerSubscriptions(@PathVariable("customerCi") String customerCi);

    /**
     * 활성 펀드 가입 목록 조회
     */
    @GetMapping("/api/hana/fund-subscription/customer/{customerCi}/active")
    List<Map<String, Object>> getActiveSubscriptions(@PathVariable("customerCi") String customerCi);

    /**
     * 펀드 매도 (환매)
     */
    @PostMapping("/api/hana/fund-subscription/redeem")
    Map<String, Object> redeemFund(@RequestBody Map<String, Object> request);

    // ==================== 펀드 거래 내역 API ====================

    /**
     * 고객의 모든 거래 내역 조회
     */
    @GetMapping("/api/hana/fund-transactions/customer/{customerCi}")
    List<Map<String, Object>> getCustomerTransactions(@PathVariable("customerCi") String customerCi);

    /**
     * 특정 가입의 거래 내역 조회
     */
    @GetMapping("/api/hana/fund-transactions/subscription/{subscriptionId}")
    List<Map<String, Object>> getSubscriptionTransactions(@PathVariable("subscriptionId") Long subscriptionId);

    /**
     * 거래 통계 조회
     */
    @GetMapping("/api/hana/fund-transactions/customer/{customerCi}/stats")
    Map<String, Object> getTransactionStats(@PathVariable("customerCi") String customerCi);

    /**
     * 계좌 입금 거래내역 생성
     */
    @PostMapping("/api/hana/accounts/deposit")
    Map<String, Object> createDepositTransaction(@RequestBody Map<String, Object> request);

    /**
     * 계좌 출금 처리 (거래내역 생성 포함)
     */
    @PostMapping("/api/hana/accounts/withdrawal")
    Map<String, Object> processWithdrawal(@RequestBody Map<String, Object> request);

    /**
     * IRP 계좌 삭제 (보상 트랜잭션용)
     */
    @DeleteMapping("/api/v1/irp/delete")
    Map<String, Object> deleteIrpAccount(@RequestBody Map<String, Object> request);

    // ==================== 계좌 동기화 API ====================

    /**
     * CI로 고객의 모든 계좌 조회
     */
    @GetMapping("/api/hana/customer-accounts/ci/{ci}")
    Map<String, Object> getCustomerAccountsByCi(@PathVariable("ci") String ci);

    /**
     * 계좌번호로 거래내역 조회
     */
    @GetMapping("/api/v1/transactions/account/{accountNumber}")
    List<BankTransactionDto> getTransactionsByAccountNumber(@PathVariable("accountNumber") String accountNumber);

    /**
     * 은행 거래내역 DTO
     */
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


