package com.hanainplan.domain.webrtc.service;

import com.hanainplan.domain.consult.entity.Consult;
import com.hanainplan.domain.consult.repository.ConsultRepository;
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
    private final ConsultRepository consultRepository;

    private final Map<Long, Boolean> onlineUsers = new ConcurrentHashMap<>();

    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();

    public String createCallRequest(Long callerId, Long calleeId) {
        if (!isUserOnline(calleeId)) {
            throw new IllegalStateException("상대방이 현재 오프라인 상태입니다.");
        }

        Optional<VideoCallRoom> existingCall = videoCallRoomRepository.findActiveCallByUserId(callerId);
        if (existingCall.isPresent()) {
            throw new IllegalStateException("이미 진행 중인 통화가 있습니다.");
        }

        existingCall = videoCallRoomRepository.findActiveCallByUserId(calleeId);
        if (existingCall.isPresent()) {
            throw new IllegalStateException("상대방이 다른 통화 중입니다.");
        }

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

    public void acceptCall(String roomId) {
        VideoCallRoom callRoom = videoCallRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("통화방을 찾을 수 없습니다."));

        callRoom.acceptCall();
        videoCallRoomRepository.save(callRoom);
        log.info("Call accepted for roomId: {}", roomId);
    }

    public void rejectCall(String roomId) {
        VideoCallRoom callRoom = videoCallRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("통화방을 찾을 수 없습니다."));

        callRoom.rejectCall();
        videoCallRoomRepository.save(callRoom);
        log.info("Call rejected for roomId: {}", roomId);
    }

    public void endCall(String roomId) {
        VideoCallRoom callRoom = videoCallRoomRepository.findByRoomId(roomId)
                .orElseThrow(() -> new IllegalArgumentException("통화방을 찾을 수 없습니다."));

        callRoom.endCall();
        videoCallRoomRepository.save(callRoom);
        log.info("Call ended for roomId: {}", roomId);

        try {
            Optional<Consult> consultOpt = consultRepository.findById(roomId);
            if (consultOpt.isPresent()) {
                Consult consult = consultOpt.get();
                if ("상담중".equals(consult.getConsultStatus())) {
                    consult.setConsultStatus("상담완료");
                    consultRepository.save(consult);
                    log.info("Consultation completed - consultId: {}", roomId);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to update consultation status for roomId: {}", roomId, e);
        }

        consultationMatchingService.onConsultationEnd(roomId);
    }

    public boolean isUserOnline(Long userId) {
        return onlineUsers.getOrDefault(userId, false);
    }

    public void setUserOnline(Long userId, String sessionId, boolean online) {
        if (online) {
            onlineUsers.put(userId, true);
            if (sessionId != null) {
                sessionUserMap.put(sessionId, userId);
            }

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

            Optional<Consultant> consultantOpt = consultantRepository.findById(userId);
            if (consultantOpt.isPresent()) {
                consultationMatchingService.updateConsultantStatus(userId, Consultant.ConsultationStatus.OFFLINE);
                log.info("Consultant {} is now OFFLINE", userId);
            }
        }
        log.debug("User {} is now {} (sessionId: {})", userId, online ? "online" : "offline", sessionId);
    }

    public void setUserOnline(Long userId, boolean online) {
        setUserOnline(userId, null, online);
    }

    public void setUserOfflineBySession(String sessionId) {
        Long userId = sessionUserMap.remove(sessionId);
        if (userId != null) {
            onlineUsers.remove(userId);
            log.debug("User {} set offline by sessionId: {}", userId, sessionId);
        }
    }

    public Optional<VideoCallRoom> getCallRoom(String roomId) {
        return videoCallRoomRepository.findByRoomId(roomId);
    }

    public Map<Long, Boolean> getOnlineUsers() {
        return new HashMap<>(onlineUsers);
    }

    public int getSessionCount() {
        return sessionUserMap.size();
    }
} 