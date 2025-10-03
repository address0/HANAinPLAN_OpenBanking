package com.hanainplan.domain.banking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 국민은행 API Feign Client
 */
@FeignClient(name = "kookmin-bank", url = "${bank.kookmin.base-url:http://localhost:8082}")
public interface KookminBankClient {

    /**
     * 정기예금 상품 가입
     */
    @PostMapping("/api/kookmin/product/financial/subscribe")
    Map<String, Object> subscribeDeposit(@RequestBody Map<String, Object> request);

    /**
     * 가입 상태 변경
     */
    @PutMapping("/api/kookmin/product/subscriptions/{subscriptionId}/status")
    Map<String, Object> updateSubscriptionStatus(
            @PathVariable("subscriptionId") Long subscriptionId,
            @RequestParam("status") String status);

    /**
     * 가입 해지
     */
    @DeleteMapping("/api/kookmin/product/subscriptions/{subscriptionId}")
    Map<String, Object> cancelSubscription(@PathVariable("subscriptionId") Long subscriptionId);

    /**
     * 모든 금리 정보 조회
     */
    @GetMapping("/api/kookmin/interest-rates/all")
    java.util.List<Map<String, Object>> getAllInterestRates();

    /**
     * 계좌 출금 처리 (거래내역 생성 포함)
     */
    @PostMapping("/api/kookmin/accounts/withdrawal")
    Map<String, Object> processWithdrawal(@RequestBody Map<String, Object> request);

    /**
     * 계좌 입금 거래내역 생성
     */
    @PostMapping("/api/kookmin/accounts/deposit")
    Map<String, Object> createDepositTransaction(@RequestBody Map<String, Object> request);
}

