package com.hanainplan.domain.banking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "kookmin-bank", url = "${bank.kookmin.base-url:http://localhost:8082}")
public interface KookminBankClient {

    @PostMapping("/api/kookmin/product/financial/subscribe")
    Map<String, Object> subscribeDeposit(@RequestBody Map<String, Object> request);

    @PutMapping("/api/kookmin/product/subscriptions/{subscriptionId}/status")
    Map<String, Object> updateSubscriptionStatus(
            @PathVariable("subscriptionId") Long subscriptionId,
            @RequestParam("status") String status);

    @DeleteMapping("/api/kookmin/product/subscriptions/{subscriptionId}")
    Map<String, Object> cancelSubscription(@PathVariable("subscriptionId") Long subscriptionId);

    @GetMapping("/api/kookmin/interest-rates/all")
    java.util.List<Map<String, Object>> getAllInterestRates();

    @PostMapping("/api/kookmin/accounts/withdrawal")
    Map<String, Object> processWithdrawal(@RequestBody Map<String, Object> request);

    @PostMapping("/api/kookmin/accounts/deposit")
    Map<String, Object> createDepositTransaction(@RequestBody Map<String, Object> request);
}