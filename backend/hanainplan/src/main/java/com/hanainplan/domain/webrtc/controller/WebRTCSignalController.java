package com.hanainplan.domain.webrtc.controller;

import com.hanainplan.domain.notification.service.EmailService;
import com.hanainplan.domain.notification.service.FCMService;
import com.hanainplan.domain.notification.service.FCMTokenService;
import com.hanainplan.domain.user.entity.User;
import com.hanainplan.domain.user.repository.UserRepository;
import com.hanainplan.domain.webrtc.dto.CallRequestMessage;
import com.hanainplan.domain.webrtc.dto.IceCandidateMessage;
import com.hanainplan.domain.webrtc.dto.SdpMessage;
import com.hanainplan.domain.webrtc.dto.WebRTCMessage;
import com.hanainplan.domain.webrtc.entity.VideoCallRoom;
import com.hanainplan.domain.webrtc.repository.VideoCallRoomRepository;
import com.hanainplan.domain.webrtc.service.WebRTCService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebRTCSignalController {

    private final SimpMessagingTemplate messagingTemplate;
    private final WebRTCService webRTCService;
    private final EmailService emailService;
    private final FCMService fcmService;
    private final FCMTokenService fcmTokenService;
    private final UserRepository userRepository;
    private final VideoCallRoomRepository videoCallRoomRepository;

    @Value("${app.base-url:http://localhost:5173}")
    private String baseUrl;

    // 통화 요청 (REST에서 생성되지만, 수신자에게는 WS로 알림)
    public void notifyCallRequest(CallRequestMessage request, String roomId) {
        try {
            // 수신자 앱은 CallRequestMessage 형태를 기대하므로 그대로 전송
            request.setRoomId(roomId);
            messagingTemplate.convertAndSendToUser(String.valueOf(request.getCalleeId()), "/queue/call-request", request);
        } catch (Exception e) {
            log.error("Failed to notify call request", e);
        }
    }

    @MessageMapping("/call.accept")
    public void handleCallAccept(@Payload WebRTCMessage message) {
        log.info("Call accept from {} to {}, room {}", message.getSenderId(), message.getReceiverId(), message.getRoomId());
        webRTCService.acceptCall(message.getRoomId());
        messagingTemplate.convertAndSendToUser(String.valueOf(message.getReceiverId()), "/queue/call-accept", message);

        // 상담 수락 시 고객에게 알림 발송
        try {
            Optional<VideoCallRoom> callRoomOpt = videoCallRoomRepository.findByRoomId(message.getRoomId());
            if (callRoomOpt.isPresent()) {
                VideoCallRoom callRoom = callRoomOpt.get();
                Long customerId = callRoom.getCallerId(); // 상담 요청한 고객
                Long consultantId = callRoom.getCalleeId(); // 수락한 상담원

                // 고객 정보 조회
                Optional<User> customerOpt = userRepository.findById(customerId);
                Optional<User> consultantOpt = userRepository.findById(consultantId);

                if (customerOpt.isPresent() && consultantOpt.isPresent()) {
                    User customer = customerOpt.get();
                    User consultant = consultantOpt.get();

                    // 이메일 발송 (고객에게)
                    if (customer.getEmail() != null && !customer.getEmail().isEmpty()) {
                        String consultationUrl = baseUrl + "/videocall?roomId=" + message.getRoomId();
                        emailService.sendConsultationAcceptedEmail(
                                customer.getEmail(),
                                customer.getUserName(),
                                consultant.getUserName(),
                                "일반상담", // TODO: 실제 상담 유형 가져오기
                                message.getRoomId(),
                                consultationUrl
                        );
                        log.info("Consultation accepted email sent to customer: {}", customer.getEmail());
                    }

                    // FCM 푸시 알림 (고객에게)
                    List<String> customerTokens = fcmTokenService.getActiveDeviceTokens(customerId);
                    if (!customerTokens.isEmpty()) {
                        fcmService.sendConsultationAcceptedNotification(
                                customerTokens.get(0),
                                consultant.getUserName(),
                                "일반상담", // TODO: 실제 상담 유형 가져오기
                                message.getRoomId()
                        );
                        log.info("FCM notification sent to customer {}", customerId);
                    }
                } else {
                    log.warn("User not found for consultation room: {}", message.getRoomId());
                }
            }
        } catch (Exception e) {
            log.error("Failed to send consultation accepted notification", e);
            // 알림 실패해도 상담은 진행되도록 예외를 던지지 않음
        }
    }

    @MessageMapping("/call.reject")
    public void handleCallReject(@Payload WebRTCMessage message) {
        log.info("Call reject from {} to {}, room {}", message.getSenderId(), message.getReceiverId(), message.getRoomId());
        webRTCService.rejectCall(message.getRoomId());
        messagingTemplate.convertAndSendToUser(String.valueOf(message.getReceiverId()), "/queue/call-reject", message);
    }

    @MessageMapping("/call.end")
    public void handleCallEnd(@Payload WebRTCMessage message) {
        log.info("Call end from {} to {}, room {}", message.getSenderId(), message.getReceiverId(), message.getRoomId());
        webRTCService.endCall(message.getRoomId());
        messagingTemplate.convertAndSendToUser(String.valueOf(message.getReceiverId()), "/queue/call-end", message);
    }

    @MessageMapping("/webrtc.offer")
    public void handleOffer(@Payload SdpMessage offer) {
        log.info("Offer from {} to {}, room {}", offer.getSenderId(), offer.getReceiverId(), offer.getRoomId());
        messagingTemplate.convertAndSendToUser(String.valueOf(offer.getReceiverId()), "/queue/webrtc-offer", offer);
    }

    @MessageMapping("/webrtc.answer")
    public void handleAnswer(@Payload SdpMessage answer) {
        log.info("Answer from {} to {}, room {}", answer.getSenderId(), answer.getReceiverId(), answer.getRoomId());
        messagingTemplate.convertAndSendToUser(String.valueOf(answer.getReceiverId()), "/queue/webrtc-answer", answer);
    }

    @MessageMapping("/webrtc.ice")
    public void handleIce(@Payload IceCandidateMessage ice) {
        log.debug("ICE from {} to {}, room {}", ice.getSenderId(), ice.getReceiverId(), ice.getRoomId());
        messagingTemplate.convertAndSendToUser(String.valueOf(ice.getReceiverId()), "/queue/webrtc-ice", ice);
    }
}

