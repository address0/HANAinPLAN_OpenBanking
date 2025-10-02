package com.hanainplan.domain.banking.service;

import com.hanainplan.domain.banking.dto.DepositSubscriptionDto;
import com.hanainplan.domain.banking.entity.DepositSubscription;
import com.hanainplan.domain.banking.repository.DepositSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 예금 가입 정보 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepositSubscriptionService {

    private final DepositSubscriptionRepository depositSubscriptionRepository;

    /**
     * 사용자의 모든 예금상품 가입내역 조회
     */
    @Transactional(readOnly = true)
    public List<DepositSubscriptionDto> getUserSubscriptions(Long userId) {
        log.info("사용자 예금상품 가입내역 조회: userId={}", userId);
        
        List<DepositSubscription> subscriptions = depositSubscriptionRepository
                .findByUserIdOrderBySubscriptionDateDesc(userId);
        
        return subscriptions.stream()
                .map(DepositSubscriptionDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 활성 예금상품 가입내역 조회
     */
    @Transactional(readOnly = true)
    public List<DepositSubscriptionDto> getActiveSubscriptions(Long userId) {
        log.info("사용자 활성 예금상품 가입내역 조회: userId={}", userId);
        
        List<DepositSubscription> subscriptions = depositSubscriptionRepository
                .findByUserIdAndStatus(userId, "ACTIVE");
        
        return subscriptions.stream()
                .map(DepositSubscriptionDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 계좌번호로 가입내역 조회
     */
    @Transactional(readOnly = true)
    public DepositSubscriptionDto getSubscriptionByAccountNumber(String accountNumber) {
        log.info("계좌번호로 예금상품 가입내역 조회: accountNumber={}", accountNumber);
        
        DepositSubscription subscription = depositSubscriptionRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("가입내역을 찾을 수 없습니다: " + accountNumber));
        
        return DepositSubscriptionDto.fromEntity(subscription);
    }

    /**
     * 만기 예정 가입내역 조회
     */
    @Transactional(readOnly = true)
    public List<DepositSubscriptionDto> getUpcomingMaturitySubscriptions(Long userId, int daysAhead) {
        log.info("만기 예정 예금상품 조회: userId={}, daysAhead={}", userId, daysAhead);
        
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(daysAhead);
        
        List<DepositSubscription> subscriptions = depositSubscriptionRepository
                .findByMaturityDateBetween(today, endDate);
        
        // userId로 필터링
        return subscriptions.stream()
                .filter(s -> s.getUserId().equals(userId))
                .filter(s -> "ACTIVE".equals(s.getStatus()))
                .map(DepositSubscriptionDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 은행별 가입내역 조회
     */
    @Transactional(readOnly = true)
    public List<DepositSubscriptionDto> getSubscriptionsByBank(Long userId, String bankCode) {
        log.info("은행별 예금상품 가입내역 조회: userId={}, bankCode={}", userId, bankCode);
        
        List<DepositSubscription> subscriptions = depositSubscriptionRepository
                .findByUserIdAndBankCode(userId, bankCode);
        
        return subscriptions.stream()
                .map(DepositSubscriptionDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 예금상품 가입내역 생성
     */
    @Transactional
    public DepositSubscriptionDto createSubscription(DepositSubscriptionDto dto) {
        log.info("예금상품 가입내역 생성: userId={}, depositCode={}", dto.getUserId(), dto.getDepositCode());
        
        // 계좌번호 중복 확인
        if (depositSubscriptionRepository.findByAccountNumber(dto.getAccountNumber()).isPresent()) {
            throw new RuntimeException("이미 존재하는 계좌번호입니다: " + dto.getAccountNumber());
        }
        
        DepositSubscription subscription = dto.toEntity();
        DepositSubscription savedSubscription = depositSubscriptionRepository.save(subscription);
        
        log.info("예금상품 가입내역 생성 완료: subscriptionId={}", savedSubscription.getSubscriptionId());
        return DepositSubscriptionDto.fromEntity(savedSubscription);
    }

    /**
     * 예금상품 가입내역 수정
     */
    @Transactional
    public DepositSubscriptionDto updateSubscription(Long subscriptionId, DepositSubscriptionDto dto) {
        log.info("예금상품 가입내역 수정: subscriptionId={}", subscriptionId);
        
        DepositSubscription subscription = depositSubscriptionRepository
                .findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("가입내역을 찾을 수 없습니다: " + subscriptionId));
        
        // 수정 가능한 필드 업데이트
        if (dto.getStatus() != null) {
            subscription.setStatus(dto.getStatus());
        }
        if (dto.getCurrentBalance() != null) {
            subscription.setCurrentBalance(dto.getCurrentBalance());
        }
        if (dto.getUnpaidInterest() != null) {
            subscription.setUnpaidInterest(dto.getUnpaidInterest());
        }
        if (dto.getLastInterestCalculationDate() != null) {
            subscription.setLastInterestCalculationDate(dto.getLastInterestCalculationDate());
        }
        if (dto.getNextInterestPaymentDate() != null) {
            subscription.setNextInterestPaymentDate(dto.getNextInterestPaymentDate());
        }
        
        DepositSubscription updatedSubscription = depositSubscriptionRepository.save(subscription);
        
        log.info("예금상품 가입내역 수정 완료: subscriptionId={}", subscriptionId);
        return DepositSubscriptionDto.fromEntity(updatedSubscription);
    }

    /**
     * 예금상품 해지
     */
    @Transactional
    public void cancelSubscription(Long subscriptionId) {
        log.info("예금상품 해지: subscriptionId={}", subscriptionId);
        
        DepositSubscription subscription = depositSubscriptionRepository
                .findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("가입내역을 찾을 수 없습니다: " + subscriptionId));
        
        subscription.setStatus("CLOSED");
        depositSubscriptionRepository.save(subscription);
        
        log.info("예금상품 해지 완료: subscriptionId={}", subscriptionId);
    }

    /**
     * 만기 처리
     */
    @Transactional
    public void processMaturity(Long subscriptionId) {
        log.info("예금상품 만기 처리: subscriptionId={}", subscriptionId);
        
        DepositSubscription subscription = depositSubscriptionRepository
                .findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("가입내역을 찾을 수 없습니다: " + subscriptionId));
        
        subscription.setStatus("MATURED");
        depositSubscriptionRepository.save(subscription);
        
        log.info("예금상품 만기 처리 완료: subscriptionId={}", subscriptionId);
    }

    /**
     * 만기된 가입내역 자동 처리
     */
    @Transactional
    public void processMaturedSubscriptions() {
        log.info("만기된 예금상품 자동 처리 시작");
        
        List<DepositSubscription> maturedSubscriptions = depositSubscriptionRepository
                .findMaturingToday();
        
        for (DepositSubscription subscription : maturedSubscriptions) {
            subscription.setStatus("MATURED");
            depositSubscriptionRepository.save(subscription);
            log.info("만기 처리 완료: subscriptionId={}", subscription.getSubscriptionId());
        }
        
        log.info("만기된 예금상품 자동 처리 완료: {} 건", maturedSubscriptions.size());
    }

    /**
     * 이자 지급 처리
     */
    @Transactional
    public void processInterestPayment(Long subscriptionId) {
        log.info("이자 지급 처리: subscriptionId={}", subscriptionId);
        
        DepositSubscription subscription = depositSubscriptionRepository
                .findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("가입내역을 찾을 수 없습니다: " + subscriptionId));
        
        // 미지급 이자를 잔액에 추가
        if (subscription.getUnpaidInterest() != null && subscription.getUnpaidInterest().signum() > 0) {
            subscription.processInterestPayment(subscription.getUnpaidInterest());
            depositSubscriptionRepository.save(subscription);
            log.info("이자 지급 완료: subscriptionId={}, 이자금액={}", 
                    subscriptionId, subscription.getUnpaidInterest());
        }
    }
}
