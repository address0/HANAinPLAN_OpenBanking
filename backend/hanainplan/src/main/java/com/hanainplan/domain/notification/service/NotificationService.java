package com.hanainplan.domain.notification.service;

import com.hanainplan.domain.notification.dto.NotificationDto;
import com.hanainplan.domain.notification.entity.Notification;
import com.hanainplan.domain.notification.entity.NotificationType;
import com.hanainplan.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public NotificationDto.Response createNotification(NotificationDto.CreateRequest request) {
        Notification notification = request.toEntity();
        Notification savedNotification = notificationRepository.save(notification);
        return NotificationDto.Response.from(savedNotification);
    }

    public Page<NotificationDto.Response> getUserNotifications(Long userId, Pageable pageable) {
        log.debug("Fetching notifications for user: {}, page: {}", userId, pageable.getPageNumber());

        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(NotificationDto.Response::from);
    }

    public Page<NotificationDto.Response> getUserUnreadNotifications(Long userId, Pageable pageable) {
        log.debug("Fetching unread notifications for user: {}, page: {}", userId, pageable.getPageNumber());

        Page<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(NotificationDto.Response::from);
    }

    public NotificationDto.Summary getNotificationSummary(Long userId) {
        Long totalCount = notificationRepository.countByUserId(userId);
        Long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(userId);

        return NotificationDto.Summary.of(totalCount, unreadCount);
    }

    public Page<NotificationDto.Response> getUserNotificationsByType(Long userId, NotificationType type, Pageable pageable) {
        log.debug("Fetching notifications for user: {}, type: {}, page: {}", userId, type, pageable.getPageNumber());

        Page<Notification> notifications = notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type, pageable);
        return notifications.map(NotificationDto.Response::from);
    }

    public Page<NotificationDto.Response> getUserNotificationsByPeriod(Long userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.debug("Fetching notifications for user: {}, period: {} to {}, page: {}", userId, startDate, endDate, pageable.getPageNumber());

        Page<Notification> notifications = notificationRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(userId, startDate, endDate, pageable);
        return notifications.map(NotificationDto.Response::from);
    }

    public NotificationDto.Response getNotificationById(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("알림을 찾을 수 없습니다. ID: " + notificationId));

        return NotificationDto.Response.from(notification);
    }

    @Transactional
    public NotificationDto.Response markAsRead(Long notificationId) {
        log.info("Marking notification as read: {}", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("알림을 찾을 수 없습니다. ID: " + notificationId));

        notification.markAsRead();
        Notification savedNotification = notificationRepository.save(notification);

        return NotificationDto.Response.from(savedNotification);
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        log.info("Marking all notifications as read for user: {}", userId);

        int updatedCount = notificationRepository.markAllAsReadByUserId(userId);
        log.info("Marked {} notifications as read for user: {}", updatedCount, userId);

        return updatedCount;
    }

    @Transactional
    public int markAllAsReadByType(Long userId, NotificationType type) {
        log.info("Marking all {} notifications as read for user: {}", type, userId);

        int updatedCount = notificationRepository.markAllAsReadByUserIdAndType(userId, type);
        log.info("Marked {} {} notifications as read for user: {}", updatedCount, type, userId);

        return updatedCount;
    }

    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        log.info("Deleting notification: {} for user: {}", notificationId, userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("알림을 찾을 수 없습니다. ID: " + notificationId));

        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("본인의 알림만 삭제할 수 있습니다.");
        }

        notificationRepository.delete(notification);
        log.info("Notification deleted: {}", notificationId);
    }

    @Transactional
    public int deleteNotifications(List<Long> notificationIds, Long userId) {
        log.info("Deleting {} notifications for user: {}", notificationIds.size(), userId);

        int deletedCount = notificationRepository.deleteNotificationsByIdsAndUserId(notificationIds, userId);
        log.info("Deleted {} notifications for user: {}", deletedCount, userId);

        return deletedCount;
    }

    @Transactional
    public int cleanupOldNotifications(Long userId) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        log.info("Cleaning up old notifications for user: {} before: {}", userId, cutoffDate);

        int deletedCount = notificationRepository.deleteOldReadNotifications(userId, cutoffDate);
        log.info("Deleted {} old notifications for user: {}", deletedCount, userId);

        return deletedCount;
    }

    @Transactional
    public NotificationDto.Response updateNotification(Long notificationId, NotificationDto.UpdateRequest request) {
        log.info("Updating notification: {}", notificationId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("알림을 찾을 수 없습니다. ID: " + notificationId));

        notification.updateNotification(request.getTitle(), request.getContent(), request.getType());
        Notification savedNotification = notificationRepository.save(notification);

        return NotificationDto.Response.from(savedNotification);
    }
}