package com.hanainplan.domain.banking.controller;

import com.hanainplan.domain.banking.dto.DepositSubscriptionDto;
import com.hanainplan.domain.banking.service.DepositSubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/deposit-subscriptions")
@RequiredArgsConstructor
public class DepositSubscriptionController {

    private final DepositSubscriptionService depositSubscriptionService;

    /**
     * 사용자의 모든 예금상품 가입내역 조회
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DepositSubscriptionDto>> getUserSubscriptions(@PathVariable Long userId) {
        log.info("예금상품 가입내역 조회 요청: userId={}", userId);
        
        try {
            List<DepositSubscriptionDto> subscriptions = depositSubscriptionService.getUserSubscriptions(userId);
            return ResponseEntity.ok(subscriptions);
        } catch (Exception e) {
            log.error("예금상품 가입내역 조회 실패: userId={}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 사용자의 활성 예금상품 가입내역 조회
     */
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<DepositSubscriptionDto>> getActiveSubscriptions(@PathVariable Long userId) {
        log.info("활성 예금상품 가입내역 조회 요청: userId={}", userId);
        
        try {
            List<DepositSubscriptionDto> subscriptions = depositSubscriptionService.getActiveSubscriptions(userId);
            return ResponseEntity.ok(subscriptions);
        } catch (Exception e) {
            log.error("활성 예금상품 가입내역 조회 실패: userId={}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 계좌번호로 가입내역 조회
     */
    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<DepositSubscriptionDto> getSubscriptionByAccountNumber(@PathVariable String accountNumber) {
        log.info("계좌번호로 예금상품 가입내역 조회 요청: accountNumber={}", accountNumber);
        
        try {
            DepositSubscriptionDto subscription = depositSubscriptionService.getSubscriptionByAccountNumber(accountNumber);
            return ResponseEntity.ok(subscription);
        } catch (RuntimeException e) {
            log.error("계좌번호로 예금상품 가입내역 조회 실패: accountNumber={}", accountNumber, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("계좌번호로 예금상품 가입내역 조회 오류: accountNumber={}", accountNumber, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 만기 예정 가입내역 조회
     */
    @GetMapping("/user/{userId}/upcoming-maturity")
    public ResponseEntity<List<DepositSubscriptionDto>> getUpcomingMaturitySubscriptions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int daysAhead) {
        log.info("만기 예정 예금상품 조회 요청: userId={}, daysAhead={}", userId, daysAhead);
        
        try {
            List<DepositSubscriptionDto> subscriptions = depositSubscriptionService
                    .getUpcomingMaturitySubscriptions(userId, daysAhead);
            return ResponseEntity.ok(subscriptions);
        } catch (Exception e) {
            log.error("만기 예정 예금상품 조회 실패: userId={}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 은행별 가입내역 조회
     */
    @GetMapping("/user/{userId}/bank/{bankCode}")
    public ResponseEntity<List<DepositSubscriptionDto>> getSubscriptionsByBank(
            @PathVariable Long userId,
            @PathVariable String bankCode) {
        log.info("은행별 예금상품 가입내역 조회 요청: userId={}, bankCode={}", userId, bankCode);
        
        try {
            List<DepositSubscriptionDto> subscriptions = depositSubscriptionService
                    .getSubscriptionsByBank(userId, bankCode);
            return ResponseEntity.ok(subscriptions);
        } catch (Exception e) {
            log.error("은행별 예금상품 가입내역 조회 실패: userId={}, bankCode={}", userId, bankCode, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 예금상품 가입내역 생성
     */
    @PostMapping
    public ResponseEntity<DepositSubscriptionDto> createSubscription(@RequestBody DepositSubscriptionDto dto) {
        log.info("예금상품 가입내역 생성 요청: userId={}, depositCode={}", dto.getUserId(), dto.getDepositCode());
        
        try {
            DepositSubscriptionDto createdSubscription = depositSubscriptionService.createSubscription(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSubscription);
        } catch (RuntimeException e) {
            log.error("예금상품 가입내역 생성 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("예금상품 가입내역 생성 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 예금상품 가입내역 수정
     */
    @PutMapping("/{subscriptionId}")
    public ResponseEntity<DepositSubscriptionDto> updateSubscription(
            @PathVariable Long subscriptionId,
            @RequestBody DepositSubscriptionDto dto) {
        log.info("예금상품 가입내역 수정 요청: subscriptionId={}", subscriptionId);
        
        try {
            DepositSubscriptionDto updatedSubscription = depositSubscriptionService
                    .updateSubscription(subscriptionId, dto);
            return ResponseEntity.ok(updatedSubscription);
        } catch (RuntimeException e) {
            log.error("예금상품 가입내역 수정 실패: subscriptionId={}", subscriptionId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("예금상품 가입내역 수정 오류: subscriptionId={}", subscriptionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 예금상품 해지
     */
    @PostMapping("/{subscriptionId}/cancel")
    public ResponseEntity<Void> cancelSubscription(@PathVariable Long subscriptionId) {
        log.info("예금상품 해지 요청: subscriptionId={}", subscriptionId);
        
        try {
            depositSubscriptionService.cancelSubscription(subscriptionId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("예금상품 해지 실패: subscriptionId={}", subscriptionId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("예금상품 해지 오류: subscriptionId={}", subscriptionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 만기 처리
     */
    @PostMapping("/{subscriptionId}/maturity")
    public ResponseEntity<Void> processMaturity(@PathVariable Long subscriptionId) {
        log.info("예금상품 만기 처리 요청: subscriptionId={}", subscriptionId);
        
        try {
            depositSubscriptionService.processMaturity(subscriptionId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.error("예금상품 만기 처리 실패: subscriptionId={}", subscriptionId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("예금상품 만기 처리 오류: subscriptionId={}", subscriptionId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

