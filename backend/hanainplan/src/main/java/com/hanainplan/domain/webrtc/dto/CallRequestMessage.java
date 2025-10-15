package com.hanainplan.domain.webrtc.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallRequestMessage {
    private String roomId;
    private Long callerId;
    private Long calleeId;
    private String callerName;
    private String calleeName;
    private String consultationType;
    private String message;
}