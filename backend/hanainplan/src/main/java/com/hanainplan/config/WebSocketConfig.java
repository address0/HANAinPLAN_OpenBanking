package com.hanainplan.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트에서 메시지를 받을 prefix 설정
        config.setApplicationDestinationPrefixes("/app");
        
        // 메시지를 발행하는 요청 url -> 클라이언트에서 서버로 메시지 전송시 사용
        // /queue : 1대1 메시징
        // /topic : 1대다 메시징
        config.enableSimpleBroker("/queue", "/topic");
        
        // 특정 사용자에게 메시지 전송시 사용할 prefix
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 엔드포인트 등록
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:5173", "http://127.0.0.1:*", "http://localhost:3000")  // CORS 허용
                .withSockJS();  // SockJS 지원 (WebSocket을 지원하지 않는 브라우저 대응)
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // STOMP CONNECT 프레임의 userId 헤더를 Principal로 설정하여 /user 큐 라우팅 가능하도록 처리
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String userId = accessor.getFirstNativeHeader("userId");
                    if (userId != null) {
                        java.security.Principal principal = new java.security.Principal() {
                            @Override
                            public String getName() {
                                return userId;
                            }
                        };
                        accessor.setUser(principal);
                    }
                }
                return message;
            }
        });
    }
} 