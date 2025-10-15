package com.hanainplan.domain.webrtc.controller;

import com.hanainplan.domain.notification.service.EmailService;
import com.hanainplan.domain.schedule.service.ScheduleService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebRTCSignalController {

    private final SimpMessagingTemplate messagingTemplate;
    private final WebRTCService webRTCService;
    private final EmailService emailService;
    private final ScheduleService scheduleService;
    private final UserRepository userRepository;
    private final VideoCallRoomRepository videoCallRoomRepository;

    @Value("${app.base-url:http://localhost:5173}")
    private String baseUrl;

    public void notifyCallRequest(CallRequestMessage request, String roomId) {
        try {
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

        try {
            Optional<VideoCallRoom> callRoomOpt = videoCallRoomRepository.findByRoomId(message.getRoomId());
            if (callRoomOpt.isPresent()) {
                VideoCallRoom callRoom = callRoomOpt.get();
                Long customerId = callRoom.getCallerId();
                Long consultantId = callRoom.getCalleeId();

                Optional<User> customerOpt = userRepository.findById(customerId);
                Optional<User> consultantOpt = userRepository.findById(consultantId);

                if (customerOpt.isPresent() && consultantOpt.isPresent()) {
                    User customer = customerOpt.get();
                    User consultant = consultantOpt.get();

                    LocalDateTime startTime = LocalDateTime.now();
                    LocalDateTime endTime = startTime.plusHours(1);

                    try {
                        scheduleService.createConsultationSchedule(
                                consultantId, 
                                customerId, 
                                customer.getUserName(), 
                                startTime, 
                                endTime
                        );
                        log.info("상담 일정 자동 생성 완료 - consultantId: {}, customerId: {}", consultantId, customerId);
                    } catch (Exception e) {
                        log.error("상담 일정 자동 생성 실패", e);
                    }

                    if (customer.getEmail() != null && !customer.getEmail().isEmpty()) {
                        String consultationUrl = baseUrl + "/videocall?roomId=" + message.getRoomId();
                        emailService.sendConsultationAcceptedEmail(
                                customer.getEmail(),
                                customer.getUserName(),
                                consultant.getUserName(),
                                "일반상담",
                                message.getRoomId(),
                                consultationUrl
                        );
                        log.info("Consultation accepted email sent to customer: {}", customer.getEmail());
                    }
                } else {
                    log.warn("User not found for consultation room: {}", message.getRoomId());
                }
            }
        } catch (Exception e) {
            log.error("Failed to send consultation accepted notification", e);
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

    @MessageMapping("/consultation.start")
    public void handleConsultationStart(@Payload WebRTCMessage message) {
        log.info("🔔 Consultation start from consultant {} to customer {}, room {}", 
                message.getSenderId(), message.getReceiverId(), message.getRoomId());

        log.info("전송할 메시지 내용: type={}, roomId={}, senderId={}, receiverId={}", 
                message.getType(), message.getRoomId(), message.getSenderId(), message.getReceiverId());

        String destination = "/queue/consultation-start";
        String userIdStr = String.valueOf(message.getReceiverId());

        log.info("메시지 전송 - 목적지: /user/{}{}", userIdStr, destination);

        messagingTemplate.convertAndSendToUser(
                userIdStr, 
                destination, 
                message
        );

        log.info("✅ Consultation start message sent to customer {}", message.getReceiverId());
    }

    @MessageMapping("/consultation.step-sync")
    public void handleConsultationStepSync(@Payload WebRTCMessage message) {
        log.info("🔄 Consultation step sync from {} to {}, room {}, step: {}", 
                message.getSenderId(), message.getReceiverId(), message.getRoomId(), message.getData());

        messagingTemplate.convertAndSendToUser(
                String.valueOf(message.getReceiverId()), 
                "/queue/consultation-step-sync", 
                message
        );

        log.info("✅ Consultation step sync sent to {}", message.getReceiverId());
    }

    @MessageMapping("/consultation.note-sync")
    public void handleConsultationNoteSync(@Payload WebRTCMessage message) {
        log.info("📝 Consultation note sync from {} to {}, room {}", 
                message.getSenderId(), message.getReceiverId(), message.getRoomId());

        messagingTemplate.convertAndSendToUser(
                String.valueOf(message.getReceiverId()), 
                "/queue/consultation-note-sync", 
                message
        );

        log.info("✅ Consultation note sync sent to {}", message.getReceiverId());
    }
}