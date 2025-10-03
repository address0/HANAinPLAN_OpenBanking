package com.hanainplan.domain.fund.controller;

import com.hanainplan.domain.fund.dto.FundPurchaseRequestDto;
import com.hanainplan.domain.fund.dto.FundPurchaseResponseDto;
import com.hanainplan.domain.fund.service.FundSubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 하나인플랜 펀드 매수 컨트롤러
 */
@RestController
@RequestMapping("/api/banking/fund-subscription")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fund Subscription", description = "펀드 매수/가입 API")
@CrossOrigin(origins = "*")
public class FundSubscriptionController {

    private final FundSubscriptionService fundSubscriptionService;

    /**
     * 펀드 매수
     */
    @PostMapping("/purchase")
    @Operation(summary = "펀드 매수", description = "펀드 클래스를 매수합니다")
    public ResponseEntity<FundPurchaseResponseDto> purchaseFund(
            @RequestBody FundPurchaseRequestDto request
    ) {
        log.info("POST /api/banking/fund-subscription/purchase - 펀드 매수 요청");
        log.info("요청 정보 - userId: {}, childFundCd: {}, amount: {}",
                request.getUserId(), request.getChildFundCd(), request.getPurchaseAmount());

        FundPurchaseResponseDto response = fundSubscriptionService.purchaseFund(request);

        if (response.isSuccess()) {
            log.info("펀드 매수 성공 - subscriptionId: {}", response.getSubscriptionId());
            return ResponseEntity.ok(response);
        } else {
            log.warn("펀드 매수 실패 - {}", response.getErrorMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 펀드 매도 (환매)
     */
    @PostMapping("/redeem")
    @Operation(summary = "펀드 매도", description = "펀드를 매도(환매)합니다")
    public ResponseEntity<com.hanainplan.domain.fund.dto.FundRedemptionResponseDto> redeemFund(
            @RequestBody com.hanainplan.domain.fund.dto.FundRedemptionRequestDto request
    ) {
        log.info("POST /api/banking/fund-subscription/redeem - 펀드 매도 요청");
        log.info("요청 정보 - userId: {}, subscriptionId: {}, sellUnits: {}, sellAll: {}",
                request.getUserId(), request.getSubscriptionId(), 
                request.getSellUnits(), request.getSellAll());

        com.hanainplan.domain.fund.dto.FundRedemptionResponseDto response = 
                fundSubscriptionService.redeemFund(request);

        if (response.isSuccess()) {
            log.info("펀드 매도 성공 - 매도 좌수: {}, 실수령액: {}원, 실현 손익: {}원",
                    response.getSellUnits(), response.getNetAmount(), response.getProfit());
            return ResponseEntity.ok(response);
        } else {
            log.warn("펀드 매도 실패 - {}", response.getErrorMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 사용자의 활성 펀드 가입 목록 조회
     */
    @GetMapping("/user/{userId}/active")
    @Operation(summary = "활성 펀드 가입 목록 조회", description = "사용자의 활성 상태 펀드 가입 목록을 조회합니다")
    public ResponseEntity<?> getActiveSubscriptions(@PathVariable Long userId) {
        log.info("GET /api/banking/fund-subscription/user/{}/active - 활성 펀드 조회", userId);
        
        try {
            var subscriptions = fundSubscriptionService.getActiveSubscriptions(userId);
            return ResponseEntity.ok(subscriptions);
        } catch (Exception e) {
            log.error("활성 펀드 조회 실패 - userId: {}", userId, e);
            return ResponseEntity.internalServerError().body(
                java.util.Map.of("error", "활성 펀드 조회에 실패했습니다", "message", e.getMessage())
            );
        }
    }

    /**
     * 사용자의 펀드 거래 내역 조회
     */
    @GetMapping("/user/{userId}/transactions")
    @Operation(summary = "펀드 거래 내역 조회", description = "사용자의 모든 펀드 거래 내역을 조회합니다")
    public ResponseEntity<?> getUserTransactions(@PathVariable Long userId) {
        log.info("GET /api/banking/fund-subscription/user/{}/transactions - 거래 내역 조회", userId);
        
        try {
            var transactions = fundSubscriptionService.getUserTransactions(userId);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("거래 내역 조회 실패 - userId: {}", userId, e);
            return ResponseEntity.internalServerError().body(
                java.util.Map.of("error", "거래 내역 조회에 실패했습니다", "message", e.getMessage())
            );
        }
    }

    /**
     * 사용자의 펀드 거래 통계 조회
     */
    @GetMapping("/user/{userId}/stats")
    @Operation(summary = "펀드 거래 통계 조회", description = "사용자의 펀드 거래 통계를 조회합니다")
    public ResponseEntity<?> getTransactionStats(@PathVariable Long userId) {
        log.info("GET /api/banking/fund-subscription/user/{}/stats - 거래 통계 조회", userId);
        
        try {
            var stats = fundSubscriptionService.getTransactionStats(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("거래 통계 조회 실패 - userId: {}", userId, e);
            return ResponseEntity.internalServerError().body(
                java.util.Map.of("error", "거래 통계 조회에 실패했습니다", "message", e.getMessage())
            );
        }
    }
}

