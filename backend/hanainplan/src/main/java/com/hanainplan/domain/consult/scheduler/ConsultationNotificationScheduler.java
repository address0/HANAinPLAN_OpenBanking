package com.hanainplan.domain.consult.scheduler;

import com.hanainplan.domain.consult.entity.Consult;
import com.hanainplan.domain.consult.repository.ConsultRepository;
import com.hanainplan.domain.notification.dto.NotificationDto;
import com.hanainplan.domain.notification.entity.NotificationType;
import com.hanainplan.domain.notification.service.NotificationService;
import com.hanainplan.domain.user.entity.User;
import com.hanainplan.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConsultationNotificationScheduler {

    private final ConsultRepository consultRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void sendConsultationNotifications() {
        try {
            send10MinNotifications();
            sendOntimeNotifications();
        } catch (Exception e) {
            log.error("상담 알림 발송 중 오류 발생", e);
        }
    }

    private void send10MinNotifications() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetTime = now.plusMinutes(10);

        LocalDateTime startRange = targetTime.minusSeconds(30);
        LocalDateTime endRange = targetTime.plusSeconds(30);

        List<Consult> consultations = consultRepository.findConsultationsFor10MinNotification(
                startRange, endRange
        );

        log.info("10분 전 알림 대상 상담: {}건", consultations.size());

        for (Consult consult : consultations) {
            try {
                sendNotificationToCustomer(consult, "곧 상담이 시작됩니다", 
                        "10분 후 상담이 시작됩니다. 준비해 주세요.");

                sendNotificationToConsultant(consult, "곧 상담이 시작됩니다", 
                        "10분 후 상담이 예정되어 있습니다.");

                consult.markNotification10minSent();
                consultRepository.save(consult);

                log.info("10분 전 알림 발송 완료 - consultId: {}", consult.getConsultId());
            } catch (Exception e) {
                log.error("10분 전 알림 발송 실패 - consultId: {}", consult.getConsultId(), e);
            }
        }
    }

    private void sendOntimeNotifications() {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime startRange = now.minusSeconds(30);
        LocalDateTime endRange = now.plusSeconds(30);

        List<Consult> consultations = consultRepository.findConsultationsForOntimeNotification(
                startRange, endRange
        );

        log.info("정각 알림 대상 상담: {}건", consultations.size());

        for (Consult consult : consultations) {
            try {
                sendNotificationToCustomer(consult, "상담 시작 시간", 
                        "상담이 시작되었습니다. 지금 입장하세요.");

                sendNotificationToConsultant(consult, "상담 시작 시간", 
                        "상담이 시작되었습니다. 고객이 입장을 기다리고 있습니다.");

                consult.markNotificationOntimeSent();
                consultRepository.save(consult);

                log.info("정각 알림 발송 완료 - consultId: {}", consult.getConsultId());
            } catch (Exception e) {
                log.error("정각 알림 발송 실패 - consultId: {}", consult.getConsultId(), e);
            }
        }
    }

    private void sendNotificationToCustomer(Consult consult, String title, String message) {
        try {
            Long customerId = Long.valueOf(consult.getCustomerId());

            String consultantName = getConsultantName(consult.getConsultantId());
            String consultationType = convertConsultationTypeToKorean(consult.getConsultType());
            String reservationTime = consult.getReservationDatetime()
                    .format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm"));

            String content = String.format("%s\n\n상담 유형: %s\n담당 상담사: %s\n상담 일시: %s\n상담 번호: %s",
                    message,
                    consultationType,
                    consultantName,
                    reservationTime,
                    consult.getConsultId());

            NotificationDto.CreateRequest notification = NotificationDto.CreateRequest.builder()
                    .userId(customerId)
                    .title(title)
                    .content(content)
                    .type(NotificationType.CONSULTATION)
                    .build();

            notificationService.createNotification(notification);
            log.info("고객 알림 생성 완료 - customerId: {}, consultId: {}", customerId, consult.getConsultId());
        } catch (Exception e) {
            log.error("고객 알림 생성 실패 - consultId: {}", consult.getConsultId(), e);
            throw e;
        }
    }

    private void sendNotificationToConsultant(Consult consult, String title, String message) {
        try {
            Long consultantId = Long.valueOf(consult.getConsultantId());

            String customerName = getCustomerName(consult.getCustomerId());
            String consultationType = convertConsultationTypeToKorean(consult.getConsultType());
            String reservationTime = consult.getReservationDatetime()
                    .format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm"));

            String content = String.format("%s\n\n고객명: %s\n상담 유형: %s\n상담 일시: %s\n상담 번호: %s",
                    message,
                    customerName,
                    consultationType,
                    reservationTime,
                    consult.getConsultId());

            NotificationDto.CreateRequest notification = NotificationDto.CreateRequest.builder()
                    .userId(consultantId)
                    .title(title)
                    .content(content)
                    .type(NotificationType.CONSULTATION)
                    .build();

            notificationService.createNotification(notification);
            log.info("상담사 알림 생성 완료 - consultantId: {}, consultId: {}", consultantId, consult.getConsultId());
        } catch (Exception e) {
            log.error("상담사 알림 생성 실패 - consultId: {}", consult.getConsultId(), e);
            throw e;
        }
    }

    private String getCustomerName(String customerIdStr) {
        try {
            Long customerId = Long.valueOf(customerIdStr);
            return userRepository.findById(customerId)
                    .map(User::getUserName)
                    .orElse("알 수 없는 고객");
        } catch (Exception e) {
            return "알 수 없는 고객";
        }
    }

    private String getConsultantName(String consultantIdStr) {
        try {
            Long consultantId = Long.valueOf(consultantIdStr);
            return userRepository.findById(consultantId)
                    .map(User::getUserName)
                    .orElse("알 수 없는 상담사");
        } catch (Exception e) {
            return "알 수 없는 상담사";
        }
    }

    private String convertConsultationTypeToKorean(String consultationType) {
        switch (consultationType) {
            case "general": return "일반";
            case "product": return "상품가입";
            case "asset-management": return "자산관리";
            default: return consultationType;
        }
    }
}