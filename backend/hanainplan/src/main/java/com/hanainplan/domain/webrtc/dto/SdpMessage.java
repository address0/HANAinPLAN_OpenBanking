package com.hanainplan.domain.webrtc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SdpMessage {
    private String type;
    private String sdp;
    private String roomId;
    private Long senderId;
    private Long receiverId;
}