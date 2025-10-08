package com.hanainplan.domain.notification.repository;

import com.hanainplan.domain.notification.entity.FCMToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * FCM 토큰 Repository
 */
@Repository
public interface FCMTokenRepository extends JpaRepository<FCMToken, Long> {

    /**
     * 사용자의 활성 토큰 목록 조회
     */
    @Query("SELECT f FROM FCMToken f WHERE f.userId = :userId AND f.isActive = true")
    List<FCMToken> findActiveTokensByUserId(@Param("userId") Long userId);

    /**
     * 사용자의 모든 토큰 조회
     */
    List<FCMToken> findByUserId(Long userId);

    /**
     * 특정 디바이스의 토큰 조회
     */
    Optional<FCMToken> findByUserIdAndDeviceId(Long userId, String deviceId);

    /**
     * 토큰으로 조회
     */
    Optional<FCMToken> findByDeviceToken(String deviceToken);

    /**
     * 사용자의 특정 디바이스 타입 토큰 조회
     */
    List<FCMToken> findByUserIdAndDeviceType(Long userId, String deviceType);

    /**
     * 비활성 토큰 삭제
     */
    @Query("DELETE FROM FCMToken f WHERE f.isActive = false AND f.lastUsedAt < :threshold")
    void deleteInactiveTokensOlderThan(@Param("threshold") java.time.LocalDateTime threshold);
}



