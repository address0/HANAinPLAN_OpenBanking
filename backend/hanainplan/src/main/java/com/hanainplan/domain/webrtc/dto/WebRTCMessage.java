package com.hanainplan.domain.webrtc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebRTCMessage {
    
    private MessageType type;
    private String roomId;
    private Long senderId;
    private Long receiverId;
    private Object data;  // SDP, ICE candidate, 또는 기타 데이터
    
    public enum MessageType {
        // Call management
        CALL_REQUEST,     // 통화 요청
        CALL_ACCEPT,      // 통화 수락
        CALL_REJECT,      // 통화 거절
        CALL_END,         // 통화 종료
        
        // WebRTC signaling
        OFFER,            // SDP offer
        ANSWER,           // SDP answer
        ICE_CANDIDATE,    // ICE candidate
        
        // Connection status
        USER_JOINED,      // 사용자 입장
        USER_LEFT,        // 사용자 퇴장
        
        // Error
        ERROR             // 에러 메시지
    }
} 