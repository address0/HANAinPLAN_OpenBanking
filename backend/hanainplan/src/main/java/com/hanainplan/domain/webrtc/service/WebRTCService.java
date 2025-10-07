package com.hanainplan.domain.webrtc.service;

import com.hanainplan.domain.user.entity.Consultant;
import com.hanainplan.domain.user.repository.ConsultantRepository;
import com.hanainplan.domain.webrtc.entity.VideoCallRoom;
import com.hanainplan.domain.webrtc.repository.VideoCallRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WebRTCService {

    private final VideoCallRoomRepository videoCallRoomRepository;
    private final ConsultantRepository consultantRepository;
    private final ConsultationMatchingService consultationMatchingService;
    
    // 온라인 사용자 관리 (실제 환경에서는 Redis 등을 사용할 수 있습니다)
    private final Map<Long, Boolean> onlineUsers = new ConcurrentHashMap<>();
    
    // 세션 ID와 사용자 ID 매핑 관리
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();

    /**
     * 통화 요청 생성
     */
    public String createCallRequest(Long callerId, Long calleeId) {
        // 상대방이 온라인 상태인지 확인
        if (!isUserOnline(calleeId)) {
            throw new IllegalStateException("상대방이 현재 오프라인 상태입니다.");
        }

        // 이미 진행 중인 통화가 있는지 확인
        Optional<VideoCallRoom> existingCall = videoCallRoomRepository.findActiveCallByUserId(callerId);
        if (existingCall.isPresent()) {
            throw new IllegalStateException("이미 진행 중인 통화가 있습니다.");
        }

        existingCall = videoCallRoomRepository.findActiveCallByUserId(calleeId);
        if (existingCall.isPresent()) {
            throw new IllegalStateException("상대방이 다른 통화 중입니다.");
        }

        // 새 통화방 생성
        String roomId = UUID.randomUUID().toString();
        VideoCallRoom callRoom = VideoCallRoom.builder()
                .roomId(roomId)
                .callerId(callerId)
                .calleeId(calleeId)
                .status(VideoCallRoom.CallStatus.WAITING)
                .build();

        videoCallRoomRepository.save(callRoom);
        log.info("Created call request: {} -> {}, roomId: {}", callerId, calleeId, roomId);
        
        return roomId;
    }

    /**
     * 통화 수락
     */
    public void acceptCall(String roomId) {
        VideoCallRoom callRoom = videoCallRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("통화방을 찾을 수 없습니다."));
        
        callRoom.acceptCall();
        videoCallRoomRepository.save(callRoom);
        log.info("Call accepted for roomId: {}", roomId);
    }

    /**
     * 통화 거절
     */
    public void rejectCall(String roomId) {
        VideoCallRoom callRoom = videoCallRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("통화방을 찾을 수 없습니다."));
        
        callRoom.rejectCall();
        videoCallRoomRepository.save(callRoom);
        log.info("Call rejected for roomId: {}", roomId);
    }

    /**
     * 통화 종료
     */
    public void endCall(String roomId) {
        VideoCallRoom callRoom = videoCallRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("통화방을 찾을 수 없습니다."));
        
        callRoom.endCall();
        videoCallRoomRepository.save(callRoom);
        log.info("Call ended for roomId: {}", roomId);

        // 상담 종료 후 상담원 상태 업데이트 및 대기 고객 매칭
        consultationMatchingService.onConsultationEnd(roomId);
    }

    /**
     * 사용자 온라인 상태 확인
     */
    public boolean isUserOnline(Long userId) {
        return onlineUsers.getOrDefault(userId, false);
    }

    /**
     * 사용자 온라인 상태 설정 (세션 ID와 함께)
     * - 상담원인 경우, ConsultationStatus도 함께 업데이트
     */
    public void setUserOnline(Long userId, String sessionId, boolean online) {
        if (online) {
            onlineUsers.put(userId, true);
            if (sessionId != null) {
                sessionUserMap.put(sessionId, userId);
            }

            // 상담원인 경우, 상태를 AVAILABLE로 변경하고 대기 중인 고객과 매칭 시도
            Optional<Consultant> consultantOpt = consultantRepository.findById(userId);
            if (consultantOpt.isPresent()) {
                consultationMatchingService.updateConsultantStatus(userId, Consultant.ConsultationStatus.AVAILABLE);
                log.info("Consultant {} is now AVAILABLE and ready for consultation", userId);
            }
        } else {
            onlineUsers.remove(userId);
            if (sessionId != null) {
                sessionUserMap.remove(sessionId);
            }

            // 상담원인 경우, 상태를 OFFLINE로 변경
            Optional<Consultant> consultantOpt = consultantRepository.findById(userId);
            if (consultantOpt.isPresent()) {
                consultationMatchingService.updateConsultantStatus(userId, Consultant.ConsultationStatus.OFFLINE);
                log.info("Consultant {} is now OFFLINE", userId);
            }
        }
        log.debug("User {} is now {} (sessionId: {})", userId, online ? "online" : "offline", sessionId);
    }

    /**
     * 사용자 온라인 상태 설정 (기존 메서드 호환성 유지)
     */
    public void setUserOnline(Long userId, boolean online) {
        setUserOnline(userId, null, online);
    }

    /**
     * 세션 ID로 사용자 오프라인 처리
     */
    public void setUserOfflineBySession(String sessionId) {
        Long userId = sessionUserMap.remove(sessionId);
        if (userId != null) {
            onlineUsers.remove(userId);
            log.debug("User {} set offline by sessionId: {}", userId, sessionId);
        }
    }

    /**
     * 통화방 정보 조회
     */
    public Optional<VideoCallRoom> getCallRoom(String roomId) {
        return videoCallRoomRepository.findByRoomId(roomId);
    }

    /**
     * 현재 온라인 사용자 목록 조회 (디버깅용)
     */
    public Map<Long, Boolean> getOnlineUsers() {
        return new HashMap<>(onlineUsers);
    }

    /**
     * 현재 세션 수 조회 (디버깅용)
     */
    public int getSessionCount() {
        return sessionUserMap.size();
    }
} 