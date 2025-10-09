package com.hanainplan.domain.consult.service;

import com.hanainplan.domain.consult.dto.ConsultationRequestDto;
import com.hanainplan.domain.consult.dto.ConsultationResponseDto;
import com.hanainplan.domain.consult.entity.Consult;
import com.hanainplan.domain.consult.repository.ConsultRepository;
import com.hanainplan.domain.notification.dto.NotificationDto;
import com.hanainplan.domain.notification.entity.NotificationType;
import com.hanainplan.domain.notification.service.EmailService;
import com.hanainplan.domain.notification.service.NotificationService;
import com.hanainplan.domain.schedule.service.ScheduleService;
import com.hanainplan.domain.user.entity.User;
import com.hanainplan.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 상담 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ConsultService {

    private final ConsultRepository consultRepository;
    private final UserRepository userRepository;
    private final ScheduleService scheduleService;
    private final EmailService emailService;
    private final NotificationService notificationService;

    /**
     * 상담 신청
     */
    @Transactional
    public ConsultationResponseDto createConsultation(ConsultationRequestDto request) {
        log.info("상담 신청 - customerId: {}, consultantId: {}, type: {}", 
                request.getCustomerId(), request.getConsultantId(), request.getConsultationType());

        // 사용자 정보 조회
        User customer = userRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다."));
        
        User consultant = userRepository.findById(request.getConsultantId())
                .orElseThrow(() -> new IllegalArgumentException("상담사를 찾을 수 없습니다."));

        // 상담 ID 생성 (날짜 + 순번 형식)
        String consultId = generateConsultId();

        // 상담 엔터티 생성
        Consult consult = Consult.builder()
                .consultId(consultId)
                .consultType(request.getConsultationType())
                .reservationDatetime(request.getReservationDatetime())
                .consultStatus("예약신청")
                .detail(request.getDetail())
                .customerId(String.valueOf(request.getCustomerId()))
                .consultantId(String.valueOf(request.getConsultantId()))
                .build();

        Consult savedConsult = consultRepository.save(consult);

        // 상담사의 Schedule에도 자동 추가
        try {
            LocalDateTime endTime = request.getReservationDatetime().plusHours(1);
            scheduleService.createConsultationSchedule(
                    request.getConsultantId(),
                    request.getCustomerId(),
                    customer.getUserName(),
                    request.getReservationDatetime(),
                    endTime
            );
            log.info("상담 일정 자동 생성 완료");
        } catch (Exception e) {
            log.error("상담 일정 자동 생성 실패", e);
            // 일정 생성 실패해도 상담 신청은 계속 진행
        }

        // 상담 신청 시 상담사에게 알림 생성
        try {
            createConsultationNotificationForConsultant(savedConsult, "새로운 상담 신청이 있습니다.");
        } catch (Exception e) {
            log.error("상담 신청 알림 생성 실패 - consultId: {}", consultId, e);
            // 알림 생성 실패해도 상담 신청은 계속 진행
        }

        // 상담 신청 시에는 이메일을 보내지 않음 (상담사가 확정할 때 보냄)
        log.info("상담 예약 신청 완료 - consultId: {}, customerId: {}, consultantId: {}",
                consultId, customer.getUserId(), consultant.getUserId());

        // DTO 변환
        ConsultationResponseDto response = ConsultationResponseDto.fromEntity(savedConsult);
        response.setCustomerName(customer.getUserName());
        response.setConsultantName(consultant.getUserName());

        log.info("상담 신청 완료 - consultId: {}", consultId);
        return response;
    }

    /**
     * 상담 ID 생성
     */
    private String generateConsultId() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "CONS" + datePart;
    }

    /**
     * 고객의 상담 목록 조회
     */
    public List<ConsultationResponseDto> getCustomerConsultations(Long customerId) {
        log.info("고객 상담 목록 조회 - customerId: {}", customerId);
        
        List<Consult> consults = consultRepository.findByCustomerIdOrderByReservationDatetimeDesc(
                String.valueOf(customerId)
        );
        
        return consults.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 상담사의 상담 목록 조회
     */
    public List<ConsultationResponseDto> getConsultantConsultations(Long consultantId) {
        log.info("상담사 상담 목록 조회 - consultantId: {}", consultantId);
        
        List<Consult> consults = consultRepository.findByConsultantIdOrderByReservationDatetimeDesc(
                String.valueOf(consultantId)
        );
        
        return consults.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 상담사의 오늘 상담 조회
     */
    public List<ConsultationResponseDto> getTodayConsultations(Long consultantId) {
        log.info("상담사 오늘 상담 조회 - consultantId: {}", consultantId);
        
        List<Consult> consults = consultRepository.findTodayConsultations(
                String.valueOf(consultantId),
                LocalDateTime.now()
        );
        
        return consults.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 상담 요청 내역 조회 (예약신청 상태)
     */
    public List<ConsultationResponseDto> getConsultationRequests(Long consultantId) {
        log.info("상담 요청 내역 조회 - consultantId: {}", consultantId);
        
        List<Consult> consults = consultRepository.findByConsultantIdAndConsultStatusInOrderByReservationDatetimeAsc(
                String.valueOf(consultantId),
                List.of("예약신청")
        );
        
        return consults.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 상담 상태 변경
     */
    @Transactional
    public ConsultationResponseDto updateConsultationStatus(String consultId, String newStatus) {
        log.info("상담 상태 변경 - consultId: {}, newStatus: {}", consultId, newStatus);
        
        Consult consult = consultRepository.findById(consultId)
                .orElseThrow(() -> new IllegalArgumentException("상담을 찾을 수 없습니다. ID: " + consultId));
        
        consult.setConsultStatus(newStatus);
        
        if ("상담중".equals(newStatus)) {
            consult.setConsultDatetime(LocalDateTime.now());
        }
        
        Consult updatedConsult = consultRepository.save(consult);
        
        // 상담사가 예약을 확정했을 때 고객에게 이메일 전송 및 알림 생성
        if ("예약확정".equals(newStatus)) {
            try {
                // 이메일 전송
                sendConsultationConfirmationEmail(consult);

                // 앱 내 알림 생성
                createConsultationNotificationForCustomer(consult, "상담 예약이 확정되었습니다.");

                log.info("상담 예약 확정 이메일 전송 및 알림 생성 완료 - consultId: {}", consultId);
            } catch (Exception e) {
                log.error("상담 예약 확정 이메일 전송 또는 알림 생성 실패 - consultId: {}", consultId, e);
                // 이메일/알림 생성 실패해도 상태 변경은 계속 진행
            }
        }
        
        return convertToDto(updatedConsult);
    }

    /**
     * Entity -> DTO 변환 (사용자 정보 포함)
     */
    private ConsultationResponseDto convertToDto(Consult consult) {
        ConsultationResponseDto dto = ConsultationResponseDto.fromEntity(consult);
        
        // 고객 이름 조회
        try {
            Long customerId = Long.valueOf(consult.getCustomerId());
            userRepository.findById(customerId).ifPresent(user -> 
                dto.setCustomerName(user.getUserName())
            );
        } catch (NumberFormatException e) {
            log.warn("Invalid customer ID format: {}", consult.getCustomerId());
        }
        
        // 상담사 이름 조회
        try {
            Long consultantId = Long.valueOf(consult.getConsultantId());
            userRepository.findById(consultantId).ifPresent(user -> 
                dto.setConsultantName(user.getUserName())
            );
        } catch (NumberFormatException e) {
            log.warn("Invalid consultant ID format: {}", consult.getConsultantId());
        }
        
        return dto;
    }

    /**
     * 상담 예약 확정 이메일 전송
     */
    private void sendConsultationConfirmationEmail(Consult consult) {
        // 고객 정보 조회
        Long customerId = Long.valueOf(consult.getCustomerId());
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + customerId));
        
        // 상담사 정보 조회
        Long consultantId = Long.valueOf(consult.getConsultantId());
        User consultant = userRepository.findById(consultantId)
                .orElseThrow(() -> new IllegalArgumentException("상담사를 찾을 수 없습니다: " + consultantId));
        
        if (customer.getEmail() == null || customer.getEmail().isEmpty()) {
            log.warn("고객 이메일이 없어서 상담 예약 확정 이메일을 전송할 수 없습니다. customerId: {}, userName: {}", 
                    customer.getUserId(), customer.getUserName());
            return;
        }

        String subject = "[하나인플랜] 상담 예약이 확정되었습니다";
        String htmlContent = buildConsultationConfirmationEmailHtml(
                customer.getUserName(),
                consultant.getUserName(),
                consult.getConsultType(),
                consult.getReservationDatetime(),
                consult.getConsultId()
        );

        boolean emailSent = emailService.sendHtmlEmail(customer.getEmail(), subject, htmlContent);
        if (emailSent) {
            log.info("상담 예약 확정 이메일 전송 성공 - customerId: {}, email: {}", customer.getUserId(), customer.getEmail());
        } else {
            log.error("상담 예약 확정 이메일 전송 실패 - customerId: {}, email: {}", customer.getUserId(), customer.getEmail());
        }
    }

    /**
     * 상담 예약 확정 이메일 HTML 템플릿 생성
     */
    private String buildConsultationConfirmationEmailHtml(
            String customerName,
            String consultantName,
            String consultationType,
            LocalDateTime reservationDatetime,
            String consultId
    ) {
        String formattedDateTime = reservationDatetime.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm"));
        String consultationTypeKorean = convertConsultationTypeToKorean(consultationType);

        return String.format("""
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>상담 예약 확정</title>
                    <style>
                        body {
                            font-family: 'Malgun Gothic', '맑은 고딕', sans-serif;
                            line-height: 1.6;
                            color: #333;
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .header {
                            background: linear-gradient(135deg, #008485 0%%, #00A091 100%%);
                            color: white;
                            padding: 30px;
                            text-align: center;
                            border-radius: 10px 10px 0 0;
                        }
                        .header h1 {
                            margin: 0;
                            font-size: 24px;
                        }
                        .content {
                            background: #f9f9f9;
                            padding: 30px;
                            border: 1px solid #ddd;
                            border-top: none;
                        }
                        .info-box {
                            background: white;
                            padding: 20px;
                            margin: 20px 0;
                            border-left: 4px solid #008485;
                            border-radius: 5px;
                        }
                        .info-row {
                            margin: 10px 0;
                        }
                        .label {
                            font-weight: bold;
                            color: #008485;
                            display: inline-block;
                            width: 120px;
                        }
                        .value {
                            color: #333;
                        }
                        .status-box {
                            background: #e8f5e9;
                            border: 1px solid #4caf50;
                            padding: 15px;
                            border-radius: 5px;
                            margin: 20px 0;
                            text-align: center;
                        }
                        .footer {
                            text-align: center;
                            padding: 20px;
                            color: #666;
                            font-size: 12px;
                            border-top: 1px solid #ddd;
                        }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <h1>✅ 상담 예약이 확정되었습니다</h1>
                    </div>
                    <div class="content">
                        <p><strong>%s</strong>님, 안녕하세요!</p>
                        <p>신청하신 상담 예약이 확정되었습니다.</p>
                        
                        <div class="info-box">
                            <div class="info-row">
                                <span class="label">예약 번호:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">상담 유형:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">담당 상담사:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">상담 일시:</span>
                                <span class="value">%s</span>
                            </div>
                        </div>
                        
                        <div class="status-box">
                            <strong>✅ 예약이 확정되었습니다!</strong><br>
                            <span style="font-size: 14px;">예약된 시간에 상담이 진행됩니다.</span>
                        </div>
                        
                        <p style="color: #666; font-size: 14px;">
                            상담 시간 5분 전까지 준비해 주시기 바랍니다.<br>
                            예약 변경이나 취소가 필요하신 경우 고객센터(1588-1111)로 연락주시기 바랍니다.
                        </p>
                    </div>
                    <div class="footer">
                        <p>본 메일은 발신 전용입니다. 문의사항은 고객센터를 이용해주세요.</p>
                        <p>© 2025 하나인플랜. All rights reserved.</p>
                    </div>
                </body>
                </html>
                """, customerName, consultId, consultationTypeKorean, consultantName, formattedDateTime);
    }

    /**
     * 고객에게 상담 관련 알림 생성
     */
    private void createConsultationNotificationForCustomer(Consult consult, String customMessage) {
        try {
            Long customerId = Long.valueOf(consult.getCustomerId());

            String title = "상담 예약 알림";
            String content = String.format("%s\n상담 일시: %s\n상담 유형: %s",
                customMessage,
                consult.getReservationDatetime().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm")),
                convertConsultationTypeToKorean(consult.getConsultType()));

            NotificationDto.CreateRequest notificationRequest = NotificationDto.CreateRequest.builder()
                    .userId(customerId)
                    .title(title)
                    .content(content)
                    .type(NotificationType.CONSULTATION)
                    .build();

            notificationService.createNotification(notificationRequest);
            log.info("고객 상담 알림 생성 완료 - customerId: {}, consultId: {}", customerId, consult.getConsultId());
        } catch (Exception e) {
            log.error("고객 상담 알림 생성 실패 - consultId: {}", consult.getConsultId(), e);
        }
    }

    /**
     * 상담사에게 상담 관련 알림 생성
     */
    private void createConsultationNotificationForConsultant(Consult consult, String customMessage) {
        try {
            Long consultantId = Long.valueOf(consult.getConsultantId());

            String title = "상담 신청 알림";
            String content = String.format("%s\n고객명: %s\n상담 일시: %s\n상담 유형: %s",
                customMessage,
                getCustomerNameById(consult.getCustomerId()),
                consult.getReservationDatetime().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm")),
                convertConsultationTypeToKorean(consult.getConsultType()));

            NotificationDto.CreateRequest notificationRequest = NotificationDto.CreateRequest.builder()
                    .userId(consultantId)
                    .title(title)
                    .content(content)
                    .type(NotificationType.CONSULTATION)
                    .build();

            notificationService.createNotification(notificationRequest);
            log.info("상담사 상담 알림 생성 완료 - consultantId: {}, consultId: {}", consultantId, consult.getConsultId());
        } catch (Exception e) {
            log.error("상담사 상담 알림 생성 실패 - consultId: {}", consult.getConsultId(), e);
        }
    }

    /**
     * 고객 ID로 고객 이름 조회
     */
    private String getCustomerNameById(String customerIdStr) {
        try {
            Long customerId = Long.valueOf(customerIdStr);
            return userRepository.findById(customerId)
                    .map(User::getUserName)
                    .orElse("알 수 없는 고객");
        } catch (Exception e) {
            return "알 수 없는 고객";
        }
    }

    /**
     * 상담 유형을 한글로 변환
     */
    private String convertConsultationTypeToKorean(String consultationType) {
        switch (consultationType) {
            case "general": return "일반";
            case "product": return "상품가입";
            case "asset-management": return "자산관리";
            default: return consultationType;
        }
    }
}

