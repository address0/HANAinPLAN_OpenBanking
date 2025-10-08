package com.hanainplan.domain.webrtc.service;

import com.hanainplan.domain.user.entity.Consultant;
import com.hanainplan.domain.user.repository.ConsultantRepository;
import com.hanainplan.domain.webrtc.entity.VideoCallRoom;
import com.hanainplan.domain.webrtc.repository.VideoCallRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 상담 대기열 및 매칭 서비스
 * - 고객의 상담 요청을 대기열에 추가
 * - 대기 중인 상담원과 자동 매칭
 * - 매칭 알고리즘: 평점 높은 순, 상담 건수 적은 순
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConsultationMatchingService {

    private final ConsultantRepository consultantRepository;
    private final VideoCallRoomRepository videoCallRoomRepository;

    // 고객 대기열 (customerId, 요청 시간)
    private final Queue<CustomerRequest> customerQueue = new ConcurrentLinkedQueue<>();

    // 매칭 중인 고객 목록 (중복 요청 방지)
    private final Set<Long> matchingCustomers = ConcurrentHashMap.newKeySet();

    /**
     * 고객 상담 요청 정보
     */
    public static class CustomerRequest {
        private final Long customerId;
        private final String customerName;
        private final String consultationType;
        private final Date requestTime;

        public CustomerRequest(Long customerId, String customerName, String consultationType) {
            this.customerId = customerId;
            this.customerName = customerName;
            this.consultationType = consultationType;
            this.requestTime = new Date();
        }

        public Long getCustomerId() { return customerId; }
        public String getCustomerName() { return customerName; }
        public String getConsultationType() { return consultationType; }
        public Date getRequestTime() { return requestTime; }
    }

    /**
     * 매칭 결과
     */
    public static class MatchingResult {
        private final boolean success;
        private final Consultant consultant;
        private final String message;
        private final String roomId;

        public MatchingResult(boolean success, Consultant consultant, String message, String roomId) {
            this.success = success;
            this.consultant = consultant;
            this.message = message;
            this.roomId = roomId;
        }

        public boolean isSuccess() { return success; }
        public Consultant getConsultant() { return consultant; }
        public String getMessage() { return message; }
        public String getRoomId() { return roomId; }
    }

    /**
     * 고객 상담 요청 및 자동 매칭
     */
    public MatchingResult requestConsultation(Long customerId, String customerName, String consultationType) {
        // 이미 매칭 중이거나 대기 중인 고객인지 확인
        if (matchingCustomers.contains(customerId)) {
            return new MatchingResult(false, null, "이미 상담 대기 중입니다.", null);
        }

        // 이미 진행 중인 상담이 있는지 확인
        Optional<VideoCallRoom> existingCall = videoCallRoomRepository.findActiveCallByUserId(customerId);
        if (existingCall.isPresent()) {
            return new MatchingResult(false, null, "이미 진행 중인 상담이 있습니다.", null);
        }

        // 대기 중인 상담원 찾기
        List<Consultant> availableConsultants = consultantRepository.findAvailableConsultants();
        
        if (availableConsultants.isEmpty()) {
            // 대기열에 추가
            CustomerRequest request = new CustomerRequest(customerId, customerName, consultationType);
            customerQueue.offer(request);
            matchingCustomers.add(customerId);
            
            log.info("No available consultant. Customer {} added to queue. Queue size: {}", 
                    customerId, customerQueue.size());
            
            return new MatchingResult(false, null, 
                    "현재 대기 가능한 상담원이 없습니다. 대기열에 추가되었습니다. (대기 순번: " + customerQueue.size() + ")", 
                    null);
        }

        // 가장 적합한 상담원 선택 (평점 높은 순으로 이미 정렬됨)
        Consultant selectedConsultant = availableConsultants.get(0);

        // 상담방 생성
        String roomId = UUID.randomUUID().toString();
        VideoCallRoom callRoom = VideoCallRoom.builder()
                .roomId(roomId)
                .callerId(customerId)
                .calleeId(selectedConsultant.getConsultantId())
                .status(VideoCallRoom.CallStatus.WAITING)
                .build();

        videoCallRoomRepository.save(callRoom);

        // 상담원 상태를 BUSY로 변경
        selectedConsultant.updateConsultationStatus(Consultant.ConsultationStatus.BUSY);
        consultantRepository.save(selectedConsultant);

        matchingCustomers.add(customerId);

        log.info("Customer {} matched with consultant {}. Room ID: {}", 
                customerId, selectedConsultant.getConsultantId(), roomId);

        return new MatchingResult(true, selectedConsultant, "상담원과 연결되었습니다.", roomId);
    }

    /**
     * 상담원이 온라인 상태가 되었을 때, 대기 중인 고객과 자동 매칭
     */
    public void matchWaitingCustomers(Long consultantId) {
        Optional<Consultant> consultantOpt = consultantRepository.findById(consultantId);
        
        if (consultantOpt.isEmpty()) {
            log.warn("Consultant not found: {}", consultantId);
            return;
        }

        Consultant consultant = consultantOpt.get();
        
        if (!consultant.isAvailableForConsultation()) {
            log.debug("Consultant {} is not available for consultation", consultantId);
            return;
        }

        // 대기열에서 첫 번째 고객 가져오기
        CustomerRequest request = customerQueue.poll();
        if (request == null) {
            log.debug("No waiting customers in queue");
            return;
        }

        try {
            // 상담방 생성
            String roomId = UUID.randomUUID().toString();
            VideoCallRoom callRoom = VideoCallRoom.builder()
                    .roomId(roomId)
                    .callerId(request.getCustomerId())
                    .calleeId(consultant.getConsultantId())
                    .status(VideoCallRoom.CallStatus.WAITING)
                    .build();

            videoCallRoomRepository.save(callRoom);

            // 상담원 상태를 BUSY로 변경
            consultant.updateConsultationStatus(Consultant.ConsultationStatus.BUSY);
            consultantRepository.save(consultant);

            log.info("Waiting customer {} matched with consultant {}. Room ID: {}", 
                    request.getCustomerId(), consultant.getConsultantId(), roomId);

            // TODO: WebSocket으로 고객과 상담원에게 알림 전송
            // 여기에서 WebRTCSignalController의 notifyCallRequest를 호출해야 함

        } catch (Exception e) {
            log.error("Error matching waiting customer", e);
            // 매칭 실패 시 고객을 다시 대기열에 추가
            customerQueue.offer(request);
        }
    }

    /**
     * 상담 종료 후 상담원 상태 업데이트 및 대기 고객 매칭
     */
    public void onConsultationEnd(String roomId) {
        Optional<VideoCallRoom> callRoomOpt = videoCallRoomRepository.findByRoomId(roomId);
        
        if (callRoomOpt.isEmpty()) {
            log.warn("Call room not found: {}", roomId);
            return;
        }

        VideoCallRoom callRoom = callRoomOpt.get();
        Long consultantId = null;

        // 상담원 ID 찾기 (callerId 또는 calleeId 중 상담원)
        Optional<Consultant> callerConsultant = consultantRepository.findById(callRoom.getCallerId());
        Optional<Consultant> calleeConsultant = consultantRepository.findById(callRoom.getCalleeId());

        if (callerConsultant.isPresent()) {
            consultantId = callerConsultant.get().getConsultantId();
        } else if (calleeConsultant.isPresent()) {
            consultantId = calleeConsultant.get().getConsultantId();
        }

        if (consultantId == null) {
            log.warn("No consultant found for room: {}", roomId);
            // 매칭 고객 목록에서 제거
            matchingCustomers.remove(callRoom.getCallerId());
            matchingCustomers.remove(callRoom.getCalleeId());
            return;
        }

        // 상담원 상태를 다시 AVAILABLE로 변경
        Optional<Consultant> consultantOpt = consultantRepository.findById(consultantId);
        if (consultantOpt.isPresent()) {
            Consultant consultant = consultantOpt.get();
            consultant.updateConsultationStatus(Consultant.ConsultationStatus.AVAILABLE);
            consultant.incrementConsultationCount();
            consultantRepository.save(consultant);

            log.info("Consultant {} is now available after ending call in room {}", consultantId, roomId);

            // 대기 중인 고객이 있으면 자동 매칭
            matchWaitingCustomers(consultantId);
        }

        // 매칭 고객 목록에서 제거
        matchingCustomers.remove(callRoom.getCallerId());
        matchingCustomers.remove(callRoom.getCalleeId());
    }

    /**
     * 대기열 정보 조회
     */
    public Map<String, Object> getQueueInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("queueSize", customerQueue.size());
        info.put("availableConsultants", consultantRepository.countAvailableConsultants());
        info.put("matchingCustomers", matchingCustomers.size());
        return info;
    }

    /**
     * 고객 대기 취소
     */
    public boolean cancelRequest(Long customerId) {
        boolean removed = customerQueue.removeIf(req -> req.getCustomerId().equals(customerId));
        matchingCustomers.remove(customerId);
        
        if (removed) {
            log.info("Customer {} request cancelled and removed from queue", customerId);
        }
        
        return removed;
    }

    /**
     * 상담원 상태 변경
     */
    public void updateConsultantStatus(Long consultantId, Consultant.ConsultationStatus status) {
        Optional<Consultant> consultantOpt = consultantRepository.findById(consultantId);
        
        if (consultantOpt.isEmpty()) {
            log.warn("Consultant not found: {}", consultantId);
            return;
        }

        Consultant consultant = consultantOpt.get();
        consultant.updateConsultationStatus(status);
        consultantRepository.save(consultant);

        log.info("Consultant {} status updated to {}", consultantId, status);

        // AVAILABLE 상태가 되면 대기 중인 고객과 매칭 시도
        if (status == Consultant.ConsultationStatus.AVAILABLE) {
            matchWaitingCustomers(consultantId);
        }
    }
}