package com.hanainplan.domain.webrtc.controller;

import com.hanainplan.domain.webrtc.dto.CallRequestMessage;
import com.hanainplan.domain.webrtc.controller.WebRTCSignalController;
import com.hanainplan.domain.webrtc.entity.VideoCallRoom;
import com.hanainplan.domain.webrtc.repository.VideoCallRoomRepository;
import com.hanainplan.domain.webrtc.service.WebRTCService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/webrtc")
@RequiredArgsConstructor
@Slf4j
public class WebRTCController {

    private final WebRTCService webRTCService;
    private final VideoCallRoomRepository videoCallRoomRepository;
    private final WebRTCSignalController signalController;

    /**
     * 통화 요청 생성 (REST API)
     */
    @PostMapping("/call/request")
    public ResponseEntity<?> createCallRequest(@RequestBody CallRequestMessage request) {
        try {
            String roomId = webRTCService.createCallRequest(request.getCallerId(), request.getCalleeId());
            // 수신자에게 STOMP로 알림 전송
            request.setRoomId(roomId);
            signalController.notifyCallRequest(request, roomId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "roomId", roomId,
                "message", "통화 요청이 전송되었습니다."
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error creating call request", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "통화 요청 생성 중 오류가 발생했습니다."
            ));
        }
    }

    /**
     * 사용자 온라인 상태 확인
     */
    @GetMapping("/user/{userId}/status")
    public ResponseEntity<?> checkUserStatus(@PathVariable Long userId) {
        boolean isOnline = webRTCService.isUserOnline(userId);
        return ResponseEntity.ok(Map.of(
            "userId", userId,
            "isOnline", isOnline
        ));
    }

    /**
     * 사용자 온라인 상태 등록/해제
     */
    @PostMapping("/user/online")
    public ResponseEntity<?> setUserOnlineStatus(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            Boolean isOnline = Boolean.valueOf(request.get("isOnline").toString());
            
            webRTCService.setUserOnline(userId, isOnline);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "userId", userId,
                "isOnline", isOnline,
                "message", isOnline ? "사용자가 온라인 상태로 등록되었습니다." : "사용자가 오프라인 상태로 등록되었습니다."
            ));
        } catch (Exception e) {
            log.error("Error setting user online status", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "사용자 상태 등록에 실패했습니다."
            ));
        }
    }

    /**
     * 사용자의 활성 통화 조회
     */
    @GetMapping("/user/{userId}/active-call")
    public ResponseEntity<?> getActiveCall(@PathVariable Long userId) {
        try {
            var activeCall = videoCallRoomRepository.findActiveCallByUserId(userId);
            if (activeCall.isPresent()) {
                VideoCallRoom call = activeCall.get();
                return ResponseEntity.ok(Map.of(
                    "hasActiveCall", true,
                    "roomId", call.getRoomId(),
                    "status", call.getStatus(),
                    "callerId", call.getCallerId(),
                    "calleeId", call.getCalleeId(),
                    "createdAt", call.getCreatedAt()
                ));
            } else {
                return ResponseEntity.ok(Map.of("hasActiveCall", false));
            }
        } catch (Exception e) {
            log.error("Error getting active call for user {}", userId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "활성 통화 조회 중 오류가 발생했습니다."
            ));
        }
    }

    /**
     * 통화방 정보 조회
     */
    @GetMapping("/room/{roomId}")
    public ResponseEntity<?> getRoomInfo(@PathVariable String roomId) {
        try {
            var callRoom = videoCallRoomRepository.findByRoomId(roomId);
            if (callRoom.isPresent()) {
                VideoCallRoom room = callRoom.get();
                return ResponseEntity.ok(Map.of(
                    "roomId", room.getRoomId(),
                    "status", room.getStatus(),
                    "callerId", room.getCallerId(),
                    "calleeId", room.getCalleeId(),
                    "startTime", room.getStartTime(),
                    "endTime", room.getEndTime(),
                    "createdAt", room.getCreatedAt()
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting room info for roomId {}", roomId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "통화방 정보 조회 중 오류가 발생했습니다."
            ));
        }
    }

    /**
     * 사용자의 통화 히스토리 조회
     */
    @GetMapping("/user/{userId}/history")
    public ResponseEntity<?> getCallHistory(@PathVariable Long userId) {
        try {
            List<VideoCallRoom> callHistory = videoCallRoomRepository.findByCallerIdAndCalleeId(userId, userId);
            return ResponseEntity.ok(Map.of(
                "userId", userId,
                "callHistory", callHistory
            ));
        } catch (Exception e) {
            log.error("Error getting call history for user {}", userId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "통화 히스토리 조회 중 오류가 발생했습니다."
            ));
        }
    }

    /**
     * STUN/TURN 서버 설정 정보 제공
     */
    @GetMapping("/ice-servers")
    public ResponseEntity<?> getIceServers() {
        // 실제 환경에서는 환경 변수나 설정 파일에서 읽어오는 것이 좋습니다
        return ResponseEntity.ok(Map.of(
            "iceServers", List.of(
                Map.of("urls", "stun:stun.l.google.com:19302"),
                Map.of("urls", "stun:stun1.l.google.com:19302"),
                Map.of("urls", "stun:stun2.l.google.com:19302")
                // TURN 서버가 있다면 여기에 추가
                // Map.of(
                //     "urls", "turn:your-turn-server.com:3478",
                //     "username", "your-username",
                //     "credential", "your-password"
                // )
            )
        ));
    }

    /**
     * 현재 온라인 사용자 목록 조회 (디버깅용)
     */
    @GetMapping("/online-users")
    public ResponseEntity<?> getOnlineUsers() {
        return ResponseEntity.ok(Map.of(
            "onlineUsers", webRTCService.getOnlineUsers(),
            "sessionCount", webRTCService.getSessionCount()
        ));
    }
} 