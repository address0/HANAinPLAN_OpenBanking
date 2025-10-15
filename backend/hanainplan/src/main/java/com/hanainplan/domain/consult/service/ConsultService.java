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
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

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

    @Transactional
    public ConsultationResponseDto createConsultation(ConsultationRequestDto request) {
        log.info("상담 신청 - customerId: {}, consultantId: {}, type: {}", 
                request.getCustomerId(), request.getConsultantId(), request.getConsultationType());

        User customer = userRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다."));

        User consultant = userRepository.findById(request.getConsultantId())
                .orElseThrow(() -> new IllegalArgumentException("상담사를 찾을 수 없습니다."));

        String consultId = generateConsultId(request.getConsultationType());

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
        }

        try {
            createConsultationNotificationForConsultant(savedConsult, "새로운 상담 신청이 있습니다.");
        } catch (Exception e) {
            log.error("상담 신청 알림 생성 실패 - consultId: {}", consultId, e);
        }

        log.info("상담 예약 신청 완료 - consultId: {}, customerId: {}, consultantId: {}",
                consultId, customer.getUserId(), consultant.getUserId());

        ConsultationResponseDto response = ConsultationResponseDto.fromEntity(savedConsult);
        response.setCustomerName(customer.getUserName());
        response.setConsultantName(consultant.getUserName());

        log.info("상담 신청 완료 - consultId: {}", consultId);
        return response;
    }

    private String generateConsultId(String consultationType) {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String prefix;

        switch (consultationType) {
            case "general":
                prefix = "COM";
                break;
            case "product":
                prefix = "PRO";
                break;
            case "asset-management":
                prefix = "ASM";
                break;
            default:
                prefix = "COM";
                break;
        }

        return prefix + datePart;
    }

    public List<ConsultationResponseDto> getCustomerConsultations(Long customerId) {
        log.info("고객 상담 목록 조회 - customerId: {}", customerId);

        List<Consult> consults = consultRepository.findByCustomerIdOrderByReservationDatetimeDesc(
                String.valueOf(customerId)
        );

        return consults.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<ConsultationResponseDto> getConsultantConsultations(Long consultantId) {
        log.info("상담사 상담 목록 조회 - consultantId: {}", consultantId);

        List<Consult> consults = consultRepository.findByConsultantIdOrderByReservationDatetimeDesc(
                String.valueOf(consultantId)
        );

        return consults.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

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

        if ("예약확정".equals(newStatus)) {
            try {
                sendConsultationConfirmationEmail(consult);

                createConsultationNotificationForCustomer(consult, "상담 예약이 확정되었습니다.");

                log.info("상담 예약 확정 이메일 전송 및 알림 생성 완료 - consultId: {}", consultId);
            } catch (Exception e) {
                log.error("상담 예약 확정 이메일 전송 또는 알림 생성 실패 - consultId: {}", consultId, e);
            }
        }

        return convertToDto(updatedConsult);
    }

    private ConsultationResponseDto convertToDto(Consult consult) {
        ConsultationResponseDto dto = ConsultationResponseDto.fromEntity(consult);

        try {
            Long customerId = Long.valueOf(consult.getCustomerId());
            userRepository.findById(customerId).ifPresent(user -> 
                dto.setCustomerName(user.getUserName())
            );
        } catch (NumberFormatException e) {
            log.warn("Invalid customer ID format: {}", consult.getCustomerId());
        }

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

    private void sendConsultationConfirmationEmail(Consult consult) {
        Long customerId = Long.valueOf(consult.getCustomerId());
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + customerId));

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

    private String buildConsultationConfirmationEmailHtml(
            String customerName,
            String consultantName,
            String consultationType,
            LocalDateTime reservationDatetime,
            String consultId
    ) {
        try {
            String formattedDateTime = reservationDatetime.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm"));
            String consultationTypeKorean = convertConsultationTypeToKorean(consultationType);
            String consultationIcon = getConsultationIcon(consultationType);

            String template = loadEmailTemplate("consultation-confirmation-email.html");

            return template
                    .replace("{title}", "🎉 축하합니다!")
                    .replace("{customerName}", customerName)
                    .replace("{consultId}", consultId)
                    .replace("{consultationIcon}", consultationIcon)
                    .replace("{consultationType}", consultationTypeKorean)
                    .replace("{consultantName}", consultantName)
                    .replace("{reservationDateTime}", formattedDateTime);

        } catch (Exception e) {
            log.error("이메일 템플릿 로드 실패", e);
            return createFallbackEmailHtml(customerName, consultantName, consultationType, reservationDatetime, consultId);
        }
    }

    private String loadEmailTemplate(String templateName) throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/" + templateName);
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String createFallbackEmailHtml(
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
                </head>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; background-color: #f8f9fa; margin: 0; padding: 20px;">
                    <div style="max-width: 600px; margin: 0 auto; background: white; border-radius: 10px; padding: 30px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);">
                        <div style="text-align: center; margin-bottom: 30px;">
                            <h1 style="color: #008485; font-size: 28px; margin-bottom: 10px;">🎉 축하합니다!</h1>
                            <p style="color: #666; font-size: 16px;">상담 예약이 성공적으로 확정되었습니다</p>
                        </div>

                        <div style="margin-bottom: 30px;">
                            <h2 style="color: #2c3e50; font-size: 20px; margin-bottom: 15px;">%s님, 안녕하세요! 👋</h2>
                            <p style="color: #666; font-size: 14px; line-height: 1.7;">
                                신청해주신 상담 예약이 확정되어 안내드립니다.<br>
                                전문 상담사와의 의미 있는 시간이 되시길 바랍니다.
                            </p>
                        </div>

                        <div style="background: #f8f9fa; padding: 20px; border-radius: 8px; margin-bottom: 30px;">
                            <div style="margin-bottom: 15px;">
                                <strong style="color: #008485;">예약 번호:</strong> %s
                            </div>
                            <div style="margin-bottom: 15px;">
                                <strong style="color: #008485;">상담 유형:</strong> %s
                            </div>
                            <div style="margin-bottom: 15px;">
                                <strong style="color: #008485;">담당 상담사:</strong> %s
                            </div>
                            <div>
                                <strong style="color: #008485;">상담 일시:</strong> %s
                            </div>
                        </div>

                        <div style="text-align: center; background: #e8f5e9; padding: 20px; border-radius: 8px; margin-bottom: 30px;">
                            <div style="font-size: 24px; margin-bottom: 10px;">✅</div>
                            <h3 style="color: #2e7d32; margin-bottom: 5px;">예약 확정 완료!</h3>
                            <p style="color: #388e3c; font-size: 14px;">예약된 시간에 상담이 진행됩니다</p>
                        </div>

                        <div style="background: #fff3e0; border-left: 4px solid #ff9800; padding: 15px; margin-bottom: 30px;">
                            <h4 style="color: #e65100; margin-bottom: 10px;">📋 상담 안내사항</h4>
                            <ul style="color: #bf360c; font-size: 14px; line-height: 1.6; margin: 0; padding-left: 20px;">
                                <li>상담 10분 전까지 준비해 주세요</li>
                                <li>상담 유형에 따라 필요한 자료를 미리 준비해 주세요</li>
                                <li>상담 취소는 상담 24시간 전까지 가능합니다</li>
                                <li>문의사항이 있으시면 언제든 연락 주세요</li>
                            </ul>
                        </div>

                        <div style="text-align: center; border-top: 1px solid #dee2e6; padding-top: 20px;">
                            <p style="color: #008485; font-weight: bold; margin-bottom: 10px;">HANAinPLAN</p>
                            <p style="color: #6c757d; font-size: 12px; margin: 0;">
                                하나인플랜에서 더 나은 금융 계획을 세워보세요
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """, customerName, consultId, consultationTypeKorean, consultantName, formattedDateTime);
    }

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

    public ConsultationResponseDto getConsultationDetails(String consultId) {
        log.info("상담 상세 정보 조회 - consultId: {}", consultId);

        Consult consult = consultRepository.findById(consultId)
                .orElseThrow(() -> new IllegalArgumentException("상담을 찾을 수 없습니다. ID: " + consultId));

        ConsultationResponseDto dto = convertToDto(consult);

        try {
            Long consultantId = Long.valueOf(consult.getConsultantId());
            userRepository.findById(consultantId).ifPresent(consultant -> {
                dto.setConsultantDepartment("상담팀");
            });
        } catch (Exception e) {
            log.warn("상담사 부서 정보 조회 실패 - consultId: {}", consultId, e);
        }

        return dto;
    }

    @Transactional
    public ConsultationResponseDto cancelConsultation(String consultId, Long customerId) {
        log.info("상담 취소 - consultId: {}, customerId: {}", consultId, customerId);

        Consult consult = consultRepository.findById(consultId)
                .orElseThrow(() -> new IllegalArgumentException("상담을 찾을 수 없습니다. ID: " + consultId));

        if (!String.valueOf(customerId).equals(consult.getCustomerId())) {
            throw new IllegalArgumentException("본인의 상담만 취소할 수 있습니다.");
        }

        if ("취소".equals(consult.getConsultStatus())) {
            throw new IllegalArgumentException("이미 취소된 상담입니다.");
        }

        if ("상담완료".equals(consult.getConsultStatus()) || "상담중".equals(consult.getConsultStatus())) {
            throw new IllegalArgumentException("완료된 상담이나 진행 중인 상담은 취소할 수 없습니다.");
        }

        consult.setConsultStatus("취소");
        Consult updatedConsult = consultRepository.save(consult);

        try {
            deleteConsultationSchedule(consult);
        } catch (Exception e) {
            log.error("상담 일정 삭제 실패 - consultId: {}", consultId, e);
        }

        try {
            createCancellationNotificationForConsultant(consult);
        } catch (Exception e) {
            log.error("상담 취소 알림 생성 실패 - consultId: {}", consultId, e);
        }

        log.info("상담 취소 완료 - consultId: {}", consultId);
        return convertToDto(updatedConsult);
    }

    private void deleteConsultationSchedule(Consult consult) {
        try {
            Long consultantId = Long.valueOf(consult.getConsultantId());
            Long customerId = Long.valueOf(consult.getCustomerId());

            String customerName = getCustomerNameById(consult.getCustomerId());
            String expectedTitle = customerName + "님 상담";

            log.info("상담 일정 삭제 시도 - consultantId: {}, customerId: {}, expectedTitle: {}", 
                    consultantId, customerId, expectedTitle);

            scheduleService.deleteConsultationSchedule(consultantId, customerId, consult.getReservationDatetime());

            log.info("상담 일정 삭제 완료 - consultantId: {}, customerId: {}", consultantId, customerId);
        } catch (Exception e) {
            log.error("상담 일정 삭제 실패 - consultId: {}", consult.getConsultId(), e);
            throw e;
        }
    }

    private void createCancellationNotificationForConsultant(Consult consult) {
        try {
            Long consultantId = Long.valueOf(consult.getConsultantId());

            String title = "상담 취소 알림";
            String content = String.format("고객이 상담을 취소했습니다.\n고객명: %s\n상담 일시: %s\n상담 유형: %s",
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
            log.info("상담사 취소 알림 생성 완료 - consultantId: {}, consultId: {}", consultantId, consult.getConsultId());
        } catch (Exception e) {
            log.error("상담사 취소 알림 생성 실패 - consultId: {}", consult.getConsultId(), e);
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

    private String getConsultationIcon(String consultationType) {
        switch (consultationType) {
            case "general": return "💬";
            case "product": return "📋";
            case "asset-management": return "💰";
            default: return "💼";
        }
    }
}