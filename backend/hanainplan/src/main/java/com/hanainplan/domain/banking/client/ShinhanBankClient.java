package com.hanainplan.domain.banking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "shinhan-bank", url = "${bank.shinhan.base-url:http://localhost:8083}")
public interface ShinhanBankClient {

    @PostMapping("/api/shinhan/product/financial/subscribe")
    Map<String, Object> subscribeDeposit(@RequestBody Map<String, Object> request);

    @PutMapping("/api/shinhan/product/subscriptions/{subscriptionId}/status")
    Map<String, Object> updateSubscriptionStatus(
            @PathVariable("subscriptionId") Long subscriptionId,
            @RequestParam("status") String status);

    @DeleteMapping("/api/shinhan/product/subscriptions/{subscriptionId}")
    Map<String, Object> cancelSubscription(@PathVariable("subscriptionId") Long subscriptionId);

    @GetMapping("/api/shinhan/interest-rates/all")
    java.util.List<Map<String, Object>> getAllInterestRates();

    @PostMapping("/api/shinhan/accounts/withdrawal")
    Map<String, Object> processWithdrawal(@RequestBody Map<String, Object> request);

    @PostMapping("/api/shinhan/accounts/deposit")
    Map<String, Object> createDepositTransaction(@RequestBody Map<String, Object> request);
}