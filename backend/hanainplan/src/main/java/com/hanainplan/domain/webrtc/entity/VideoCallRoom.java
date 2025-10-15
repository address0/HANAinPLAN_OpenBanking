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
    private Long callerId;

    @Column(nullable = false)
    private Long calleeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CallStatus status;

    @Column
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;

    @Column(length = 1000)
    private String callerSessionDescription;

    @Column(length = 1000)
    private String calleeSessionDescription;

    public enum CallStatus {
        WAITING,
        CONNECTED,
        ENDED,
        REJECTED
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