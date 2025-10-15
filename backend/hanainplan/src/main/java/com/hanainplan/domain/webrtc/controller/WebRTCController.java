package com.hanainplan.domain.webrtc.controller;

import com.hanainplan.domain.consult.entity.Consult;
import com.hanainplan.domain.consult.repository.ConsultRepository;
import com.hanainplan.domain.webrtc.dto.CallRequestMessage;
import com.hanainplan.domain.webrtc.controller.WebRTCSignalController;
import com.hanainplan.domain.webrtc.entity.VideoCallRoom;
import com.hanainplan.domain.webrtc.repository.VideoCallRoomRepository;
import com.hanainplan.domain.webrtc.service.ConsultationMatchingService;
import com.hanainplan.domain.webrtc.service.WebRTCService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
    private final ConsultationMatchingService consultationMatchingService;
    private final ConsultRepository consultRepository;

    @PostMapping("/call/request")
    public ResponseEntity<?> createCallRequest(@RequestBody CallRequestMessage request) {
        try {
            String roomId = webRTCService.createCallRequest(request.getCallerId(), request.getCalleeId());
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

    @PostMapping("/consultation/request")
    public ResponseEntity<?> requestConsultation(@RequestBody Map<String, Object> request) {
        try {
            Long customerId = Long.valueOf(request.get("callerId").toString());
            String customerName = request.get("callerName").toString();
            String consultationType = request.getOrDefault("consultationType", "일반상담").toString();

            ConsultationMatchingService.MatchingResult result = 
                consultationMatchingService.requestConsultation(customerId, customerName, consultationType);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());

            if (result.isSuccess()) {
                response.put("roomId", result.getRoomId());
                response.put("consultantId", result.getConsultant().getConsultantId());
                response.put("consultantName", result.getConsultant().getEmployeeId());

                Long consultantId = result.getConsultant().getConsultantId();

                CallRequestMessage callRequest = new CallRequestMessage();
                callRequest.setCallerId(customerId);
                callRequest.setCalleeId(consultantId);
                callRequest.setCallerName(customerName);
                callRequest.setCalleeName("상담원");
                callRequest.setConsultationType(consultationType);
                callRequest.setRoomId(result.getRoomId());

                signalController.notifyCallRequest(callRequest, result.getRoomId());

                log.info("Customer {} matched with consultant {} for consultation", 
                        customerId, consultantId);
            } else {
                log.info("Customer {} added to waiting queue", customerId);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error requesting consultation", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "상담 요청 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/consultation/cancel")
    public ResponseEntity<?> cancelConsultationRequest(@RequestBody Map<String, Object> request) {
        try {
            Long customerId = Long.valueOf(request.get("customerId").toString());
            boolean cancelled = consultationMatchingService.cancelRequest(customerId);

            return ResponseEntity.ok(Map.of(
                "success", cancelled,
                "message", cancelled ? "상담 요청이 취소되었습니다." : "취소할 상담 요청이 없습니다."
            ));
        } catch (Exception e) {
            log.error("Error cancelling consultation request", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "상담 요청 취소 중 오류가 발생했습니다."
            ));
        }
    }

    @GetMapping("/consultation/queue")
    public ResponseEntity<?> getQueueInfo() {
        return ResponseEntity.ok(consultationMatchingService.getQueueInfo());
    }

    @GetMapping("/user/{userId}/status")
    public ResponseEntity<?> checkUserStatus(@PathVariable Long userId) {
        boolean isOnline = webRTCService.isUserOnline(userId);
        return ResponseEntity.ok(Map.of(
            "userId", userId,
            "isOnline", isOnline
        ));
    }

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

    @GetMapping("/ice-servers")
    public ResponseEntity<?> getIceServers() {
        return ResponseEntity.ok(Map.of(
            "iceServers", List.of(
                Map.of("urls", "stun:stun.l.google.com:19302"),
                Map.of("urls", "stun:stun1.l.google.com:19302"),
                Map.of("urls", "stun:stun2.l.google.com:19302")
            )
        ));
    }

    @GetMapping("/online-users")
    public ResponseEntity<?> getOnlineUsers() {
        return ResponseEntity.ok(Map.of(
            "onlineUsers", webRTCService.getOnlineUsers(),
            "sessionCount", webRTCService.getSessionCount()
        ));
    }

    @PostMapping("/consultation/{consultationId}/join")
    public ResponseEntity<?> joinConsultationRoom(
            @PathVariable String consultationId,
            @RequestBody Map<String, Object> request
    ) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            log.info("POST /api/webrtc/consultation/{}/join - userId: {}", consultationId, userId);

            Consult consult = consultRepository.findById(consultationId)
                    .orElseThrow(() -> new IllegalArgumentException("상담을 찾을 수 없습니다. ID: " + consultationId));

            if (!"예약확정".equals(consult.getConsultStatus()) && !"상담중".equals(consult.getConsultStatus())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "예약 확정되거나 진행 중인 상담만 입장할 수 있습니다. 현재 상태: " + consult.getConsultStatus()
                ));
            }

            String customerIdStr = consult.getCustomerId();
            String consultantIdStr = consult.getConsultantId();

            if (!userId.equals(Long.valueOf(customerIdStr)) && !userId.equals(Long.valueOf(consultantIdStr))) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "해당 상담의 참여자가 아닙니다."
                ));
            }

            VideoCallRoom callRoom = videoCallRoomRepository.findByRoomId(consultationId)
                    .orElseGet(() -> {
                        VideoCallRoom newRoom = VideoCallRoom.builder()
                                .roomId(consultationId)
                                .callerId(Long.valueOf(customerIdStr))
                                .calleeId(Long.valueOf(consultantIdStr))
                                .status(VideoCallRoom.CallStatus.WAITING)
                                .build();
                        return videoCallRoomRepository.save(newRoom);
                    });

            if ("예약확정".equals(consult.getConsultStatus())) {
                consult.startConsult();
                consultRepository.save(consult);
                log.info("상담 시작 - consultId: {}", consultationId);
            }

            if (callRoom.getStatus() == VideoCallRoom.CallStatus.WAITING) {
                callRoom.updateStatus(VideoCallRoom.CallStatus.CONNECTED);
                videoCallRoomRepository.save(callRoom);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "roomId", consultationId,
                "consultationId", consultationId,
                "callerId", callRoom.getCallerId(),
                "calleeId", callRoom.getCalleeId(),
                "status", callRoom.getStatus(),
                "message", "상담 방에 입장했습니다."
            ));

        } catch (NumberFormatException e) {
            log.error("Invalid user ID format", e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "잘못된 사용자 ID 형식입니다."
            ));
        } catch (IllegalArgumentException e) {
            log.error("Consultation not found: {}", consultationId);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error joining consultation room: {}", consultationId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "상담 방 입장 중 오류가 발생했습니다."
            ));
        }
    }
} 