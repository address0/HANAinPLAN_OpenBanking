package com.hanainplan.domain.webrtc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IceCandidateMessage {
    private String candidate;
    private String sdpMid;
    private Integer sdpMLineIndex;
    private String roomId;
    private Long senderId;
    private Long receiverId;
}