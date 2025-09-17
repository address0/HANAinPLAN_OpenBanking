package com.hanainplan.domain.webrtc.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallRequestMessage {
    private String roomId; // 응답/알림에서 사용
    private Long callerId;
    private Long calleeId;
    // 프론트가 표시용으로 사용하는 이름 필드 추가
    private String callerName;
    private String calleeName;
    // 선택 메시지
    private String message;
}