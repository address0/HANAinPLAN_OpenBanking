package com.hanainplan.hana.fund.controller;

import com.hanainplan.hana.fund.dto.FundPurchaseRequestDto;
import com.hanainplan.hana.fund.dto.FundPurchaseResponseDto;
import com.hanainplan.hana.fund.entity.FundSubscription;
import com.hanainplan.hana.fund.service.FundSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hana/fund-subscription")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Hana Fund Subscription", description = "하나은행 펀드 매수/가입 API")
public class FundSubscriptionController {

    private final FundSubscriptionService fundSubscriptionService;

    @PostMapping("/purchase")
    @Operation(summary = "펀드 매수", description = "펀드 클래스를 매수합니다")
    public ResponseEntity<FundPurchaseResponseDto> purchaseFund(
            @RequestBody FundPurchaseRequestDto request
    ) {
        log.info("POST /api/hana/fund-subscription/purchase - 펀드 매수 요청");
        log.info("요청 정보 - customerCi: {}, childFundCd: {}, amount: {}",
                request.getCustomerCi(), request.getChildFundCd(), request.getPurchaseAmount());

        FundPurchaseResponseDto response = fundSubscriptionService.purchaseFund(request);

        if (response.isSuccess()) {
            log.info("펀드 매수 성공 - subscriptionId: {}", response.getSubscriptionId());
            return ResponseEntity.ok(response);
        } else {
            log.warn("펀드 매수 실패 - {}", response.getErrorMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/customer/{customerCi}")
    @Operation(summary = "고객 펀드 가입 목록", description = "고객의 모든 펀드 가입 내역을 조회합니다")
    public ResponseEntity<List<FundSubscription>> getCustomerSubscriptions(
            @Parameter(description = "고객 CI", required = true)
            @PathVariable String customerCi
    ) {
        log.info("GET /api/hana/fund-subscription/customer/{} - 고객 펀드 가입 목록 조회", customerCi);

        List<FundSubscription> subscriptions = fundSubscriptionService.getCustomerSubscriptions(customerCi);

        log.info("고객 펀드 가입 목록 조회 완료 - {}건", subscriptions.size());
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/customer/{customerCi}/active")
    @Operation(summary = "활성 펀드 가입 목록", description = "고객의 보유중인 펀드 가입 내역만 조회합니다")
    public ResponseEntity<List<FundSubscription>> getActiveSubscriptions(
            @Parameter(description = "고객 CI", required = true)
            @PathVariable String customerCi
    ) {
        log.info("GET /api/hana/fund-subscription/customer/{}/active - 활성 펀드 가입 목록 조회", customerCi);

        List<FundSubscription> subscriptions = fundSubscriptionService.getActiveSubscriptions(customerCi);

        log.info("활성 펀드 가입 목록 조회 완료 - {}건", subscriptions.size());
        return ResponseEntity.ok(subscriptions);
    }

    @PostMapping("/redeem")
    @Operation(summary = "펀드 매도", description = "펀드를 매도(환매)합니다")
    public ResponseEntity<com.hanainplan.hana.fund.dto.FundRedemptionResponseDto> redeemFund(
            @RequestBody com.hanainplan.hana.fund.dto.FundRedemptionRequestDto request
    ) {
        log.info("POST /api/hana/fund-subscription/redeem - 펀드 매도 요청");
        log.info("요청 정보 - customerCi: {}, subscriptionId: {}, sellUnits: {}, sellAll: {}",
                request.getCustomerCi(), request.getSubscriptionId(), 
                request.getSellUnits(), request.getSellAll());

        com.hanainplan.hana.fund.dto.FundRedemptionResponseDto response = 
                fundSubscriptionService.redeemFund(request);

        if (response.isSuccess()) {
            log.info("펀드 매도 성공 - 매도 좌수: {}, 실수령액: {}원", 
                    response.getSellUnits(), response.getNetAmount());
            return ResponseEntity.ok(response);
        } else {
            log.warn("펀드 매도 실패 - {}", response.getErrorMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}