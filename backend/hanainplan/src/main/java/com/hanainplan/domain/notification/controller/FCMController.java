package com.hanainplan.domain.notification.controller;

import com.hanainplan.domain.notification.service.FCMService;
import com.hanainplan.domain.notification.service.FCMTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * FCM (Firebase Cloud Messaging) 컨트롤러
 * - FCM 토큰 등록/관리
 * - 푸시 알림 전송
 */
@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
@Slf4j
public class FCMController {

    private final FCMTokenService fcmTokenService;
    private final FCMService fcmService;

    /**
     * FCM 토큰 등록 또는 업데이트
     */
    @PostMapping("/token")
    public ResponseEntity<?> registerToken(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            String deviceToken = request.get("deviceToken").toString();
            String deviceId = request.getOrDefault("deviceId", "").toString();
            String deviceType = request.getOrDefault("deviceType", "WEB").toString();

            fcmTokenService.registerOrUpdateToken(userId, deviceToken, deviceId, deviceType);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "FCM 토큰이 성공적으로 등록되었습니다."
            ));
        } catch (Exception e) {
            log.error("Error registering FCM token", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "FCM 토큰 등록에 실패했습니다: " + e.getMessage()
            ));
        }
    }

    /**
     * FCM 토큰 비활성화 (로그아웃 시)
     */
    @PostMapping("/token/deactivate")
    public ResponseEntity<?> deactivateToken(@RequestBody Map<String, Object> request) {
        try {
            String deviceToken = request.get("deviceToken").toString();
            fcmTokenService.deactivateToken(deviceToken);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "FCM 토큰이 비활성화되었습니다."
            ));
        } catch (Exception e) {
            log.error("Error deactivating FCM token", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "FCM 토큰 비활성화에 실패했습니다: " + e.getMessage()
            ));
        }
    }

    /**
     * 사용자의 모든 FCM 토큰 비활성화
     */
    @PostMapping("/token/deactivate-all")
    public ResponseEntity<?> deactivateAllUserTokens(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            fcmTokenService.deactivateAllUserTokens(userId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "모든 FCM 토큰이 비활성화되었습니다."
            ));
        } catch (Exception e) {
            log.error("Error deactivating all user tokens", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "FCM 토큰 비활성화에 실패했습니다: " + e.getMessage()
            ));
        }
    }

    /**
     * 테스트용 알림 전송
     */
    @PostMapping("/test")
    public ResponseEntity<?> sendTestNotification(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            String title = request.getOrDefault("title", "테스트 알림").toString();
            String message = request.getOrDefault("message", "FCM 알림 테스트입니다.").toString();

            List<String> tokens = fcmTokenService.getActiveDeviceTokens(userId);
            
            if (tokens.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "등록된 FCM 토큰이 없습니다."
                ));
            }

            // 첫 번째 토큰으로 알림 전송
            String response = fcmService.sendGeneralNotification(tokens.get(0), title, message);

            return ResponseEntity.ok(Map.of(
                    "success", response != null,
                    "message", response != null ? "알림이 전송되었습니다." : "알림 전송에 실패했습니다.",
                    "fcmResponse", response != null ? response : ""
            ));
        } catch (Exception e) {
            log.error("Error sending test notification", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "알림 전송에 실패했습니다: " + e.getMessage()
            ));
        }
    }

    /**
     * 사용자의 활성 토큰 수 조회
     */
    @GetMapping("/token/count/{userId}")
    public ResponseEntity<?> getActiveTokenCount(@PathVariable Long userId) {
        try {
            List<String> tokens = fcmTokenService.getActiveDeviceTokens(userId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "userId", userId,
                    "tokenCount", tokens.size(),
                    "hasToken", !tokens.isEmpty()
            ));
        } catch (Exception e) {
            log.error("Error getting token count", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "토큰 수 조회에 실패했습니다: " + e.getMessage()
            ));
        }
    }
}

