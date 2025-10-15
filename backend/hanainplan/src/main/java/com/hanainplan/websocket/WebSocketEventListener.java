package com.hanainplan.websocket;

import com.hanainplan.domain.webrtc.service.WebRTCService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final WebRTCService webRTCService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        String userIdStr = headerAccessor.getFirstNativeHeader("userId");

        if (userIdStr != null) {
            try {
                Long userId = Long.parseLong(userIdStr);
                webRTCService.setUserOnline(userId, sessionId, true);
                log.info("User {} connected to WebSocket, sessionId: {}", userId, sessionId);
            } catch (NumberFormatException e) {
                log.warn("Invalid userId format in WebSocket connection: {}", userIdStr);
            }
        } else {
            log.warn("WebSocket connection without userId header, sessionId: {}", sessionId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        webRTCService.setUserOfflineBySession(sessionId);
        log.info("WebSocket session disconnected: {}", sessionId);
    }
} 