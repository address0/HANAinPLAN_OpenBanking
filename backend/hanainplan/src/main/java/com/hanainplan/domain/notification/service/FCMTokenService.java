package com.hanainplan.domain.notification.service;

import com.hanainplan.domain.notification.entity.FCMToken;
import com.hanainplan.domain.notification.repository.FCMTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * FCM 토큰 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FCMTokenService {

    private final FCMTokenRepository fcmTokenRepository;

    /**
     * FCM 토큰 등록 또는 업데이트
     * - Race Condition 방지를 위해 중복 키 예외 처리 추가
     */
    public FCMToken registerOrUpdateToken(Long userId, String deviceToken, String deviceId, String deviceType) {
        try {
            // 기존 토큰 확인
            Optional<FCMToken> existingToken = deviceId != null 
                    ? fcmTokenRepository.findByUserIdAndDeviceId(userId, deviceId)
                    : fcmTokenRepository.findByDeviceToken(deviceToken);

            if (existingToken.isPresent()) {
                // 기존 토큰 업데이트
                FCMToken token = existingToken.get();
                token.updateToken(deviceToken);
                token.updateLastUsed();
                log.info("Updated FCM token for user: {}, device: {}", userId, deviceId);
                return fcmTokenRepository.save(token);
            } else {
                // 새 토큰 생성
                FCMToken newToken = FCMToken.builder()
                        .userId(userId)
                        .deviceToken(deviceToken)
                        .deviceId(deviceId)
                        .deviceType(deviceType)
                        .isActive(true)
                        .lastUsedAt(LocalDateTime.now())
                        .build();
                log.info("Registered new FCM token for user: {}, device: {}", userId, deviceId);
                return fcmTokenRepository.save(newToken);
            }
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // 중복 키 예외 발생 시 (동시 요청으로 인한 Race Condition)
            // 이미 생성된 토큰을 다시 조회하여 업데이트
            log.warn("Duplicate key detected, retrying update for user: {}, device: {}", userId, deviceId);
            
            Optional<FCMToken> existingToken = deviceId != null 
                    ? fcmTokenRepository.findByUserIdAndDeviceId(userId, deviceId)
                    : fcmTokenRepository.findByDeviceToken(deviceToken);
            
            if (existingToken.isPresent()) {
                FCMToken token = existingToken.get();
                token.updateToken(deviceToken);
                token.updateLastUsed();
                log.info("Updated FCM token after retry for user: {}, device: {}", userId, deviceId);
                return fcmTokenRepository.save(token);
            } else {
                log.error("Failed to find token after duplicate key exception for user: {}, device: {}", userId, deviceId);
                throw e;
            }
        }
    }

    /**
     * 사용자의 모든 활성 토큰 조회
     */
    public List<String> getActiveDeviceTokens(Long userId) {
        return fcmTokenRepository.findActiveTokensByUserId(userId)
                .stream()
                .map(FCMToken::getDeviceToken)
                .collect(Collectors.toList());
    }

    /**
     * 토큰 비활성화
     */
    public void deactivateToken(String deviceToken) {
        fcmTokenRepository.findByDeviceToken(deviceToken)
                .ifPresent(token -> {
                    token.deactivate();
                    fcmTokenRepository.save(token);
                    log.info("Deactivated FCM token for user: {}", token.getUserId());
                });
    }

    /**
     * 사용자의 모든 토큰 비활성화 (로그아웃 시)
     */
    public void deactivateAllUserTokens(Long userId) {
        List<FCMToken> tokens = fcmTokenRepository.findActiveTokensByUserId(userId);
        tokens.forEach(FCMToken::deactivate);
        fcmTokenRepository.saveAll(tokens);
        log.info("Deactivated all FCM tokens for user: {}", userId);
    }

    /**
     * 오래된 비활성 토큰 삭제 (스케줄러로 주기적 실행)
     */
    public void cleanupOldTokens(int daysOld) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysOld);
        fcmTokenRepository.deleteInactiveTokensOlderThan(threshold);
        log.info("Cleaned up inactive FCM tokens older than {} days", daysOld);
    }

    /**
     * 토큰 존재 여부 확인
     */
    public boolean hasActiveToken(Long userId) {
        return !fcmTokenRepository.findActiveTokensByUserId(userId).isEmpty();
    }
}


