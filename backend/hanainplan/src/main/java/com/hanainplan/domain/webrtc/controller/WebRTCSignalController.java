package com.hanainplan.domain.webrtc.controller;

import com.hanainplan.domain.webrtc.dto.CallRequestMessage;
import com.hanainplan.domain.webrtc.dto.IceCandidateMessage;
import com.hanainplan.domain.webrtc.dto.SdpMessage;
import com.hanainplan.domain.webrtc.dto.WebRTCMessage;
import com.hanainplan.domain.webrtc.service.WebRTCService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebRTCSignalController {

    private final SimpMessagingTemplate messagingTemplate;
    private final WebRTCService webRTCService;

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

