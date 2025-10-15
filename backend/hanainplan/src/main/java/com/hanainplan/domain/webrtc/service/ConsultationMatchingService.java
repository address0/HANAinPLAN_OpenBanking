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

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConsultationMatchingService {

    private final ConsultantRepository consultantRepository;
    private final VideoCallRoomRepository videoCallRoomRepository;

    private final Queue<CustomerRequest> customerQueue = new ConcurrentLinkedQueue<>();

    private final Set<Long> matchingCustomers = ConcurrentHashMap.newKeySet();

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

    public MatchingResult requestConsultation(Long customerId, String customerName, String consultationType) {
        if (matchingCustomers.contains(customerId)) {
            return new MatchingResult(false, null, "이미 상담 대기 중입니다.", null);
        }

        Optional<VideoCallRoom> existingCall = videoCallRoomRepository.findActiveCallByUserId(customerId);
        if (existingCall.isPresent()) {
            return new MatchingResult(false, null, "이미 진행 중인 상담이 있습니다.", null);
        }

        List<Consultant> availableConsultants = consultantRepository.findAvailableConsultants();

        if (availableConsultants.isEmpty()) {
            CustomerRequest request = new CustomerRequest(customerId, customerName, consultationType);
            customerQueue.offer(request);
            matchingCustomers.add(customerId);

            log.info("No available consultant. Customer {} added to queue. Queue size: {}", 
                    customerId, customerQueue.size());

            return new MatchingResult(false, null, 
                    "현재 대기 가능한 상담원이 없습니다. 대기열에 추가되었습니다. (대기 순번: " + customerQueue.size() + ")", 
                    null);
        }

        Consultant selectedConsultant = availableConsultants.get(0);

        String roomId = UUID.randomUUID().toString();
        VideoCallRoom callRoom = VideoCallRoom.builder()
                .roomId(roomId)
                .callerId(customerId)
                .calleeId(selectedConsultant.getConsultantId())
                .status(VideoCallRoom.CallStatus.WAITING)
                .build();

        videoCallRoomRepository.save(callRoom);

        selectedConsultant.updateConsultationStatus(Consultant.ConsultationStatus.BUSY);
        consultantRepository.save(selectedConsultant);

        matchingCustomers.add(customerId);

        log.info("Customer {} matched with consultant {}. Room ID: {}", 
                customerId, selectedConsultant.getConsultantId(), roomId);

        return new MatchingResult(true, selectedConsultant, "상담원과 연결되었습니다.", roomId);
    }

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

        CustomerRequest request = customerQueue.poll();
        if (request == null) {
            log.debug("No waiting customers in queue");
            return;
        }

        try {
            String roomId = UUID.randomUUID().toString();
            VideoCallRoom callRoom = VideoCallRoom.builder()
                    .roomId(roomId)
                    .callerId(request.getCustomerId())
                    .calleeId(consultant.getConsultantId())
                    .status(VideoCallRoom.CallStatus.WAITING)
                    .build();

            videoCallRoomRepository.save(callRoom);

            consultant.updateConsultationStatus(Consultant.ConsultationStatus.BUSY);
            consultantRepository.save(consultant);

            log.info("Waiting customer {} matched with consultant {}. Room ID: {}", 
                    request.getCustomerId(), consultant.getConsultantId(), roomId);

        } catch (Exception e) {
            log.error("Error matching waiting customer", e);
            customerQueue.offer(request);
        }
    }

    public void onConsultationEnd(String roomId) {
        Optional<VideoCallRoom> callRoomOpt = videoCallRoomRepository.findByRoomId(roomId);

        if (callRoomOpt.isEmpty()) {
            log.warn("Call room not found: {}", roomId);
            return;
        }

        VideoCallRoom callRoom = callRoomOpt.get();
        Long consultantId = null;

        Optional<Consultant> callerConsultant = consultantRepository.findById(callRoom.getCallerId());
        Optional<Consultant> calleeConsultant = consultantRepository.findById(callRoom.getCalleeId());

        if (callerConsultant.isPresent()) {
            consultantId = callerConsultant.get().getConsultantId();
        } else if (calleeConsultant.isPresent()) {
            consultantId = calleeConsultant.get().getConsultantId();
        }

        if (consultantId == null) {
            log.warn("No consultant found for room: {}", roomId);
            matchingCustomers.remove(callRoom.getCallerId());
            matchingCustomers.remove(callRoom.getCalleeId());
            return;
        }

        Optional<Consultant> consultantOpt = consultantRepository.findById(consultantId);
        if (consultantOpt.isPresent()) {
            Consultant consultant = consultantOpt.get();
            consultant.updateConsultationStatus(Consultant.ConsultationStatus.AVAILABLE);
            consultant.incrementConsultationCount();
            consultantRepository.save(consultant);

            log.info("Consultant {} is now available after ending call in room {}", consultantId, roomId);

            matchWaitingCustomers(consultantId);
        }

        matchingCustomers.remove(callRoom.getCallerId());
        matchingCustomers.remove(callRoom.getCalleeId());
    }

    public Map<String, Object> getQueueInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("queueSize", customerQueue.size());
        info.put("availableConsultants", consultantRepository.countAvailableConsultants());
        info.put("matchingCustomers", matchingCustomers.size());
        return info;
    }

    public boolean cancelRequest(Long customerId) {
        boolean removed = customerQueue.removeIf(req -> req.getCustomerId().equals(customerId));
        matchingCustomers.remove(customerId);

        if (removed) {
            log.info("Customer {} request cancelled and removed from queue", customerId);
        }

        return removed;
    }

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

        if (status == Consultant.ConsultationStatus.AVAILABLE) {
            matchWaitingCustomers(consultantId);
        }
    }
}