package com.hanainplan.domain.notification.repository;

import com.hanainplan.domain.notification.entity.Notification;
import com.hanainplan.domain.notification.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 알림 리포지토리 인터페이스
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 특정 사용자의 모든 알림 조회 (페이징)
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 특정 사용자의 읽지 않은 알림 조회 (페이징)
     */
    Page<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 특정 사용자의 읽지 않은 알림 개수 조회
     */
    Long countByUserIdAndIsReadFalse(Long userId);

    /**
     * 특정 사용자의 전체 알림 개수 조회
     */
    Long countByUserId(Long userId);

    /**
     * 특정 사용자의 특정 타입 알림 조회 (페이징)
     */
    Page<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, NotificationType type, Pageable pageable);

    /**
     * 특정 기간 동안의 알림 조회 (페이징)
     */
    Page<Notification> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        Long userId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    );

    /**
     * 특정 사용자의 모든 알림을 읽음 처리
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.userId = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") Long userId);

    /**
     * 특정 사용자의 특정 타입 알림들을 읽음 처리
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.userId = :userId AND n.type = :type AND n.isRead = false")
    int markAllAsReadByUserIdAndType(@Param("userId") Long userId, @Param("type") NotificationType type);

    /**
     * 오래된 읽은 알림들 삭제 (30일 이전)
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.userId = :userId AND n.isRead = true AND n.createdAt < :cutoffDate")
    int deleteOldReadNotifications(@Param("userId") Long userId, @Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * 특정 알림들을 일괄 삭제
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.id IN :ids AND n.userId = :userId")
    int deleteNotificationsByIdsAndUserId(@Param("ids") List<Long> ids, @Param("userId") Long userId);
}
