package com.hanainplan.domain.notification.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Firebase Cloud Messaging (FCM) 서비스
 * - 상담원에게 푸시 알림 전송
 * - 고객에게 상담 수락 알림 전송
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FCMService {

    /**
     * 단일 디바이스에 알림 전송
     */
    public String sendNotificationToDevice(String deviceToken, String title, String body, Map<String, String> data) {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                log.warn("Firebase not initialized. Skipping notification: {} - {}", title, body);
                return null;
            }

            // 알림 메시지 생성
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            // 메시지 생성
            Message.Builder messageBuilder = Message.builder()
                    .setToken(deviceToken)
                    .setNotification(notification);

            // 데이터 추가 (선택사항)
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            // 웹 푸시 설정 (선택사항)
            messageBuilder.setWebpushConfig(WebpushConfig.builder()
                    .setNotification(WebpushNotification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .setIcon("/logo/hana-logo.png")
                            .build())
                    .build());

            Message message = messageBuilder.build();

            // 메시지 전송
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent FCM message: {}", response);
            return response;

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM message to device: {}", deviceToken, e);
            return null;
        }
    }

    /**
     * 여러 디바이스에 알림 전송
     */
    public BatchResponse sendNotificationToDevices(List<String> deviceTokens, String title, String body, Map<String, String> data) {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                log.warn("Firebase not initialized. Skipping batch notification: {} - {}", title, body);
                return null;
            }

            // 알림 메시지 생성
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            // 멀티캐스트 메시지 생성
            MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                    .addAllTokens(deviceTokens)
                    .setNotification(notification);

            // 데이터 추가 (선택사항)
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            // 웹 푸시 설정
            messageBuilder.setWebpushConfig(WebpushConfig.builder()
                    .setNotification(WebpushNotification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .setIcon("/logo/hana-logo.png")
                            .build())
                    .build());

            MulticastMessage message = messageBuilder.build();

            // 메시지 전송
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            log.info("Successfully sent FCM batch message. Success count: {}, Failure count: {}",
                    response.getSuccessCount(), response.getFailureCount());

            // 실패한 토큰 로깅
            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        log.error("Failed to send to token {}: {}", 
                                deviceTokens.get(i), 
                                responses.get(i).getException().getMessage());
                    }
                }
            }

            return response;

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM batch message", e);
            return null;
        }
    }

    /**
     * 토픽 구독 (특정 주제에 대한 알림 수신)
     */
    public void subscribeToTopic(List<String> deviceTokens, String topic) {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                log.warn("Firebase not initialized. Skipping topic subscription");
                return;
            }

            TopicManagementResponse response = FirebaseMessaging.getInstance()
                    .subscribeToTopic(deviceTokens, topic);

            log.info("Successfully subscribed {} devices to topic: {}. Failure count: {}",
                    response.getSuccessCount(), topic, response.getFailureCount());

        } catch (FirebaseMessagingException e) {
            log.error("Failed to subscribe to topic: {}", topic, e);
        }
    }

    /**
     * 토픽 구독 해제
     */
    public void unsubscribeFromTopic(List<String> deviceTokens, String topic) {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                log.warn("Firebase not initialized. Skipping topic unsubscription");
                return;
            }

            TopicManagementResponse response = FirebaseMessaging.getInstance()
                    .unsubscribeFromTopic(deviceTokens, topic);

            log.info("Successfully unsubscribed {} devices from topic: {}. Failure count: {}",
                    response.getSuccessCount(), topic, response.getFailureCount());

        } catch (FirebaseMessagingException e) {
            log.error("Failed to unsubscribe from topic: {}", topic, e);
        }
    }

    /**
     * 상담 요청 알림 전송 (상담원에게)
     */
    public String sendConsultationRequestNotification(String deviceToken, String customerName, String consultationType, String roomId) {
        Map<String, String> data = new HashMap<>();
        data.put("type", "CONSULTATION_REQUEST");
        data.put("customerName", customerName);
        data.put("consultationType", consultationType);
        data.put("roomId", roomId);
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));

        return sendNotificationToDevice(
                deviceToken,
                "새로운 상담 요청",
                customerName + "님께서 " + consultationType + " 상담을 요청하셨습니다.",
                data
        );
    }

    /**
     * 상담 수락 알림 전송 (고객에게)
     */
    public String sendConsultationAcceptedNotification(String deviceToken, String consultantName, String consultationType, String roomId) {
        Map<String, String> data = new HashMap<>();
        data.put("type", "CONSULTATION_ACCEPTED");
        data.put("consultantName", consultantName);
        data.put("consultationType", consultationType);
        data.put("roomId", roomId);
        data.put("timestamp", String.valueOf(System.currentTimeMillis()));

        return sendNotificationToDevice(
                deviceToken,
                "상담이 수락되었습니다",
                consultantName + " 상담원이 상담을 수락했습니다. 잠시 후 상담이 시작됩니다.",
                data
        );
    }

    /**
     * 일반 알림 전송
     */
    public String sendGeneralNotification(String deviceToken, String title, String message) {
        return sendNotificationToDevice(deviceToken, title, message, null);
    }
}