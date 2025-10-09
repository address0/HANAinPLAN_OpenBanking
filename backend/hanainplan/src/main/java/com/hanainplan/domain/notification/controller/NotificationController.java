package com.hanainplan.domain.notification.controller;

import com.hanainplan.domain.notification.dto.NotificationDto;
import com.hanainplan.domain.notification.entity.NotificationType;
import com.hanainplan.domain.notification.repository.NotificationRepository;
import com.hanainplan.domain.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 알림 컨트롤러
 * - 알림 관련 REST API 엔드포인트 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 관리 API")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    /**
     * 사용자 알림 목록 조회
     */
    @GetMapping
    @Operation(summary = "사용자 알림 목록 조회", description = "현재 로그인한 사용자의 알림 목록을 페이징으로 조회합니다.")
    public ResponseEntity<Page<NotificationDto.Response>> getUserNotifications(
            Authentication authentication,
            @Parameter(description = "사용자 ID (개발용)") @RequestParam(required = false) Long userId,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size) {

        Long targetUserId;

        // userId 파라미터가 제공되었으면 이를 우선 사용
        if (userId != null) {
            targetUserId = userId;
        } else {
            // 인증 정보에서 사용자 ID 추출 시도
            targetUserId = getUserIdFromAuthentication(authentication);
            if (targetUserId == null) {
                return ResponseEntity.badRequest().body(null);
            }
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDto.Response> notifications = notificationService.getUserNotifications(targetUserId, pageable);
        return ResponseEntity.ok(notifications);
    }

    /**
     * 사용자 읽지 않은 알림 목록 조회
     */
    @GetMapping("/unread")
    @Operation(summary = "읽지 않은 알림 목록 조회", description = "현재 로그인한 사용자의 읽지 않은 알림 목록을 페이징으로 조회합니다.")
    public ResponseEntity<Page<NotificationDto.Response>> getUserUnreadNotifications(
            Authentication authentication,
            @Parameter(description = "사용자 ID (개발용)") @RequestParam(required = false) Long userId,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size) {

        Long targetUserId = (userId != null) ? userId : getUserIdFromAuthentication(authentication);
        Pageable pageable = PageRequest.of(page, size);

        Page<NotificationDto.Response> notifications = notificationService.getUserUnreadNotifications(targetUserId, pageable);
        return ResponseEntity.ok(notifications);
    }

    /**
     * 알림 개수 요약 조회
     */
    @GetMapping("/summary")
    @Operation(summary = "알림 개수 요약 조회", description = "현재 로그인한 사용자의 총 알림 수와 읽지 않은 알림 수를 조회합니다.")
    public ResponseEntity<NotificationDto.Summary> getNotificationSummary(
            Authentication authentication,
            @Parameter(description = "사용자 ID (개발용)") @RequestParam(required = false) Long userId) {
        Long targetUserId = (userId != null) ? userId : getUserIdFromAuthentication(authentication);
        NotificationDto.Summary summary = notificationService.getNotificationSummary(targetUserId);
        return ResponseEntity.ok(summary);
    }

    /**
     * 특정 타입의 알림 목록 조회
     */
    @GetMapping("/type/{type}")
    @Operation(summary = "특정 타입 알림 목록 조회", description = "현재 로그인한 사용자의 특정 타입 알림 목록을 페이징으로 조회합니다.")
    public ResponseEntity<Page<NotificationDto.Response>> getUserNotificationsByType(
            Authentication authentication,
            @Parameter(description = "알림 타입") @PathVariable NotificationType type,
            @Parameter(description = "사용자 ID (개발용)") @RequestParam(required = false) Long userId,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size) {

        Long targetUserId = (userId != null) ? userId : getUserIdFromAuthentication(authentication);
        Pageable pageable = PageRequest.of(page, size);

        Page<NotificationDto.Response> notifications = notificationService.getUserNotificationsByType(targetUserId, type, pageable);
        return ResponseEntity.ok(notifications);
    }

    /**
     * 기간별 알림 목록 조회
     */
    @GetMapping("/period")
    @Operation(summary = "기간별 알림 목록 조회", description = "현재 로그인한 사용자의 특정 기간 알림 목록을 페이징으로 조회합니다.")
    public ResponseEntity<Page<NotificationDto.Response>> getUserNotificationsByPeriod(
            Authentication authentication,
            @Parameter(description = "시작일 (yyyy-MM-ddTHH:mm:ss)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "종료일 (yyyy-MM-ddTHH:mm:ss)") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size) {

        Long userId = getUserIdFromAuthentication(authentication);
        Pageable pageable = PageRequest.of(page, size);

        Page<NotificationDto.Response> notifications = notificationService.getUserNotificationsByPeriod(userId, startDate, endDate, pageable);
        return ResponseEntity.ok(notifications);
    }

    /**
     * 단일 알림 조회
     */
    @GetMapping("/{notificationId}")
    @Operation(summary = "단일 알림 조회", description = "알림 ID로 특정 알림을 조회합니다.")
    public ResponseEntity<NotificationDto.Response> getNotification(
            Authentication authentication,
            @Parameter(description = "알림 ID") @PathVariable Long notificationId,
            @Parameter(description = "사용자 ID (개발용)") @RequestParam(required = false) Long userId) {

        NotificationDto.Response notification = notificationService.getNotificationById(notificationId);
        // 본인의 알림인지 확인
        Long targetUserId = (userId != null) ? userId : getUserIdFromAuthentication(authentication);
        if (!notification.getUserId().equals(targetUserId)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(notification);
    }

    /**
     * 알림 읽음 처리
     */
    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 처리합니다.")
    public ResponseEntity<ApiResponse> markAsRead(
            Authentication authentication,
            @Parameter(description = "알림 ID") @PathVariable Long notificationId,
            @Parameter(description = "사용자 ID (개발용)") @RequestParam(required = false) Long userId) {

        try {
            Long targetUserId;

            // userId 파라미터가 제공되었으면 이를 우선 사용
            if (userId != null) {
                targetUserId = userId;
                log.info("사용자 ID 파라미터 사용: {}", userId);
            } else {
                // 인증 정보에서 사용자 ID 추출 시도
                targetUserId = getUserIdFromAuthentication(authentication);
                if (targetUserId == null) {
                    log.error("알림 읽음 처리 실패 - 사용자 ID를 확인할 수 없습니다");
                    return ResponseEntity.badRequest().body(ApiResponse.builder()
                            .success(false)
                            .message("사용자 인증 정보가 필요합니다.")
                            .build());
                }
            }

            // 알림 존재 확인
            if (!notificationRepository.existsById(notificationId)) {
                log.warn("알림 읽음 처리 실패 - 알림 존재하지 않음, 알림 ID: {}", notificationId);
                return ResponseEntity.notFound().build();
            }

            // 알림 조회 및 본인 소유 확인
            NotificationDto.Response notification = notificationService.getNotificationById(notificationId);

            // 본인의 알림인지 확인
            if (!notification.getUserId().equals(targetUserId)) {
                log.warn("알림 읽음 처리 권한 없음 - 요청 사용자 ID: {}, 알림 소유자 ID: {}", targetUserId, notification.getUserId());
                return ResponseEntity.notFound().build();
            }

            NotificationDto.Response updatedNotification = notificationService.markAsRead(notificationId);
            log.info("알림 읽음 처리 완료 - 알림 ID: {}, 사용자 ID: {}", notificationId, targetUserId);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("알림을 읽음 처리했습니다.")
                    .build());
        } catch (Exception e) {
            // 기타 예외 처리
            log.error("알림 읽음 처리 중 오류 발생 - 알림 ID: {}", notificationId, e);
            return ResponseEntity.status(500).body(ApiResponse.builder()
                    .success(false)
                    .message("알림 읽음 처리 중 오류가 발생했습니다.")
                    .build());
        }
    }

    /**
     * 모든 알림 읽음 처리
     */
    @PatchMapping("/read-all")
    @Operation(summary = "모든 알림 읽음 처리", description = "현재 로그인한 사용자의 모든 알림을 읽음 처리합니다.")
    public ResponseEntity<ApiResponse> markAllAsRead(
            Authentication authentication,
            @Parameter(description = "사용자 ID (개발용)") @RequestParam(required = false) Long userId) {
        Long targetUserId = (userId != null) ? userId : getUserIdFromAuthentication(authentication);
        int updatedCount = notificationService.markAllAsRead(targetUserId);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message(String.format("%d개의 알림을 읽음 처리했습니다.", updatedCount))
                .build());
    }

    /**
     * 특정 타입의 모든 알림 읽음 처리
     */
    @PatchMapping("/type/{type}/read-all")
    @Operation(summary = "특정 타입 모든 알림 읽음 처리", description = "현재 로그인한 사용자의 특정 타입 모든 알림을 읽음 처리합니다.")
    public ResponseEntity<ApiResponse> markAllAsReadByType(
            Authentication authentication,
            @Parameter(description = "알림 타입") @PathVariable NotificationType type,
            @Parameter(description = "사용자 ID (개발용)") @RequestParam(required = false) Long userId) {

        Long targetUserId = (userId != null) ? userId : getUserIdFromAuthentication(authentication);
        int updatedCount = notificationService.markAllAsReadByType(targetUserId, type);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message(String.format("%d개의 %s 알림을 읽음 처리했습니다.", updatedCount, type.getDescription()))
                .build());
    }

    /**
     * 알림 삭제
     */
    @DeleteMapping("/{notificationId}")
    @Operation(summary = "알림 삭제", description = "특정 알림을 삭제합니다.")
    public ResponseEntity<ApiResponse> deleteNotification(
            Authentication authentication,
            @Parameter(description = "알림 ID") @PathVariable Long notificationId,
            @Parameter(description = "사용자 ID (개발용)") @RequestParam(required = false) Long userId) {

        Long targetUserId = (userId != null) ? userId : getUserIdFromAuthentication(authentication);
        notificationService.deleteNotification(notificationId, targetUserId);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("알림이 삭제되었습니다.")
                .build());
    }

    /**
     * 여러 알림 일괄 삭제
     */
    @DeleteMapping("/batch")
    @Operation(summary = "여러 알림 일괄 삭제", description = "여러 알림을 일괄 삭제합니다.")
    public ResponseEntity<ApiResponse> deleteNotifications(
            Authentication authentication,
            @Parameter(description = "알림 ID 목록") @RequestBody List<Long> notificationIds,
            @Parameter(description = "사용자 ID (개발용)") @RequestParam(required = false) Long userId) {

        Long targetUserId = (userId != null) ? userId : getUserIdFromAuthentication(authentication);
        int deletedCount = notificationService.deleteNotifications(notificationIds, targetUserId);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message(String.format("%d개의 알림이 삭제되었습니다.", deletedCount))
                .build());
    }

    /**
     * 오래된 알림 정리
     */
    @DeleteMapping("/cleanup")
    @Operation(summary = "오래된 알림 정리", description = "30일 이전의 읽은 알림들을 정리합니다.")
    public ResponseEntity<ApiResponse> cleanupOldNotifications(
            Authentication authentication,
            @Parameter(description = "사용자 ID (개발용)") @RequestParam(required = false) Long userId) {
        Long targetUserId = (userId != null) ? userId : getUserIdFromAuthentication(authentication);
        int deletedCount = notificationService.cleanupOldNotifications(targetUserId);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message(String.format("%d개의 오래된 알림이 정리되었습니다.", deletedCount))
                .build());
    }

    /**
     * Authentication에서 사용자 ID 추출
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        // 인증 정보가 없는 경우 (개발/테스트용)
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("인증 정보가 없습니다. 인증되지 않은 요청입니다.");
            return null;
        }

        // 실제 구현에서는 JWT 토큰이나 세션에서 사용자 ID를 추출
        try {
            // Principal이 User 객체인 경우를 먼저 확인
            Object principal = authentication.getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.User) {
                // User 객체에서 username을 사용자 ID로 사용 (실제 구현 시 수정 필요)
                String username = ((org.springframework.security.core.userdetails.User) principal).getUsername();
                return Long.parseLong(username);
            } else if (principal instanceof String) {
                // String인 경우 직접 파싱
                return Long.parseLong((String) principal);
            } else {
                log.warn("알 수 없는 Principal 타입: {}", principal.getClass());
                return null;
            }
        } catch (Exception e) {
            log.warn("사용자 ID 추출에 실패했습니다: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * API 응답 래퍼 클래스
     */
    @Getter
    @RequiredArgsConstructor
    public static class ApiResponse {
        private final boolean success;
        private final String message;

        public static ApiResponseBuilder builder() {
            return new ApiResponseBuilder();
        }

        public static class ApiResponseBuilder {
            private boolean success;
            private String message;

            public ApiResponseBuilder success(boolean success) {
                this.success = success;
                return this;
            }

            public ApiResponseBuilder message(String message) {
                this.message = message;
                return this;
            }

            public ApiResponse build() {
                return new ApiResponse(success, message);
            }
        }
    }
}
