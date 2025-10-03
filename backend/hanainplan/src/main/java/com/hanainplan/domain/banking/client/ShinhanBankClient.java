package com.hanainplan.domain.banking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 신한은행 API Feign Client
 */
@FeignClient(name = "shinhan-bank", url = "${bank.shinhan.base-url:http://localhost:8083}")
public interface ShinhanBankClient {

    /**
     * 정기예금 상품 가입
     */
    @PostMapping("/api/shinhan/product/financial/subscribe")
    Map<String, Object> subscribeDeposit(@RequestBody Map<String, Object> request);

    /**
     * 가입 상태 변경
     */
    @PutMapping("/api/shinhan/product/subscriptions/{subscriptionId}/status")
    Map<String, Object> updateSubscriptionStatus(
            @PathVariable("subscriptionId") Long subscriptionId,
            @RequestParam("status") String status);

    /**
     * 가입 해지
     */
    @DeleteMapping("/api/shinhan/product/subscriptions/{subscriptionId}")
    Map<String, Object> cancelSubscription(@PathVariable("subscriptionId") Long subscriptionId);

    /**
     * 모든 금리 정보 조회
     */
    @GetMapping("/api/shinhan/interest-rates/all")
    java.util.List<Map<String, Object>> getAllInterestRates();

    /**
     * 계좌 출금 처리 (거래내역 생성 포함)
     */
    @PostMapping("/api/shinhan/accounts/withdrawal")
    Map<String, Object> processWithdrawal(@RequestBody Map<String, Object> request);

    /**
     * 계좌 입금 거래내역 생성
     */
    @PostMapping("/api/shinhan/accounts/deposit")
    Map<String, Object> createDepositTransaction(@RequestBody Map<String, Object> request);
}

