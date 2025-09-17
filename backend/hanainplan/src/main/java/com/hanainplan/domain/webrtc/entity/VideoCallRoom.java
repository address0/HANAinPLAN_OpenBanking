package com.hanainplan.domain.webrtc.entity;

import com.hanainplan.domain.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "video_call_rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class VideoCallRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String roomId;

    @Column(nullable = false)
    private Long callerId;  // 통화를 시작한 사용자 ID

    @Column(nullable = false)
    private Long calleeId;  // 통화를 받는 사용자 ID

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CallStatus status;

    @Column
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;

    @Column(length = 1000)
    private String callerSessionDescription;  // Caller의 SDP

    @Column(length = 1000)
    private String calleeSessionDescription;  // Callee의 SDP

    public enum CallStatus {
        WAITING,    // 통화 대기중
        CONNECTED,  // 통화 연결됨
        ENDED,      // 통화 종료됨
        REJECTED    // 통화 거절됨
    }

    public void updateStatus(CallStatus status) {
        this.status = status;
        if (status == CallStatus.CONNECTED) {
            this.startTime = LocalDateTime.now();
        } else if (status == CallStatus.ENDED || status == CallStatus.REJECTED) {
            this.endTime = LocalDateTime.now();
        }
    }

    public void updateCallerSessionDescription(String sessionDescription) {
        this.callerSessionDescription = sessionDescription;
    }

    public void updateCalleeSessionDescription(String sessionDescription) {
        this.calleeSessionDescription = sessionDescription;
    }

    // WebRTCService에서 필요한 메서드들
    public void acceptCall() {
        updateStatus(CallStatus.CONNECTED);
    }

    public void rejectCall() {
        updateStatus(CallStatus.REJECTED);
    }

    public void endCall() {
        updateStatus(CallStatus.ENDED);
    }
} 