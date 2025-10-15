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
    private Object data;

    public enum MessageType {
        CALL_REQUEST,
        CALL_ACCEPT,
        CALL_REJECT,
        CALL_END,
        CONSULTATION_START,

        OFFER,
        ANSWER,
        ICE_CANDIDATE,

        CONSULTATION_STEP_SYNC,
        CONSULTATION_NOTE_SYNC,

        USER_JOINED,
        USER_LEFT,

        ERROR
    }
} 