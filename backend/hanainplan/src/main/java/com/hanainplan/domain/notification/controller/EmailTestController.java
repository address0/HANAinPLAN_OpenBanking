package com.hanainplan.domain.notification.controller;

import com.hanainplan.domain.notification.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Email Test", description = "이메일 전송 테스트 API")
@CrossOrigin(origins = "*")
public class EmailTestController {

    private final EmailService emailService;

    @PostMapping("/test/simple")
    @Operation(summary = "간단한 텍스트 이메일 테스트", description = "텍스트 이메일 전송을 테스트합니다.")
    public ResponseEntity<?> testSimpleEmail(@RequestBody Map<String, String> request) {
        try {
            String to = request.get("to");
            String subject = request.getOrDefault("subject", "[하나인플랜] 이메일 테스트");
            String text = request.getOrDefault("text", "이것은 테스트 이메일입니다.");

            if (to == null || to.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "수신자 이메일 주소가 필요합니다."
                ));
            }

            log.info("간단한 이메일 테스트 요청 - to: {}, subject: {}", to, subject);

            boolean success = emailService.sendSimpleEmail(to, subject, text);

            return ResponseEntity.ok(Map.of(
                "success", success,
                "message", success ? "이메일이 성공적으로 전송되었습니다." : "이메일 전송에 실패했습니다.",
                "to", to,
                "subject", subject
            ));

        } catch (Exception e) {
            log.error("간단한 이메일 테스트 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "이메일 테스트 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/test/html")
    @Operation(summary = "HTML 이메일 테스트", description = "HTML 이메일 전송을 테스트합니다.")
    public ResponseEntity<?> testHtmlEmail(@RequestBody Map<String, String> request) {
        try {
            String to = request.get("to");
            String subject = request.getOrDefault("subject", "[하나인플랜] HTML 이메일 테스트");
            String htmlContent = request.getOrDefault("htmlContent", 
                "<h1>테스트 이메일</h1><p>이것은 <strong>HTML</strong> 테스트 이메일입니다.</p>");

            if (to == null || to.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "수신자 이메일 주소가 필요합니다."
                ));
            }

            log.info("HTML 이메일 테스트 요청 - to: {}, subject: {}", to, subject);

            boolean success = emailService.sendHtmlEmail(to, subject, htmlContent);

            return ResponseEntity.ok(Map.of(
                "success", success,
                "message", success ? "HTML 이메일이 성공적으로 전송되었습니다." : "HTML 이메일 전송에 실패했습니다.",
                "to", to,
                "subject", subject
            ));

        } catch (Exception e) {
            log.error("HTML 이메일 테스트 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "HTML 이메일 테스트 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/test/consultation-request")
    @Operation(summary = "상담 예약 신청 이메일 테스트", description = "상담 예약 신청 이메일을 테스트합니다.")
    public ResponseEntity<?> testConsultationRequestEmail(@RequestBody Map<String, String> request) {
        try {
            String to = request.get("to");
            String customerName = request.getOrDefault("customerName", "홍길동");
            String consultantName = request.getOrDefault("consultantName", "김상담");
            String consultationType = request.getOrDefault("consultationType", "일반");
            String consultId = request.getOrDefault("consultId", "CONS20250108123456");

            if (to == null || to.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "수신자 이메일 주소가 필요합니다."
                ));
            }

            log.info("상담 예약 신청 이메일 테스트 요청 - to: {}, customerName: {}", to, customerName);

            String htmlContent = buildTestConsultationRequestEmailHtml(
                customerName, consultantName, consultationType, consultId
            );

            boolean success = emailService.sendHtmlEmail(to, "[하나인플랜] 상담 예약 신청이 접수되었습니다", htmlContent);

            return ResponseEntity.ok(Map.of(
                "success", success,
                "message", success ? "상담 예약 신청 이메일이 성공적으로 전송되었습니다." : "상담 예약 신청 이메일 전송에 실패했습니다.",
                "to", to,
                "customerName", customerName,
                "consultantName", consultantName,
                "consultId", consultId
            ));

        } catch (Exception e) {
            log.error("상담 예약 신청 이메일 테스트 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "상담 예약 신청 이메일 테스트 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    private String buildTestConsultationRequestEmailHtml(
            String customerName,
            String consultantName,
            String consultationType,
            String consultId
    ) {
        return String.format("""
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>상담 예약 신청 접수</title>
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
                            background: #e3f2fd;
                            border: 1px solid #2196f3;
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
                        <h1>📋 상담 예약 신청이 접수되었습니다</h1>
                    </div>
                    <div class="content">
                        <p><strong>%s</strong>님, 안녕하세요!</p>
                        <p>상담 예약 신청이 정상적으로 접수되었습니다.</p>

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
                                <span class="label">예약 일시:</span>
                                <span class="value">2025년 01월 15일 14:00</span>
                            </div>
                        </div>

                        <div class="status-box">
                            <strong>📧 예약 확인 후 확정 시 등록된 메일로 안내드리겠습니다</strong>
                        </div>

                        <p style="color: #666; font-size: 14px;">
                            상담사가 예약을 확인한 후 확정되면 별도로 안내 메일을 보내드립니다.<br>
                            문의사항이 있으시면 고객센터(1588-1111)로 연락주시기 바랍니다.
                        </p>
                    </div>
                    <div class="footer">
                        <p>본 메일은 발신 전용입니다. 문의사항은 고객센터를 이용해주세요.</p>
                        <p>© 2025 하나인플랜. All rights reserved.</p>
                    </div>
                </body>
                </html>
                """, customerName, consultId, consultationType, consultantName);
    }
}